package ragna.wf.orc.engine.domain.tasks;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.fissore.slf4j.FluentLogger;
import org.fissore.slf4j.FluentLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ragna.wf.orc.engine.domain.tasks.vo.CriteriaEvaluationQuery;
import ragna.wf.orc.engine.domain.tasks.vo.CriteriaEvaluationResult;
import ragna.wf.orc.engine.domain.tasks.vo.Criterion1Query;
import ragna.wf.orc.engine.domain.tasks.vo.Criterion1ResponseVo;
import ragna.wf.orc.engine.domain.tasks.vo.Criterion2Query;
import ragna.wf.orc.engine.domain.tasks.vo.Criterion2ResponseVo;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TaskActivationCriteriaService {
  private static final FluentLogger LOGGER =
          FluentLoggerFactory.getLogger(TaskActivationCriteriaService.class);

  private final TaskActivationCriterion1Service taskActivationCriterion1Service;
  private final TaskActivationCriterion2Service taskActivationCriterion2Service;

  public Mono<CriteriaEvaluationResult> matchTaskCriteria(
          final CriteriaEvaluationQuery criteriaEvaluationQuery) {
    LOGGER.info().log("Matching task criteria: {}", criteriaEvaluationQuery);
    final var criterionResultCallMonoList = criteriaEvaluationQuery.getCriteriaList().stream()
            .map(criterion ->
                    switch (CriteriaServices.fromStringOrDefault(criterion.getId())) {
                      case CRITERION_01 -> callCriterion1ServiceMono(criterion);
                      case CRITERION_02 -> callCriterion2ServiceMono(criterion);
                      default -> invalidCriterion(criterion);
                    })
            .map(criterionMonoFunction -> criterionMonoFunction.apply(criteriaEvaluationQuery.getCustomerId()))
            .collect(Collectors.toList());

    final var criterionResultArrayMono = Mono.zip(criterionResultCallMonoList,
            objects -> Arrays.stream(objects)
                    .map(object -> (CriteriaEvaluationResult.CriterionResult) object)
                    .collect(Collectors.toList()));
    return criterionResultArrayMono
            .map(criteriaEvaluationResults -> CriteriaEvaluationResult.builder()
                    .customerId(criteriaEvaluationQuery.getCustomerId())
                    .addAllCriteriaResult(criteriaEvaluationResults)
                    .build());
  }

  final Function<Long, Mono<CriteriaEvaluationResult.CriterionResult>> callCriterion1ServiceMono(final CriteriaEvaluationQuery.Criterion criterion) {
    return (customerId) ->
            taskActivationCriterion1Service.findByCustomerId(
                    Criterion1Query.builder().customerId(customerId).build())
                    .map(Criterion1ResponseVo::getValue)
                    .map(value -> this.evaluateCriterion(value, criterion))
                    .onErrorResume(throwable -> Mono.just(newCriterionResultError(criterion)));
  }

  final Function<Long, Mono<CriteriaEvaluationResult.CriterionResult>> callCriterion2ServiceMono(final CriteriaEvaluationQuery.Criterion criterion) {
    return (customerId) -> taskActivationCriterion2Service.findByCustomerId(
            Criterion2Query.builder().customerId(customerId).build())
            .map(Criterion2ResponseVo::getValue)
            .map(value -> this.evaluateCriterion(value, criterion))
            .onErrorResume(throwable -> Mono.just(newCriterionResultError(criterion)));
  }

  private CriteriaEvaluationResult.CriterionResult evaluateCriterion(final Long value, final CriteriaEvaluationQuery.Criterion criterion) {
    final var matched = switch (criterion.getOrder()) {
      case ASC -> value >= criterion.getAcceptedValue() && value <= criterion.getUpperBound();
      case DESC -> value >= criterion.getLowerBound() && value <= criterion.getAcceptedValue();
    };

    final var resultType = matched
            ? CriteriaEvaluationResult.CriterionResult.CriterionResultType.MATCHED
            : CriteriaEvaluationResult.CriterionResult.CriterionResultType.UNMATCHED;

    return CriteriaEvaluationResult.CriterionResult.builder()
            .id(criterion.getId())
            .name(criterion.getName())
            .value(String.valueOf(value))
            .resultType(resultType)
            .build();
  }

  final Function<Long, Mono<CriteriaEvaluationResult.CriterionResult>> invalidCriterion(final CriteriaEvaluationQuery.Criterion criterion) {
    return (customerId) -> Mono.just(CriteriaEvaluationResult.CriterionResult.builder()
            .id(criterion.getId())
            .name(criterion.getName())
            .value(StringUtils.EMPTY)
            .resultType(CriteriaEvaluationResult.CriterionResult.CriterionResultType.INVALID_CRITERION)
            .build());
  }

  private CriteriaEvaluationResult.CriterionResult newCriterionResultError(CriteriaEvaluationQuery.Criterion criterion) {
    return CriteriaEvaluationResult.CriterionResult.builder()
            .id(criterion.getId())
            .name(criterion.getName())
            .value(StringUtils.EMPTY)
            .resultType(CriteriaEvaluationResult.CriterionResult.CriterionResultType.ERROR)
            .build();
  }

}
