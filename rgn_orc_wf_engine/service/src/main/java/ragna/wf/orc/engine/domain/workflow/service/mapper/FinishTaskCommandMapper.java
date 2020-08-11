package ragna.wf.orc.engine.domain.workflow.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ragna.wf.orc.engine.domain.workflow.model.PlannedTask;
import ragna.wf.orc.engine.domain.workflow.model.TaskType;
import ragna.wf.orc.engine.domain.workflow.service.vo.FinishTaskCommand;

@Mapper
public interface FinishTaskCommandMapper {
  FinishTaskCommandMapper INSTANCE = Mappers.getMapper(FinishTaskCommandMapper.class);

  TaskType toModel(FinishTaskCommand.TaskType taskType);

  PlannedTask.Result toModel(FinishTaskCommand.Result result);
}
