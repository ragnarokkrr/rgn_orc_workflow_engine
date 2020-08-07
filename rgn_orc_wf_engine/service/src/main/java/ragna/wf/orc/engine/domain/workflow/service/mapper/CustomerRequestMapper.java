package ragna.wf.orc.engine.domain.workflow.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ragna.wf.orc.engine.domain.workflow.model.CustomerRequest;
import ragna.wf.orc.engine.domain.workflow.service.vo.CreateWorkflowCommand;

@Mapper
public interface CustomerRequestMapper {
  CustomerRequestMapper INSTANCE = Mappers.getMapper(CustomerRequestMapper.class);

  CustomerRequest toModel(CreateWorkflowCommand createWorkflowCommand);
}
