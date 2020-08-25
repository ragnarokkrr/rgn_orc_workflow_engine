package ragna.wf.orc.engine.infrastructure.storedevents.replay.main;

import ragna.wf.orc.common.events.DomainEvent;
import ragna.wf.orc.engine.application.replay.WorkflowRootCreatedReplayer;
import ragna.wf.orc.engine.domain.workflow.model.events.WorkflowRootCreated;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum MainStoredEventReplayerRegistry {
  WORKFLOW_ROOT_CREATED(WorkflowRootCreated.class, WorkflowRootCreatedReplayer.class);

  private static final Map<
          Class<? extends DomainEvent>, Class<? extends WorkflowRootCreatedReplayer>>
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
  private final Class<? extends WorkflowRootCreatedReplayer> workflowRootCreatedReplayerType;

  MainStoredEventReplayerRegistry(
      Class<? extends DomainEvent> domainEventType,
      Class<? extends WorkflowRootCreatedReplayer> workflowRootCreatedReplayerType) {
    this.domainEventType = domainEventType;
    this.workflowRootCreatedReplayerType = workflowRootCreatedReplayerType;
  }

  public static Optional<Class<? extends WorkflowRootCreatedReplayer>> matchReplayer(
      Class<? extends DomainEvent> domainEventType) {
    return Optional.ofNullable(HANDLER_MAP.get(domainEventType));
  }
}
