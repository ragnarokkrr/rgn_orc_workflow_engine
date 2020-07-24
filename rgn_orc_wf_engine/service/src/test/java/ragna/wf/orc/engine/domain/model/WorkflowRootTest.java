package ragna.wf.orc.engine.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WorkflowRootTest {

    @Test
    void whenWorkflowRootIsCreated_thenStatusInstantiated() {
        // given
        final var customerRequest = kyleReese();

        // when
        final var workflowRoot = WorkflowRoot.createWorkflowRoot(customerRequest);

        // then
        assertThat(workflowRoot)
                .isNotNull()
                .hasFieldOrPropertyWithValue("customerRequest", customerRequest)
                .hasFieldOrPropertyWithValue("status", WorkflowStatus.INSTANTIATED)
                .hasFieldOrPropertyWithValue("result", WorkflowResult.WORKFLOW_ONGOING)
                .hasNoNullFieldsOrPropertiesExcept("configuration", "executionPlan");
    }

    @Test
    void whenCreditGrantingWorkflowRootIsConfigured_thenStatusConfigured() {
        // given
        final var customerRequest = kyleReese();

        // when
        final var workflowRoot = WorkflowRoot.createWorkflowRoot(customerRequest)
                .addWorkflowConfiguration(WorkflowConfigurationFixture.sampleConfiguration())
                .createExecutionPlan()
                .configured();

        // then
        assertThat(workflowRoot)
                .isNotNull();
    }

    private CustomerRequest kyleReese() {
        return CustomerRequest.builder()
                .id("1")
                .name("Kyle Reese")
                .requestMemo("Kyle Reese's request memo")
                .build();
    }
}

