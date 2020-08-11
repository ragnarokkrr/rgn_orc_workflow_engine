package ragna.wf.orc.engine.domain.workflow.service;

import lombok.RequiredArgsConstructor;
import org.fissore.slf4j.FluentLogger;
import org.fissore.slf4j.FluentLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ragna.wf.orc.engine.domain.workflow.model.WorkflowRoot;
import ragna.wf.orc.engine.domain.workflow.repository.WorkflowRepository;
import ragna.wf.orc.engine.domain.workflow.service.mapper.FinishTaskCommandMapper;
import ragna.wf.orc.engine.domain.workflow.service.mapper.RegisterTaskResultsCommandMapper;
import ragna.wf.orc.engine.domain.workflow.service.mapper.WorkflowMapper;
import ragna.wf.orc.engine.domain.workflow.service.vo.FinishTaskCommand;
import ragna.wf.orc.engine.domain.workflow.service.vo.RegisterTaskResultsCommand;
import ragna.wf.orc.engine.domain.workflow.service.vo.TriggerFirstTaskCommand;
import ragna.wf.orc.engine.domain.workflow.service.vo.WorkflowVO;
import reactor.core.publisher.Mono;

import javax.validation.Validator;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class WorkflowTaskManagementService {
  private static final FluentLogger LOGGER =
          FluentLoggerFactory.getLogger(WorkflowTaskManagementService.class);
  private final Validator validator;
  private final WorkflowRepository workflowRepository;

  public Mono<WorkflowVO> triggerFirstTask(final TriggerFirstTaskCommand triggerFirstTaskCommand) {
    LOGGER.info().log("Triggering first task for workflow. {}", triggerFirstTaskCommand);
    final var constraintViolations = validator.validate(triggerFirstTaskCommand);
    if (!constraintViolations.isEmpty()) {
      return Mono.error(
              WorkflowExceptionFactory.newInvalidArgumentException(
                      triggerFirstTaskCommand, constraintViolations));
    }

    return Mono.just(triggerFirstTaskCommand.getWorkflowId())
            .flatMap(this::findByWorkflowId)
            .switchIfEmpty(
                    Mono.error(
                            WorkflowExceptionFactory.newOrcNotFoundException(
                                    triggerFirstTaskCommand.getWorkflowId())))
            .map(WorkflowRoot::triggerFirstTask)
            .flatMap(this::saveWorkflowRoot)
            .map(WorkflowMapper.INSTANCE::toService)
            .doOnSuccess(
                    workflowVO ->
                            LOGGER
                                    .info()
                                    .log("First Task Triggered {}. ({})", workflowVO, triggerFirstTaskCommand));
  }

  public Mono<WorkflowVO> finishTaskAndAdvance(final FinishTaskCommand finishTaskCommand) {
    LOGGER.info().log("Finishing task for workflow. {}", finishTaskCommand);
    final var constraintViolations = validator.validate(finishTaskCommand);
    if (!constraintViolations.isEmpty()) {
      return Mono.error(
              WorkflowExceptionFactory.newInvalidArgumentException(
                      finishTaskCommand, constraintViolations));
    }

    return findByWorkflowId(finishTaskCommand.getWorkflowId())
            .switchIfEmpty(
                    Mono.error(
                            WorkflowExceptionFactory.newOrcNotFoundException(
                                    finishTaskCommand.getWorkflowId())))
            .map(workflowRoot -> this.finishDomainWorkflowTask(workflowRoot, finishTaskCommand))
            .flatMap(this::saveWorkflowRoot)
            .map(WorkflowMapper.INSTANCE::toService)
            .doOnSuccess(
                    workflowVO ->
                            LOGGER.info().log("Task Finished {}. ({})", workflowVO, finishTaskCommand));
  }

  public Mono<WorkflowVO> registerTaskResult(
          final RegisterTaskResultsCommand registerTaskResultsCommand) {

    LOGGER.info().log("Registering task results for workflow. {}", registerTaskResultsCommand);
    final var constraintViolations = validator.validate(registerTaskResultsCommand);
    if (!constraintViolations.isEmpty()) {
      return Mono.error(
              WorkflowExceptionFactory.newInvalidArgumentException(
                      registerTaskResultsCommand, constraintViolations));
    }

    return findByWorkflowId(registerTaskResultsCommand.getWorkflowId())
            .switchIfEmpty(
                    Mono.error(
                            WorkflowExceptionFactory.newOrcNotFoundException(
                                    registerTaskResultsCommand.getWorkflowId())))
            .map(
                    workflowRoot -> this.registerDomainTaskResult(workflowRoot, registerTaskResultsCommand))
            .flatMap(this::saveWorkflowRoot)
            .map(WorkflowMapper.INSTANCE::toService)
            .doOnSuccess(
                    workflowVO ->
                            LOGGER
                                    .info()
                                    .log(
                                            "Registering task results {}. ({})",
                                            workflowVO,
                                            registerTaskResultsCommand));
  }

  private WorkflowRoot registerDomainTaskResult(
          final WorkflowRoot workflowRoot,
          final RegisterTaskResultsCommand registerTaskResultsCommand) {
    final var taskType =
            RegisterTaskResultsCommandMapper.INSTANCE.toModel(registerTaskResultsCommand.getTaskType());

    final var taskCriteriaResults =
            registerTaskResultsCommand.getResult().stream()
                    .map(RegisterTaskResultsCommandMapper.INSTANCE::toModel)
                    .collect(Collectors.toList());

    workflowRoot.registerTaskCriteriaEvaluationResults(
            taskType, registerTaskResultsCommand.getOrder(), taskCriteriaResults);

    return workflowRoot;
  }

  private WorkflowRoot finishDomainWorkflowTask(
          final WorkflowRoot workflowRoot, final FinishTaskCommand finishTaskCommand) {
    final var taskType = FinishTaskCommandMapper.INSTANCE.toModel(finishTaskCommand.getTaskType());
    final var result = FinishTaskCommandMapper.INSTANCE.toModel(finishTaskCommand.getResult());
    workflowRoot.finishTaskAndAdvance(taskType, finishTaskCommand.getOrder(), result);
    return workflowRoot;
  }

  private Mono<WorkflowRoot> findByWorkflowId(final String workflowId) {
    return workflowRepository
            .findById(workflowId)
            .doOnNext(savedWorkflowRoot -> LOGGER.debug().log("Workflow found! {}", workflowId))
            .doOnError(
                    throwable -> LOGGER.error().log("Error saving workflow {}", workflowId, throwable));
  }

  private Mono<WorkflowRoot> saveWorkflowRoot(final WorkflowRoot workflowRoot) {
    return workflowRepository
            .save(workflowRoot)
            .doOnNext(savedWorkflowRoot -> LOGGER.info().log("Workflow saved! {}", workflowRoot))
            .doOnError(
                    throwable -> LOGGER.error().log("Error saving workflow {}", workflowRoot, throwable));
  }
}
