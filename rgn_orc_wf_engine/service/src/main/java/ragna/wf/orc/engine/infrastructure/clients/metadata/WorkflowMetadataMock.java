package ragna.wf.orc.engine.infrastructure.clients.metadata;

import org.springframework.stereotype.Component;
import ragna.wf.orc.engine.domain.metadata.service.WorkflowMetadataService;
import ragna.wf.orc.engine.domain.metadata.service.vo.ConfigurationRequest;
import ragna.wf.orc.engine.domain.metadata.service.vo.ConfigurationVO;
import ragna.wf.orc.engine.domain.workflow.model.Configuration;
import ragna.wf.orc.engine.domain.workflow.model.ConfiguredTask;
import ragna.wf.orc.engine.domain.workflow.model.TaskType;
import ragna.wf.orc.engine.domain.workflow.service.ConfiguredTaskCriteriaFactory;
import ragna.wf.orc.engine.domain.workflow.service.mapper.ConfigurationMapper;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class WorkflowMetadataMock implements WorkflowMetadataService {
    @Override
    public Mono<ConfigurationVO> peekConfigurationForWorkflow(final ConfigurationRequest configurationRequest) {
        return Mono.just(buildConfigurationMock());
    }

    private ConfigurationVO buildConfigurationMock() {

        return ConfigurationMapper.INSTANCE.toService(Configuration.builder()
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
                                                                        ConfiguredTaskCriteriaFactory.TASK_CRITERIA_ASC.get(),
                                                                        ConfiguredTaskCriteriaFactory.TASK_CRITERIA_DESC.get())))
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
                .build());
    }
}
