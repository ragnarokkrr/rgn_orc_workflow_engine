package ragna.wf.orc.engine.infrastructure.storedevents.replay.main;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import ragna.wf.orc.common.events.DomainEvent;
import ragna.wf.orc.engine.application.replay.WorkflowRootCreatedReplayer;
import ragna.wf.orc.engine.application.replay.WorkflowRootTaskEvaluatedReplayer;
import ragna.wf.orc.engine.application.replay.WorkflowRootTaskFinishedReplayer;
import ragna.wf.orc.engine.application.replay.WorkflowRootTaskTriggeredReplayer;
import ragna.wf.orc.engine.domain.workflow.model.events.WorkflowRootCreated;
import ragna.wf.orc.engine.domain.workflow.model.events.WorkflowRootTaskEvaluated;
import ragna.wf.orc.engine.domain.workflow.model.events.WorkflowRootTaskFinished;
import ragna.wf.orc.engine.domain.workflow.model.events.WorkflowRootTaskTriggered;

public enum MainStoredEventReplayerRegistry {
  WORKFLOW_ROOT_CREATED(WorkflowRootCreated.class, WorkflowRootCreatedReplayer.class),
  WORKFLOW_ROOT_TASK_TRIGGERED(
      WorkflowRootTaskTriggered.class, WorkflowRootTaskTriggeredReplayer.class),
  WORKFLOW_ROOT_TASK_EVALUATED(
      WorkflowRootTaskEvaluated.class, WorkflowRootTaskEvaluatedReplayer.class),
  WORKFLOW_ROOT_TASK_FINISHED(
      WorkflowRootTaskFinished.class, WorkflowRootTaskFinishedReplayer.class);

  private static final Map<
          Class<? extends DomainEvent>, Class<? extends MainStoredEventReplayerCallback>>
      HANDLER_MAP = new HashMap<>();

  static {
    Arrays.stream(values())
        .forEach(
            mainStoredEventReplayerRegistry ->
                HANDLER_MAP.put(
                    mainStoredEventReplayerRegistry.domainEventType,
                    mainStoredEventReplayerRegistry.workflowRootCreatedReplayerType));
  }

  private final Class<? extends DomainEvent> domainEventType;
  private final Class<? extends MainStoredEventReplayerCallback> workflowRootCreatedReplayerType;

  MainStoredEventReplayerRegistry(
      Class<? extends DomainEvent> domainEventType,
      Class<? extends MainStoredEventReplayerCallback> workflowRootCreatedReplayerType) {
    this.domainEventType = domainEventType;
    this.workflowRootCreatedReplayerType = workflowRootCreatedReplayerType;
  }

  public static Optional<Class<? extends MainStoredEventReplayerCallback>> matchReplayer(
      Class<? extends DomainEvent> domainEventType) {
    return Optional.ofNullable(HANDLER_MAP.get(domainEventType));
  }
}
