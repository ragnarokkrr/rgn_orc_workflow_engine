package ragna.wf.orc.engine.domain.tasks.vo;

import lombok.Builder;
import lombok.Data;
import ragna.wf.orc.engine.domain.workflow.model.ConfiguredTask;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class CriteriaEvaluationQuery {
  private List<Criterion> criteriaList;
  private Long customerId;

  @Data
  @Builder
  public static class Criterion {
    private String id;
    private String name;
    private Long lowerBound;
    private Long upperBound;
    private Long acceptedValue;
    private ConfiguredTask.TaskCriterion.Order order;

    public enum Order {
      ASC,
      DESC
    }
  }

  public static class CriteriaEvaluationQueryBuilder {
    private List<Criterion> criteriaList = new ArrayList<>();

    public CriteriaEvaluationQueryBuilder addCriterion(final Criterion criterion) {
      this.criteriaList.add(criterion);
      return this;
    }
  }
}
