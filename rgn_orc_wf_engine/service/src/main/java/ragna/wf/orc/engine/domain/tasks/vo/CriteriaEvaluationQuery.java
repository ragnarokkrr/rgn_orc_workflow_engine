package ragna.wf.orc.engine.domain.tasks.vo;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CriteriaEvaluationQuery {
  private List<Criterion> criteriaList;
  private String customerId;

  @Data
  @Builder
  public static class Criterion {
    private String id;
    private String name;
    private Long lowerBound;
    private Long upperBound;
    private Long acceptedValue;
    private Order order;

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
