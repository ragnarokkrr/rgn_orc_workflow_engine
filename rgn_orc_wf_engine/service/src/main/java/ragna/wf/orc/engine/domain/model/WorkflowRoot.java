package ragna.wf.orc.engine.domain.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.fissore.slf4j.FluentLogger;
import org.fissore.slf4j.FluentLoggerFactory;
import org.springframework.util.Assert;
import ragna.wf.orc.common.events.DomainEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Data
@Setter(AccessLevel.NONE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class WorkflowRoot {
    private static final FluentLogger LOGGER = FluentLoggerFactory.getLogger(WorkflowRoot.class);

    // @Transient
    private final Collection<DomainEvent> domainEvents = new ArrayList<>();

    private String id;
    private CustomerRequest customerRequest;
    private Configuration configuration;
    private ExecutionPlan executionPlan;
    private WorkflowStatus status;
    private WorkflowResult result;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;

    public static WorkflowRoot createWorkflowRoot(final CustomerRequest customerRequest) {
        Assert.notNull(customerRequest, "customerRequest must not be null");
        final var workflowRoot = new WorkflowRoot();
        workflowRoot.customerRequest = customerRequest.toBuilder().build();
        workflowRoot.id = UUID.randomUUID().toString();
        workflowRoot.status = WorkflowStatus.INSTANTIATED;
        workflowRoot.result = WorkflowResult.WORKFLOW_ONGOING;
        workflowRoot.createdOn = LocalDateTime.now();
        workflowRoot.updatedOn = LocalDateTime.now();
        return workflowRoot;
    }

    public WorkflowRoot addWorkflowConfiguration(final Configuration configuration) {
        Assert.notNull(configuration, "workflow configuration must not be null");
        this.configuration = configuration.toBuilder().build();
        return this;
    }

    public WorkflowRoot createExecutionPlan() {
        Assert.state(this.configuration != null, String.format("%s: Cannot create plan without configuration",
                this.customerRequest));
        Assert.state(this.status == WorkflowStatus.INSTANTIATED, String.format("%s: cannot create execution plan: %s",
                this.customerRequest, this.status));

        final var plannedTaskList = configuration
                .getConfiguredTasks()
                .stream()
                .map(configuredTask ->
                        PlannedTask.builder()
                                .order(configuredTask.getOrder())
                                .taskType(configuredTask.getTaskType())
                                .result(PlannedTask.Result.WAITING_FOR_RESULT)
                                .status(PlannedTask.Status.WAITING_FOR_RESULT)
                                .build()
                )
                .collect(Collectors.toList());

        this.executionPlan = ExecutionPlan.builder()
                .plannedTasks(plannedTaskList)
                .createdOn(LocalDateTime.now())
                .build();
        return this;
    }

    public WorkflowRoot configured() {
        Assert.state(this.configuration != null, String.format("%s: Cannot create plan without configuration",
                this.customerRequest));
        Assert.state(this.status == WorkflowStatus.INSTANTIATED, String.format("%s: cannot create execution plan: %s",
                this.customerRequest, this.status));
        Assert.state(this.executionPlan != null, String.format("%s: executionPlan must not be null",
                this.customerRequest));

        this.status = WorkflowStatus.CONFIGURED;
        this.updatedOn = LocalDateTime.now();
        return this;
    }

    // TODO @AfterDomainEventPublication / Spring Data
    public void clearDomainEventsCallback() {
    }

    // TODO @DomainEvents / Spring Data
    public Collection<DomainEvent> events() {
        return List.copyOf(domainEvents);
    }

}
