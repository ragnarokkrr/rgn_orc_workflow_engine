package ragna.wf.orc.engine.domain.workflow.service;

import ragna.wf.orc.engine.domain.workflow.service.vo.CreateWorkflowCommand;

public class ServiceFixtures {
  private ServiceFixtures() {}

  public static CreateWorkflowCommand kyleReeseCreateWorkflowCommand() {
    return CreateWorkflowCommand.builder()
        .id("1")
        .customerId("1")
        .customerName("Kyle Reese")
        .requestMemo("Kyle Reese's request memo")
        .build();
  }
}
