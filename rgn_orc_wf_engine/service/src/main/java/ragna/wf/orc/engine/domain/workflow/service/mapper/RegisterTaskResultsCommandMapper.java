package ragna.wf.orc.engine.domain.workflow.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ragna.wf.orc.engine.domain.workflow.model.TaskCriteriaEvaluationCommand;
import ragna.wf.orc.engine.domain.workflow.model.TaskType;
import ragna.wf.orc.engine.domain.workflow.service.vo.RegisterTaskResultsCommand;

@Mapper
public interface RegisterTaskResultsCommandMapper {
  RegisterTaskResultsCommandMapper INSTANCE =
          Mappers.getMapper(RegisterTaskResultsCommandMapper.class);

  TaskType toModel(RegisterTaskResultsCommand.TaskType taskType);

  TaskCriteriaEvaluationCommand toModel(RegisterTaskResultsCommand.TaskCriteriaResult result);

  RegisterTaskResultsCommand.TaskCriteriaResult toService(
          TaskCriteriaEvaluationCommand taskCriteriaEvaluationCommand);
}
