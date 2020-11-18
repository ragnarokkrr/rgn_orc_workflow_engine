package ragna.wf.orc.engine.domain.tasks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ragna.wf.orc.engine.domain.tasks.mappers.CriterionMapper;
import ragna.wf.orc.engine.domain.tasks.vo.CriteriaEvaluationQuery;
import ragna.wf.orc.engine.domain.tasks.vo.CriteriaEvaluationResult;
import ragna.wf.orc.engine.domain.tasks.vo.Criterion1ResponseVo;
import ragna.wf.orc.engine.domain.tasks.vo.Criterion2ResponseVo;
import ragna.wf.orc.engine.infrastructure.clients.metadata.ConfiguredTaskCriteriaMockFactory;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class TaskActivationCriteriaServiceTest {
  private final TaskActivationCriterion1Service taskActivationCriterion1Service =
      mock(TaskActivationCriterion1Service.class);
  private final TaskActivationCriterion2Service taskActivationCriterion2Service =
      mock(TaskActivationCriterion2Service.class);
  private TaskActivationCriteriaService taskActivationCriteriaService;

  @BeforeEach
  void init() {
    taskActivationCriteriaService =
        new TaskActivationCriteriaService(
            taskActivationCriterion1Service, taskActivationCriterion2Service);

    doReturn(Mono.just(Criterion1ResponseVo.builder().value(5L).build()))
        .when(taskActivationCriterion1Service)
        .findByCustomerId(any());

    doReturn(Mono.just(Criterion2ResponseVo.builder().value(3L).build()))
        .when(taskActivationCriterion2Service)
        .findByCustomerId(any());
  }

  @Test
  void whenEachCriterionIsMatched_thenCriteriaEvaluationResultIsMatched() {
    // given
    final var taskCriteria1 = ConfiguredTaskCriteriaMockFactory.TASK_CRITERIA_ASC.get();
    final var taskCriteria2 = ConfiguredTaskCriteriaMockFactory.TASK_CRITERIA_DESC.get();
    final var criteriaEvaluationQuery =
        CriteriaEvaluationQuery.builder()
            .customerId("1")
            .addCriterion(CriterionMapper.INSTANCE.mapToService(taskCriteria1))
            .addCriterion(CriterionMapper.INSTANCE.mapToService(taskCriteria2))
            .build();

    // when
    final var matchTaskCriteriaMono =
        taskActivationCriteriaService.matchTaskCriteria(criteriaEvaluationQuery);

    // then
    StepVerifier.create(matchTaskCriteriaMono)
        .expectNextMatches(
            criteriaEvaluationResult -> {
              assertThat(criteriaEvaluationResult).isNotNull();
              assertThat(criteriaEvaluationResult)
                  .hasFieldOrPropertyWithValue("customerId", "1")
                  .hasFieldOrPropertyWithValue(
                      "criteriaResultType", CriteriaEvaluationResult.CriteriaResultType.MATCHED);

              assertThat(criteriaEvaluationResult.getCriteriaResultList()).isNotEmpty().hasSize(2);

              assertThat(criteriaEvaluationResult.getCriteriaResultList())
                  .flatExtracting(CriteriaEvaluationResult.CriterionResult::getId)
                  .containsExactly("crit01", "crit02");

              assertThat(criteriaEvaluationResult.getCriteriaResultList())
                  .flatExtracting(CriteriaEvaluationResult.CriterionResult::getName)
                  .containsExactly("Criteria 01 - ASC", "Criteria 02 - DESC");

              assertThat(criteriaEvaluationResult.getCriteriaResultList())
                  .flatExtracting(CriteriaEvaluationResult.CriterionResult::getValue)
                  .containsExactly("5", "3");

              assertThat(criteriaEvaluationResult.getCriteriaResultList())
                  .flatExtracting(CriteriaEvaluationResult.CriterionResult::getResultType)
                  .containsOnly(
                      CriteriaEvaluationResult.CriterionResult.CriterionResultType.MATCHED);

              return true;
            })
        .verifyComplete();
  }
}
