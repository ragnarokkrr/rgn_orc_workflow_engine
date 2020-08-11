package ragna.wf.orc.engine.application.rest.workflow.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ragna.wf.orc.engine.application.rest.workflow.dto.WorkflowDTO;
import ragna.wf.orc.engine.domain.workflow.service.vo.CreateWorkflowCommand;
import ragna.wf.orc.engine.domain.workflow.service.vo.FinishTaskCommand;
import ragna.wf.orc.engine.domain.workflow.service.vo.RegisterTaskResultsCommand;
import ragna.wf.orc.engine.domain.workflow.service.vo.TriggerFirstTaskCommand;
import ragna.wf.orc.engine.domain.workflow.service.vo.WorkflowVO;

@Mapper
public interface RestMapper {
  RestMapper INSTANCE = Mappers.getMapper(RestMapper.class);

  CreateWorkflowCommand toService(
          ragna.wf.orc.engine.application.rest.workflow.dto.CreateWorkflowCommand
                  createWorkflowCommand);

  FinishTaskCommand toService(
          String workflowId,
          ragna.wf.orc.engine.application.rest.workflow.dto.FinishTaskCommand finishTaskCommand);

  RegisterTaskResultsCommand toService(
          String workflowId,
          ragna.wf.orc.engine.application.rest.workflow.dto.RegisterTaskResultsCommand
                  registerTaskResultsCommand);

  default TriggerFirstTaskCommand toService(String workflowId) {
    return TriggerFirstTaskCommand.builder().workflowId(workflowId).build();
  }

  WorkflowVO toService(WorkflowDTO workflowDTO);

  WorkflowDTO toRest(WorkflowVO workflowVO);
}
