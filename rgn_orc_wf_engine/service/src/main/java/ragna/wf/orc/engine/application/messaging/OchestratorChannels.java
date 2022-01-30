package ragna.wf.orc.engine.application.messaging;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

public interface OchestratorChannels {

  @Input(Channels.CREATE_WORKFLOW)
  SubscribableChannel createWorkflow();

  @Output(Channels.TRIGGER_TASK)
  MessageChannel triggerTask();

  @Input(Channels.FINISH_TASK)
  SubscribableChannel finishTask();

  @Output(Channels.FINISH_WORKFLOW)
  MessageChannel finishWorkflow();

  interface Channels {
    String CREATE_WORKFLOW = "orc-create-workflow-input";
    String TRIGGER_TASK = "orc-trigger-task-output";
    String FINISH_TASK = "orc-finish-task-input";
    String FINISH_WORKFLOW = "orc-finish-workflow-output";
  }
}
