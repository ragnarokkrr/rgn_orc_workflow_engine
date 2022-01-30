package ragna.wf.orc.engine.application.messaging.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import ragna.wf.orc.engine.domain.workflow.service.vo.CreateWorkflowCommand;

@Mapper
public interface CreateWorkflowMapper {
  CreateWorkflowMapper INSTANCE = Mappers.getMapper(CreateWorkflowMapper.class);

  CreateWorkflowCommand toService(
      ragna.wf.orc.engine.application.messaging.consumer.vo.CreateWorkflowCommand
          createWorkflowCommand);
}
