package ragna.wf.orc.engine.domain.model;

import ragna.wf.orc.engine.domain.service.ConfiguredTaskCriteriaFactory;

import java.time.LocalDateTime;
import java.util.List;

public final class WorkflowConfigurationFixture {
    private WorkflowConfigurationFixture() {
    }

    public static Configuration sampleConfiguration() {
        return Configuration.builder()
                .id("configId-1")
                .configuredTasks(List.of(
                        ConfiguredTask.builder()
                                .order(1)
                                .taskType(TaskType.ANALYSIS)
                                .description("John Connor analysis")
                                .taskResponsible(TaskResponsible.builder()
                                        .id("jc_500")
                                        .name("John Connor")
                                        .email("john.connor@sky.net")
                                        .build())
                                .addAllCriteria(List.of(
                                        ConfiguredTaskCriteriaFactory.TASK_CRITERIA_ASC.get(),
                                        ConfiguredTaskCriteriaFactory.TASK_CRITERIA_DESC.get()
                                ))
                                .build(),
                        ConfiguredTask.builder()
                                .order(2)
                                .taskType(TaskType.DECISION)
                                .description("John Connor analysis")
                                .taskResponsible(TaskResponsible.builder()
                                        .id("sc_600")
                                        .name("Sarah Connor")
                                        .email("sarah.connor@sky.net")
                                        .build())
                                .addAllCriteria(List.of())
                                .build()
                ))
                .date(LocalDateTime.now())
                .status(Configuration.Status.ACTIVE)
                .build();
    }
}
