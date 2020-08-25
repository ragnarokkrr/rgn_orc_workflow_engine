package ragna.wf.orc.engine.domain.tasks.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ragna.wf.orc.engine.domain.tasks.vo.CriteriaEvaluationQuery;
import ragna.wf.orc.engine.domain.workflow.model.ConfiguredTask;

@Mapper
public interface CriterionMapper {
    CriterionMapper INSTANCE = Mappers.getMapper(CriterionMapper.class);

    CriteriaEvaluationQuery.Criterion mapToService(ConfiguredTask.TaskCriterion taskCriterion);
}
