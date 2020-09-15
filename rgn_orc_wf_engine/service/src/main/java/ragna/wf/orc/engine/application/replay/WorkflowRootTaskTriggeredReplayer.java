package ragna.wf.orc.engine.application.replay;

import lombok.RequiredArgsConstructor;
import org.fissore.slf4j.FluentLogger;
import org.fissore.slf4j.FluentLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ragna.wf.orc.common.exceptions.ErrorCode;
import ragna.wf.orc.common.exceptions.OrcException;
import ragna.wf.orc.engine.domain.tasks.TaskActivationCriteriaService;
import ragna.wf.orc.engine.domain.tasks.mappers.CriterionMapper;
import ragna.wf.orc.engine.domain.tasks.vo.CriteriaEvaluationQuery;
import ragna.wf.orc.engine.domain.tasks.vo.CriteriaEvaluationResult;
import ragna.wf.orc.engine.domain.workflow.model.WorkflowRoot;
import ragna.wf.orc.engine.domain.workflow.model.events.WorkflowRootTaskTriggered;
import ragna.wf.orc.engine.infrastructure.storedevents.replay.main.MainStoredEventReplayerCallback;
import ragna.wf.orc.engine.infrastructure.storedevents.replay.main.vo.MainReplayContextVo;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class WorkflowRootTaskTriggeredReplayer implements MainStoredEventReplayerCallback<WorkflowRootTaskTriggered> {
    private static final FluentLogger LOGGER =
            FluentLoggerFactory.getLogger(WorkflowRootTaskTriggeredReplayer.class);
    private final TaskActivationCriteriaService taskActivationCriteriaService;

    @Override
    public Mono<MainReplayContextVo> activateTaskIfConfigured(final MainReplayContextVo mainReplayContextVo) {
        final var workflowRootTaskTriggered = (WorkflowRootTaskTriggered) mainReplayContextVo.getStoredEventVo().getDomainEvent();
        final var workflowRoot = (WorkflowRoot) workflowRootTaskTriggered.getSource();

        final var lastTriggeredTaskOptional = workflowRoot.findLastTriggeredTask();

        if (lastTriggeredTaskOptional.isEmpty()) {
            return Mono.just(mainReplayContextVo.matchResult(MainReplayContextVo.MatchResult.builder()
                    .matchResultType(MainReplayContextVo.MatchResultEnum.TASK_NOT_FOUND)
                    .build()));
        }

        final var configuredTaskOptional = workflowRoot.findTaskConfiguration(lastTriggeredTaskOptional.get());

        if (configuredTaskOptional.isEmpty()) {
            return Mono.just(mainReplayContextVo.matchResult(MainReplayContextVo.MatchResult.builder()
                    .matchResultType(MainReplayContextVo.MatchResultEnum.TASK_CONFIGURATION_NOT_FOUND)
                    .build()));
        }

        final var criteriaEvaluationQueryBuilder = CriteriaEvaluationQuery.builder()
                .customerId(workflowRoot.getCustomerRequest().getCustomerId());

        configuredTaskOptional.get().getConfiguredTaskCriteriaList().stream()
                .map(CriterionMapper.INSTANCE::mapToService)
                .forEach(criteriaEvaluationQueryBuilder::addCriterion);

        return taskActivationCriteriaService.matchTaskCriteria(criteriaEvaluationQueryBuilder.build())
                .map(criteriaEvaluationResult ->
                        mainReplayContextVo.matchResult(MainReplayContextVo.MatchResult.builder()
                                .matchResultType(mapMatchResult(criteriaEvaluationResult.getCriteriaResultType())).build())
                                .criteriaEvaluationResult(criteriaEvaluationResult)
                )
                ;
    }

    private MainReplayContextVo.MatchResultEnum mapMatchResult(CriteriaEvaluationResult.CriteriaResultType criteriaResultType) {
        return switch (criteriaResultType) {
            case MATCHED -> MainReplayContextVo.MatchResultEnum.MATCHED;
            case ERROR -> MainReplayContextVo.MatchResultEnum.ERROR;
            case UNMATCHED, INVALID_CRITERION -> MainReplayContextVo.MatchResultEnum.UNMATCHED;
        };
    }

    @Override
    public Mono<MainReplayContextVo> publish(final MainReplayContextVo mainReplayContextVo) {
        LOGGER.info().log("Publishing {} to Orc topic output", mainReplayContextVo.getStoredEventVo());
        return Mono.just(mainReplayContextVo);
    }

    private OrcException newNoTriggeredTaskFoundException(WorkflowRoot workflowRoot) {
        return new OrcException(String.format("No triggered task in workflow: %s. (%s)", workflowRoot.getId(), workflowRoot.getCustomerRequest())
                , ErrorCode.WORKFLOW_TASK_NOT_FOUND);
    }
}
