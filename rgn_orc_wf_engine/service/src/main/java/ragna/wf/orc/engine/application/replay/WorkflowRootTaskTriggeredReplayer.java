package ragna.wf.orc.engine.application.replay;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.fissore.slf4j.FluentLogger;
import org.fissore.slf4j.FluentLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import ragna.wf.orc.common.exceptions.ErrorCode;
import ragna.wf.orc.common.exceptions.OrcException;
import ragna.wf.orc.engine.application.messaging.output.TriggerTaskMessageProducer;
import ragna.wf.orc.engine.application.messaging.output.vo.TriggerTaskDto;
import ragna.wf.orc.engine.domain.tasks.TaskActivationCriteriaService;
import ragna.wf.orc.engine.domain.tasks.mappers.CriterionMapper;
import ragna.wf.orc.engine.domain.tasks.vo.CriteriaEvaluationQuery;
import ragna.wf.orc.engine.domain.tasks.vo.CriteriaEvaluationResult;
import ragna.wf.orc.engine.domain.workflow.model.PlannedTask;
import ragna.wf.orc.engine.domain.workflow.model.WorkflowRoot;
import ragna.wf.orc.engine.domain.workflow.model.events.WorkflowRootTaskTriggered;
import ragna.wf.orc.engine.domain.workflow.service.WorkflowTaskManagementService;
import ragna.wf.orc.engine.domain.workflow.service.vo.RegisterTaskResultsCommand;
import ragna.wf.orc.engine.infrastructure.storedevents.replay.main.MainStoredEventReplayerCallback;
import ragna.wf.orc.engine.infrastructure.storedevents.replay.main.vo.MainReplayContextVo;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class WorkflowRootTaskTriggeredReplayer
    implements MainStoredEventReplayerCallback<WorkflowRootTaskTriggered> {
  private static final FluentLogger LOGGER =
      FluentLoggerFactory.getLogger(WorkflowRootTaskTriggeredReplayer.class);
  private final TaskActivationCriteriaService taskActivationCriteriaService;
  private final WorkflowTaskManagementService workflowTaskManagementService;
  private final TriggerTaskMessageProducer triggerTaskMessageProducer;

  @Override
  public Mono<MainReplayContextVo> activateTaskIfConfigured(
      final MainReplayContextVo mainReplayContextVo) {
    final var workflowRootTaskTriggered =
        (WorkflowRootTaskTriggered) mainReplayContextVo.getStoredEventVo().getDomainEvent();
    final var workflowRoot = (WorkflowRoot) workflowRootTaskTriggered.getSource();

    final var lastTriggeredTaskOptional = workflowRoot.findLastTriggeredTask();

    if (lastTriggeredTaskOptional.isEmpty()) {
      LOGGER
          .debug()
          .log("Last triggered task not found: {}", mainReplayContextVo.getStoredEventVo());
      return Mono.just(
          mainReplayContextVo.matchResult(
              MainReplayContextVo.MatchResult.builder()
                  .matchResultType(MainReplayContextVo.MatchResultEnum.TASK_NOT_FOUND)
                  .build()));
    }

    final var configuredTaskOptional =
        workflowRoot.findTaskConfiguration(lastTriggeredTaskOptional.get());

    if (configuredTaskOptional.isEmpty()) {
      LOGGER
          .debug()
          .log(
              "Configured task for last triggered task not found: {}",
              mainReplayContextVo.getStoredEventVo());

      return Mono.just(
          mainReplayContextVo.matchResult(
              MainReplayContextVo.MatchResult.builder()
                  .matchResultType(MainReplayContextVo.MatchResultEnum.TASK_CONFIGURATION_NOT_FOUND)
                  .build()));
    }

    if (CollectionUtils.isEmpty(configuredTaskOptional.get().getConfiguredTaskCriteriaList())) {
      LOGGER
          .debug()
          .log(
              "No Configured task criteria for last triggered task not found: {}",
              mainReplayContextVo.getStoredEventVo());
      return Mono.just(
          mainReplayContextVo.matchResult(
              MainReplayContextVo.MatchResult.builder()
                  .matchResultType(MainReplayContextVo.MatchResultEnum.NO_CRITERIA_FOUND)
                  .build()));
    }

    final var criteriaEvaluationQueryBuilder =
        CriteriaEvaluationQuery.builder()
            .customerId(workflowRoot.getCustomerRequest().getCustomerId());

    configuredTaskOptional.get().getConfiguredTaskCriteriaList().stream()
        .map(CriterionMapper.INSTANCE::mapToService)
        .forEach(criteriaEvaluationQueryBuilder::addCriterion);

    return taskActivationCriteriaService
        .matchTaskCriteria(criteriaEvaluationQueryBuilder.build())
        .map(mainReplayContextVo::criteriaEvaluationResult)
        .map(this::activateTask)
        .doOnNext(
            mainReplayContextVo1 ->
                LOGGER.info().log("activateTaskIfConfigured {}", mainReplayContextVo1));
  }

  @Override
  public Mono<MainReplayContextVo> doReplay(final MainReplayContextVo mainReplayContextVo) {
    return saveTaskCriteriaMatchResultIfNecessary(mainReplayContextVo);
  }

  @Override
  public Mono<MainReplayContextVo> publish(final MainReplayContextVo mainReplayContextVo) {
    LOGGER.info().log("Publishing {} to Orc topic output", mainReplayContextVo.getStoredEventVo());
    final var triggerTaskDto = TriggerTaskDto.builder().build();
    return Mono.fromCallable(() -> triggerTaskMessageProducer.send(triggerTaskDto))
        .then(Mono.just(mainReplayContextVo));
  }

  Mono<MainReplayContextVo> saveTaskCriteriaMatchResultIfNecessary(
      final MainReplayContextVo mainReplayContextVo) {
    final var workflowRoot =
        (WorkflowRoot) mainReplayContextVo.getStoredEventVo().getDomainEvent().getSource();
    final var lastTriggeredTaskOptional = workflowRoot.findLastTriggeredTask();

    if (lastTriggeredTaskOptional.isEmpty()) {
      return Mono.error(newNoTriggeredTaskFoundException(workflowRoot));
    }

    if (mainReplayContextVo.getCriteriaEvaluationResult().isEmpty()
        && mainReplayContextVo.getMatchResult().getMatchResultType()
            != MainReplayContextVo.MatchResultEnum.NO_CRITERIA_FOUND) {
      return Mono.error(newActivationCriteriaResultsNotFoundException(workflowRoot));
    }

    if (mainReplayContextVo.getCriteriaEvaluationResult().isEmpty()
        && mainReplayContextVo.getMatchResult().getMatchResultType()
            == MainReplayContextVo.MatchResultEnum.NO_CRITERIA_FOUND) {
      return Mono.just(mainReplayContextVo);
    }

    final var registerTaskResultsCommand =
        buildRegisterTaskResultCommand(
            workflowRoot,
            lastTriggeredTaskOptional.get(),
            mainReplayContextVo.getCriteriaEvaluationResult().get());
    return saveTaskCriteriaMatchResult(mainReplayContextVo, registerTaskResultsCommand);
  }

  Mono<MainReplayContextVo> saveTaskCriteriaMatchResult(
      final MainReplayContextVo mainReplayContextVo,
      final RegisterTaskResultsCommand registerTaskResultsCommand) {
    return workflowTaskManagementService
        .registerTaskActivationResult(registerTaskResultsCommand)
        .then(Mono.just(mainReplayContextVo));
  }

  private RegisterTaskResultsCommand buildRegisterTaskResultCommand(
      final WorkflowRoot workflowRoot,
      final PlannedTask lastTriggeredTask,
      final CriteriaEvaluationResult criteriaEvaluationResult) {
    final var domainTaskType =
        RegisterTaskResultsCommand.TaskType.valueOf(lastTriggeredTask.getTaskType().name());

    final var criteriaResultList =
        criteriaEvaluationResult.getCriteriaResultList().stream()
            .map(
                criterionResult ->
                    RegisterTaskResultsCommand.TaskCriteriaResult.builder()
                        .id(criterionResult.getId())
                        .value(criterionResult.getValue())
                        .result(mapTaskActivationCriteriaResult(criterionResult))
                        .build())
            .collect(Collectors.toList());

    return RegisterTaskResultsCommand.builder()
        .workflowId(workflowRoot.getId())
        .taskType(domainTaskType)
        .order(lastTriggeredTask.getOrder())
        .taskCriteriaResults(criteriaResultList)
        .build();
  }

  private RegisterTaskResultsCommand.TaskCriteriaResult.Result mapTaskActivationCriteriaResult(
      final CriteriaEvaluationResult.CriterionResult criterionResult) {
    return switch (criterionResult.getResultType()) {
      case MATCHED -> RegisterTaskResultsCommand.TaskCriteriaResult.Result.APPROVED;
      case ERROR -> RegisterTaskResultsCommand.TaskCriteriaResult.Result.ERROR;
      default -> RegisterTaskResultsCommand.TaskCriteriaResult.Result.DISAPPROVED;
    };
  }

  MainReplayContextVo.MatchResultEnum mapMatchResult(
      final CriteriaEvaluationResult.CriteriaResultType criteriaResultType) {
    return switch (criteriaResultType) {
      case MATCHED -> MainReplayContextVo.MatchResultEnum.MATCHED;
      case ERROR -> MainReplayContextVo.MatchResultEnum.ERROR;
      case UNMATCHED, INVALID_CRITERION -> MainReplayContextVo.MatchResultEnum.UNMATCHED;
    };
  }

  MainReplayContextVo activateTask(final MainReplayContextVo mainReplayContextVo) {
    if (mainReplayContextVo.getCriteriaEvaluationResult().isEmpty()) {
      return mainReplayContextVo;
    }
    final var matchResult =
        MainReplayContextVo.MatchResult.builder()
            .matchResultType(
                mapMatchResult(
                    mainReplayContextVo
                        .getCriteriaEvaluationResult()
                        .get()
                        .getCriteriaResultType()))
            .build();
    return mainReplayContextVo.matchResult(matchResult);
  }

  private OrcException newNoTriggeredTaskFoundException(final WorkflowRoot workflowRoot) {
    return new OrcException(
        String.format(
            "No triggered task in workflow: %s. (%s)",
            workflowRoot.getId(), workflowRoot.getCustomerRequest()),
        ErrorCode.WORKFLOW_TASK_NOT_FOUND);
  }

  private OrcException newActivationCriteriaResultsNotFoundException(
      final WorkflowRoot workflowRoot) {
    return new OrcException(
        String.format(
            "No triggered task in workflow: %s. (%s)",
            workflowRoot.getId(), workflowRoot.getCustomerRequest()),
        ErrorCode.WORKFLOW_TASK_ACTIVATION_EVALUATION_RESULT_NOT_FOUND);
  }
}
