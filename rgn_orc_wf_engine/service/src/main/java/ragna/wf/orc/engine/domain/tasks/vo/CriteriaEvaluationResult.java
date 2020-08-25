package ragna.wf.orc.engine.domain.tasks.vo;

import lombok.Builder;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Data
@Builder
public class CriteriaEvaluationResult {
  private List<CriterionResult> criteriaResultList;
  private Long customerId;

  public CriteriaResultType getCriteriaResultType() {
    if (CollectionUtils.isEmpty(criteriaResultList)) {
      return CriteriaResultType.UNMATCHED;
    }

    if (criteriaResultList.stream()
            .map(CriterionResult::getResultType)
            .anyMatch(resultType -> isError().test(resultType))) {
      return CriteriaResultType.ERROR;
    }

    if (criteriaResultList.stream()
            .map(CriterionResult::getResultType)
            .anyMatch(resultType -> isInvalidCriterion().test(resultType))) {
      return CriteriaResultType.INVALID_CRITERION;
    }

    if (criteriaResultList.stream()
            .allMatch(criteriaResult -> isMatched().test(criteriaResult.resultType))) {
      return CriteriaResultType.MATCHED;
    }

    return CriteriaResultType.UNMATCHED;
  }

  private Predicate<CriterionResult.CriterionResultType> isMatched() {
    return (resultType) -> resultType == CriterionResult.CriterionResultType.MATCHED;
  }

  private Predicate<CriterionResult.CriterionResultType> isError() {
    return (resultType) -> resultType == CriterionResult.CriterionResultType.ERROR;
  }

  private Predicate<CriterionResult.CriterionResultType> isInvalidCriterion() {
    return (resultType) -> resultType == CriterionResult.CriterionResultType.INVALID_CRITERION;
  }

  public enum CriteriaResultType {
    MATCHED,
    UNMATCHED,
    ERROR,
    INVALID_CRITERION
  }

  @Data
  @Builder
  public static class CriterionResult {
    private String id;
    private String name;
    private String value;
    private CriterionResultType resultType;

    public enum CriterionResultType {
      MATCHED,
      UNMATCHED,
      INVALID_CRITERION,
      ERROR
    }
  }

  public static class CriteriaEvaluationResultBuilder {
    private List<CriterionResult> criteriaResultList = new ArrayList<>();

    public CriteriaEvaluationResultBuilder addAllCriteriaResult(
            final List<CriterionResult> criterionResults) {
      this.criteriaResultList.addAll(criterionResults);
      return this;
    }
  }
}
