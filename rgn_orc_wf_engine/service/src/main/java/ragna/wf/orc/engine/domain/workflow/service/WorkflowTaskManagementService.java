package ragna.wf.orc.engine.domain.workflow.service;

import java.util.stream.Collectors;
import javax.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.fissore.slf4j.FluentLogger;
import org.fissore.slf4j.FluentLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
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

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class WorkflowTaskManagementService {
  private static final FluentLogger LOGGER =
      FluentLoggerFactory.getLogger(WorkflowTaskManagementService.class);
  private final Validator validator;
  private final WorkflowRepository workflowRepository;
  private final TransactionalOperator transactionalOperator;

  @Transactional
  public Mono<WorkflowVO> triggerFirstTask(final TriggerFirstTaskCommand triggerFirstTaskCommand) {
    LOGGER.info().log("Triggering first task for workflow. {}", triggerFirstTaskCommand);
    final var constraintViolations = validator.validate(triggerFirstTaskCommand);
    if (!constraintViolations.isEmpty()) {
      return Mono.error(
          WorkflowExceptionFactory.newInvalidArgumentException(
              triggerFirstTaskCommand, constraintViolations));
    }

    return transactionalOperator.transactional(
        Mono.just(triggerFirstTaskCommand.getWorkflowId())
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
                        .log(
                            "First Task Triggered {}. ({})", workflowVO, triggerFirstTaskCommand)));
  }

  @Transactional
  public Mono<WorkflowVO> finishTaskAndAdvance(final FinishTaskCommand finishTaskCommand) {
    LOGGER.info().log("Finishing task for workflow. {}", finishTaskCommand);
    final var constraintViolations = validator.validate(finishTaskCommand);
    if (!constraintViolations.isEmpty()) {
      return Mono.error(
          WorkflowExceptionFactory.newInvalidArgumentException(
              finishTaskCommand, constraintViolations));
    }

    final var finishTaskAndAdvanceMono =
        findByWorkflowId(finishTaskCommand.getWorkflowId())
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
    return transactionalOperator.transactional(finishTaskAndAdvanceMono);
  }

  @Transactional
  public Mono<WorkflowVO> registerTaskActivationResult(
      final RegisterTaskResultsCommand registerTaskResultsCommand) {

    LOGGER.info().log("Registering task results for workflow. {}", registerTaskResultsCommand);
    final var constraintViolations = validator.validate(registerTaskResultsCommand);
    if (!constraintViolations.isEmpty()) {
      return Mono.error(
          WorkflowExceptionFactory.newInvalidArgumentException(
              registerTaskResultsCommand, constraintViolations));
    }

    final var registerTaskResultMono =
        findByWorkflowId(registerTaskResultsCommand.getWorkflowId())
            .switchIfEmpty(
                Mono.error(
                    WorkflowExceptionFactory.newOrcNotFoundException(
                        registerTaskResultsCommand.getWorkflowId())))
            .map(
                workflowRoot ->
                    this.registerDomainTaskActivationResult(
                        workflowRoot, registerTaskResultsCommand))
            .flatMap(this::saveWorkflowRoot)
            .map(WorkflowMapper.INSTANCE::toService)
            .doOnSuccess(
                workflowVO ->
                    LOGGER
                        .info()
                        .log(
                            "Task results registered {}. ({})",
                            workflowVO,
                            registerTaskResultsCommand));
    return transactionalOperator.transactional(registerTaskResultMono);
  }

  private WorkflowRoot registerDomainTaskActivationResult(
      final WorkflowRoot workflowRoot,
      final RegisterTaskResultsCommand registerTaskResultsCommand) {
    final var taskType =
        RegisterTaskResultsCommandMapper.INSTANCE.toModel(registerTaskResultsCommand.getTaskType());

    final var taskCriteriaResults =
        registerTaskResultsCommand.getTaskCriteriaResults().stream()
            .map(RegisterTaskResultsCommandMapper.INSTANCE::toModel)
            .collect(Collectors.toList());

    workflowRoot.registerTaskCriteriaActivationResults(
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
