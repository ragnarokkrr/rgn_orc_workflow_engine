package ragna.wf.orc.engine.domain.workflow.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ragna.wf.orc.engine.domain.workflow.model.WorkflowRoot;
import ragna.wf.orc.engine.domain.workflow.service.vo.WorkflowVO;

@Mapper
public interface WorkflowMapper {
  WorkflowMapper INSTANCE = Mappers.getMapper(WorkflowMapper.class);

  @Mapping(source = "configuration.id", target = "configurationId")
  @Mapping(source = "customerRequest.customerId", target = "customerId")
  @Mapping(source = "customerRequest.id", target = "customerRequestId")
  WorkflowVO toService(WorkflowRoot workflowRoot);
}
