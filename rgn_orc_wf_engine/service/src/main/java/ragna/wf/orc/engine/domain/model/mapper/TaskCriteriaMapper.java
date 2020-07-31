package ragna.wf.orc.engine.domain.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ragna.wf.orc.engine.domain.model.PlannedTask;
import ragna.wf.orc.engine.domain.model.TaskCriteriaEvaluationCommand;

@Mapper
public interface TaskCriteriaMapper {
    TaskCriteriaMapper INSTANCE = Mappers.getMapper(TaskCriteriaMapper.class);

    PlannedTask.TaskCriteriaResult toModel(TaskCriteriaEvaluationCommand taskCriteriaEvaluationCommand);
}
