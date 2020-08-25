package ragna.wf.orc.engine.domain.workflow.model;

import ragna.wf.orc.engine.infrastructure.clients.metadata.ConfiguredTaskCriteriaMockFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class WorkflowModelFixture {
  private WorkflowModelFixture() {}

  public static Configuration sampleTwoTasksConfiguration() {
    return Configuration.builder()
        .id("configId-1")
        // Kryo serialization problems with immutable collections
        .configuredTasks(
            new ArrayList<>(
                List.of(
                    ConfiguredTask.builder()
                        .order(1)
                        .taskType(TaskType.ANALYSIS)
                        .description("John Connor analysis")
                        .taskResponsible(
                            ConfiguredTask.TaskResponsible.builder()
                                .id("jc_500")
                                .name("John Connor")
                                .email("john.connor@sky.net")
                                .build())
                        // Kryo serialization problems with immutable collections
                        .addAllCriteria(
                            new ArrayList<>(
                                List.of(
                                    ConfiguredTaskCriteriaMockFactory.TASK_CRITERIA_ASC.get(),
                                    ConfiguredTaskCriteriaMockFactory.TASK_CRITERIA_DESC.get())))
                        .build(),
                    ConfiguredTask.builder()
                        .order(2)
                        .taskType(TaskType.DECISION)
                        .description("John Connor analysis")
                        .taskResponsible(
                            ConfiguredTask.TaskResponsible.builder()
                                .id("sc_600")
                                .name("Sarah Connor")
                                .email("sarah.connor@sky.net")
                                .build())
                        // Kryo serialization problems with immutable collections
                        .addAllCriteria(new ArrayList<>())
                        .build())))
        .date(LocalDateTime.now())
        .status(Configuration.Status.ACTIVE)
        .build();
  }

  public static List<TaskCriteriaEvaluationCommand> johnConnorCriteriaEvaluation() {
    return new ArrayList<>(
        List.of(
            TaskCriteriaEvaluationCommand.builder()
                .id(ConfiguredTaskCriteriaMockFactory.TASK_CRITERIA_ASC.get().getId())
                .value("8.5")
                .result(TaskCriteriaEvaluationCommand.Result.APPROVED)
                .status(TaskCriteriaEvaluationCommand.Status.MATCHED)
                .build(),
            TaskCriteriaEvaluationCommand.builder()
                .id(ConfiguredTaskCriteriaMockFactory.TASK_CRITERIA_DESC.get().getId())
                .value("2")
                .result(TaskCriteriaEvaluationCommand.Result.APPROVED)
                .status(TaskCriteriaEvaluationCommand.Status.MATCHED)
                .build()));
  }
}
