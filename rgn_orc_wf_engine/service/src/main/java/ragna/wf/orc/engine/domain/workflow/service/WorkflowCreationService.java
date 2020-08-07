package ragna.wf.orc.engine.domain.workflow.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.fissore.slf4j.FluentLogger;
import org.fissore.slf4j.FluentLoggerFactory;
import org.springframework.stereotype.Service;
import ragna.wf.orc.common.exceptions.OrcInvalidArgument;
import ragna.wf.orc.engine.domain.configuration.service.WorkflowMetadataService;
import ragna.wf.orc.engine.domain.configuration.service.vo.ConfigurationRequest;
import ragna.wf.orc.engine.domain.configuration.service.vo.ConfigurationVO;
import ragna.wf.orc.engine.domain.workflow.model.WorkflowRoot;
import ragna.wf.orc.engine.domain.workflow.service.mapper.ConfigurationMapper;
import ragna.wf.orc.engine.domain.workflow.service.mapper.CustomerRequestMapper;
import ragna.wf.orc.engine.domain.workflow.service.mapper.WorkflowMapper;
import ragna.wf.orc.engine.domain.workflow.service.vo.CreateWorkflowCommand;
import ragna.wf.orc.engine.domain.workflow.service.vo.WorkflowVO;
import reactor.core.publisher.Mono;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

@Service
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class WorkflowCreationService {
  private static final FluentLogger LOGGER =
      FluentLoggerFactory.getLogger(WorkflowCreationService.class);

  private WorkflowMetadataService workflowMetadataService;
  private Validator validator;

  public Mono<WorkflowVO> createWorkflow(final CreateWorkflowCommand createWorkflowCommand) {
    LOGGER.info().log("Creating workflow. {}", createWorkflowCommand);
    final var constraintViolations = validator.validate(createWorkflowCommand);
    if (!constraintViolations.isEmpty()) {
      return Mono.error(newInvalidArgumentException(createWorkflowCommand, constraintViolations));
    }

    final var configurationVOMono = getConfigurationVO(createWorkflowCommand);

    final var workflowRootMono =
        Mono.just(createWorkflowCommand)
            .map(CustomerRequestMapper.INSTANCE::toModel)
            .map(WorkflowRoot::createWorkflowRoot);

    return workflowRootMono
        .zipWith(configurationVOMono, this::addWorkflowConfig)
        .map(WorkflowMapper.INSTANCE::toVO)
        .doOnSuccess(
            workflowVO ->
                LOGGER.info().log("Workflow created {}. ({})", workflowVO, createWorkflowCommand));
  }

  private WorkflowRoot addWorkflowConfig(
      final WorkflowRoot workflowRoot, final ConfigurationVO configurationVO) {
    final var configuration = ConfigurationMapper.INSTANCE.toModel(configurationVO);
    return workflowRoot.addWorkflowConfiguration(configuration);
  }

  private Mono<ConfigurationVO> getConfigurationVO(CreateWorkflowCommand createWorkflowCommand) {
    return workflowMetadataService.peekConfigurationForWorkflow(
        ConfigurationRequest.builder().customerId(createWorkflowCommand.getCustomerId()).build());
  }

  private OrcInvalidArgument newInvalidArgumentException(
      CreateWorkflowCommand createWorkflowCommand,
      Set<ConstraintViolation<CreateWorkflowCommand>> constraintViolations) {
    return new OrcInvalidArgument(
        String.format(
            "invalid createWorkflow command=%s, violations=%s",
            createWorkflowCommand, constraintViolations));
  }
}
