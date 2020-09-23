package ragna.wf.orc.engine.domain.workflow.service;

import lombok.RequiredArgsConstructor;
import org.fissore.slf4j.FluentLogger;
import org.fissore.slf4j.FluentLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import ragna.wf.orc.engine.domain.metadata.service.WorkflowMetadataService;
import ragna.wf.orc.engine.domain.metadata.service.vo.ConfigurationRequest;
import ragna.wf.orc.engine.domain.metadata.service.vo.ConfigurationVO;
import ragna.wf.orc.engine.domain.workflow.model.WorkflowRoot;
import ragna.wf.orc.engine.domain.workflow.repository.WorkflowRepository;
import ragna.wf.orc.engine.domain.workflow.service.mapper.ConfigurationMapper;
import ragna.wf.orc.engine.domain.workflow.service.mapper.CustomerRequestMapper;
import ragna.wf.orc.engine.domain.workflow.service.mapper.WorkflowMapper;
import ragna.wf.orc.engine.domain.workflow.service.vo.CreateWorkflowCommand;
import ragna.wf.orc.engine.domain.workflow.service.vo.WorkflowVO;
import reactor.core.publisher.Mono;

import javax.validation.Validator;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class WorkflowCreationService {
  private static final FluentLogger LOGGER =
      FluentLoggerFactory.getLogger(WorkflowCreationService.class);

  private final WorkflowMetadataService workflowMetadataService;
  private final Validator validator;
  private final WorkflowRepository workflowRepository;
  private final TransactionalOperator transactionalOperator;

  @Transactional
  public Mono<WorkflowVO> createWorkflow(final CreateWorkflowCommand createWorkflowCommand) {
    LOGGER.info().log("Creating workflow. {}", createWorkflowCommand);
    final var constraintViolations = validator.validate(createWorkflowCommand);
    if (!constraintViolations.isEmpty()) {
      return Mono.error(
          WorkflowExceptionFactory.newInvalidArgumentException(
              createWorkflowCommand, constraintViolations));
    }

    final var getConfigurationVOMono = getConfigurationVO(createWorkflowCommand);

    final var newWorkflowRootMono =
        findWorkflowByCustomerRequest(createWorkflowCommand)
            .switchIfEmpty(
                createWorkflowRootMono(createWorkflowCommand)
                    .zipWith(getConfigurationVOMono, this::configureWorkflow)
                    .flatMap(this::saveWorkflowRoot));

    return transactionalOperator.transactional(
        newWorkflowRootMono
            .map(WorkflowMapper.INSTANCE::toService)
            .doOnSuccess(
                workflowVO ->
                    LOGGER
                        .info()
                        .log("Workflow created {}. ({})", workflowVO, createWorkflowCommand)));
  }

  private Mono<WorkflowRoot> findWorkflowByCustomerRequest(
      final CreateWorkflowCommand createWorkflowCommand) {
    return workflowRepository
        .findByCustomerRequest(createWorkflowCommand.getId())
        .doOnSuccess(
            workflowRoot ->
                LOGGER
                    .debug()
                    .log(
                        "Found WorkflowRoot for {}? '{}'",
                        createWorkflowCommand,
                        workflowRoot != null));
  }

  private Mono<WorkflowRoot> createWorkflowRootMono(
      final CreateWorkflowCommand createWorkflowCommand) {
    return Mono.just(createWorkflowCommand)
        .map(CustomerRequestMapper.INSTANCE::toModel)
        .map(WorkflowRoot::createWorkflowRoot)
        .doOnNext(
            workflowRoot ->
                LOGGER.debug().log("Workflow created {}.", workflowRoot, createWorkflowCommand));
  }

  private Mono<WorkflowRoot> saveWorkflowRoot(final WorkflowRoot workflowRoot) {
    return workflowRepository
        .save(workflowRoot)
        .doOnNext(savedWorkflowRoot -> LOGGER.info().log("Workflow saved! {}", workflowRoot))
        .doOnError(
            throwable -> LOGGER.error().log("Error saving workflow {}", workflowRoot, throwable));
  }

  private WorkflowRoot configureWorkflow(
      final WorkflowRoot workflowRoot, final ConfigurationVO configurationVO) {
    final var configuration = ConfigurationMapper.INSTANCE.toModel(configurationVO);
    return workflowRoot.addWorkflowConfiguration(configuration).createExecutionPlan().configured();
  }

  private Mono<ConfigurationVO> getConfigurationVO(
      final CreateWorkflowCommand createWorkflowCommand) {
    return workflowMetadataService
        .peekConfigurationForWorkflow(
            ConfigurationRequest.builder()
                .customerId(createWorkflowCommand.getCustomerId())
                .build())
        .doOnSubscribe(
            subscription ->
                LOGGER.debug().log("Looking for configuration. {}", createWorkflowCommand))
        .doOnError(
            Throwable.class,
            throwable ->
                LOGGER
                    .error()
                    .log("Can' t find configuration for", createWorkflowCommand, throwable))
        .doOnSuccess(
            configurationVO ->
                LOGGER
                    .debug()
                    .log("Configuration found {}! {}", createWorkflowCommand, configurationVO));
  }

}
