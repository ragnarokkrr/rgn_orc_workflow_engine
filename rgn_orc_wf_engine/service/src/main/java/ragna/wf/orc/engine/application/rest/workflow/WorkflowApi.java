package ragna.wf.orc.engine.application.rest.workflow;

import java.net.URI;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.fissore.slf4j.FluentLogger;
import org.fissore.slf4j.FluentLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ragna.wf.orc.engine.application.rest.workflow.dto.CreateWorkflowCommand;
import ragna.wf.orc.engine.application.rest.workflow.dto.FinishTaskCommand;
import ragna.wf.orc.engine.application.rest.workflow.dto.RegisterTaskResultsCommand;
import ragna.wf.orc.engine.application.rest.workflow.dto.TriggerFirstTaskCommand;
import ragna.wf.orc.engine.application.rest.workflow.dto.WorkflowDTO;
import ragna.wf.orc.engine.application.rest.workflow.mapper.RestMapper;
import ragna.wf.orc.engine.domain.workflow.service.WorkflowCreationService;
import ragna.wf.orc.engine.domain.workflow.service.WorkflowQueryService;
import ragna.wf.orc.engine.domain.workflow.service.WorkflowTaskManagementService;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/workflow")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class WorkflowApi {
  private static final FluentLogger LOGGER = FluentLoggerFactory.getLogger(WorkflowApi.class);
  private final WorkflowTaskManagementService workflowTaskManagementService;
  private final WorkflowCreationService workflowCreationService;
  private final WorkflowQueryService workflowQueryService;

  @PostMapping
  public Mono<ResponseEntity<WorkflowDTO>> createWorkflow(
      @Valid @RequestBody final CreateWorkflowCommand createWorkflowCommand) {
    return Mono.just(createWorkflowCommand)
        .map(RestMapper.INSTANCE::toService)
        .flatMap(workflowCreationService::createWorkflow)
        .map(RestMapper.INSTANCE::toRest)
        .map(
            workflowDTO ->
                ResponseEntity.created(
                        URI.create(String.format("/workflow/%s", workflowDTO.getId())))
                    .body(workflowDTO));
  }

  @PostMapping("{workflowId}:triggerFirstTask")
  public Mono<ResponseEntity<WorkflowDTO>> triggerFirstTask(
      @NotEmpty(message = "workflowId must not Empty") @PathVariable("workflowId")
          final String workflowId,
      @Valid @RequestBody final TriggerFirstTaskCommand triggerFirstTaskCommand) {
    LOGGER.info().log("Triggering first task {}", triggerFirstTaskCommand);
    Assert.isTrue(
        Objects.equals(workflowId, triggerFirstTaskCommand.getWorkflowId()),
        String.format(
            "pathvariable(rowkflowId=%s) and requestBody(%s) must be the same",
            workflowId, triggerFirstTaskCommand));
    return Mono.just(RestMapper.INSTANCE.toService(workflowId))
        .flatMap(workflowTaskManagementService::triggerFirstTask)
        .map(RestMapper.INSTANCE::toRest)
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @PostMapping("{workflowId}:finishTaskAndAdvance")
  public Mono<ResponseEntity<WorkflowDTO>> finishTaskAndAdvance(
      @NotEmpty(message = "workflowId must not Empty") @PathVariable("workflowId")
          final String workflowId,
      @Valid @RequestBody FinishTaskCommand finishTaskCommand) {
    return Mono.just(RestMapper.INSTANCE.toService(workflowId, finishTaskCommand))
        .flatMap(workflowTaskManagementService::finishTaskAndAdvance)
        .map(RestMapper.INSTANCE::toRest)
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @PostMapping("{workflowId}:registerTaskResult")
  public Mono<ResponseEntity<WorkflowDTO>> registerTaskResult(
      @NotEmpty(message = "workflowId must not Empty") @PathVariable("workflowId")
          final String workflowId,
      @Valid @RequestBody RegisterTaskResultsCommand registerTaskResultsCommand) {
    return Mono.just(RestMapper.INSTANCE.toService(workflowId, registerTaskResultsCommand))
        .flatMap(workflowTaskManagementService::registerTaskActivationResult)
        .map(RestMapper.INSTANCE::toRest)
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @GetMapping("{workflowId}")
  public Mono<ResponseEntity<WorkflowDTO>> findById(
      @NotEmpty(message = "workflowId must not Empty") @PathVariable("workflowId")
          final String workflowId) {
    return workflowQueryService
        .findById(workflowId)
        .map(RestMapper.INSTANCE::toRest)
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @GetMapping(":findByCustomerRequest")
  public Mono<ResponseEntity<WorkflowDTO>> findByCustomerRequestId(
      @NotEmpty(message = "customerRequestId must not Empty") @RequestParam("customerRequestId")
          final String customerRequestId) {
    return workflowQueryService
        .findByCustomerRequestId(customerRequestId)
        .map(RestMapper.INSTANCE::toRest)
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity.notFound().build());
  }
}
