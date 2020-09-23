package ragna.wf.orc.engine.application.replay;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import ragna.wf.orc.common.serialization.DefaultKryoContext;
import ragna.wf.orc.engine.application.messaging.output.TriggerTaskMessageProducer;
import ragna.wf.orc.engine.application.messaging.output.vo.TriggerTaskDto;
import ragna.wf.orc.engine.domain.tasks.TaskActivationCriteriaService;
import ragna.wf.orc.engine.domain.tasks.vo.CriteriaEvaluationResult;
import ragna.wf.orc.engine.domain.workflow.model.WorkflowModelFixture;
import ragna.wf.orc.engine.domain.workflow.model.WorkflowRoot;
import ragna.wf.orc.engine.domain.workflow.model.events.WorkflowRootTaskTriggered;
import ragna.wf.orc.engine.domain.workflow.service.WorkflowTaskManagementService;
import ragna.wf.orc.engine.domain.workflow.service.vo.WorkflowVO;
import ragna.wf.orc.engine.infrastructure.clients.metadata.ConfiguredTaskCriteriaMockFactory;
import ragna.wf.orc.engine.infrastructure.config.FeatureTogglesConfigProperties;
import ragna.wf.orc.engine.infrastructure.storedevents.replay.main.MainReplayService;
import ragna.wf.orc.engine.infrastructure.storedevents.replay.main.vo.MainReplayContextVo;
import ragna.wf.orc.engine.infrastructure.storedevents.replay.secondary.vo.SecondaryReplayContextVo;
import ragna.wf.orc.eventstore.model.StoredEvent;
import ragna.wf.orc.eventstore.service.EventSerializationDelegate;
import ragna.wf.orc.eventstore.service.EventStoreService;
import ragna.wf.orc.eventstore.service.mappers.StoredEventMapper;
import ragna.wf.utils.ResultCaptor;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static ragna.wf.orc.engine.domain.workflow.model.WorkflowModelFixture.kyleReeseCustomerRequest;

class WorkflowRootTaskTriggeredReplayerTest {
  private MainReplayService mainReplayServiceSpy;
  private WorkflowRootTaskTriggeredReplayer workflowRootTaskTriggeredReplayerSpy;
  private ReplayProcessor<MainReplayContextVo> mainReplayContextVoReplayProcessor;
  private ReplayProcessor<SecondaryReplayContextVo> sideReplayContextVoReplayProcessor;
  private EventSerializationDelegate eventSerializationDelegate;
  private EventStoreService eventStoreServiceMock;
  private WorkflowTaskManagementService workflowTaskManagementServiceMock;
  private TaskActivationCriteriaService taskActivationCriteriaService;
  private TriggerTaskMessageProducer triggerTaskMessageProducerMock;

  @BeforeEach
  public void init() {
    eventSerializationDelegate =
        new EventSerializationDelegate(DefaultKryoContext.kryoContextWithDefaultSerializers());
    mainReplayContextVoReplayProcessor = ReplayProcessor.create();
    // TODO fix mockmaker to enable spy
    sideReplayContextVoReplayProcessor = ReplayProcessor.create();

    eventStoreServiceMock = mock(EventStoreService.class);
    workflowTaskManagementServiceMock = mock(WorkflowTaskManagementService.class);
    taskActivationCriteriaService = mock(TaskActivationCriteriaService.class);
    triggerTaskMessageProducerMock = mock(TriggerTaskMessageProducer.class);

    final var applicationContextMock = mock(ApplicationContext.class);
    final var featureTogglesConfigPropertiesMock = mock(FeatureTogglesConfigProperties.class);

    this.mainReplayServiceSpy =
        spy(
            new MainReplayService(
                eventStoreServiceMock,
                mainReplayContextVoReplayProcessor,
                sideReplayContextVoReplayProcessor,
                applicationContextMock,
                featureTogglesConfigPropertiesMock));

    this.workflowRootTaskTriggeredReplayerSpy =
        spy(
            new WorkflowRootTaskTriggeredReplayer(
                taskActivationCriteriaService,
                workflowTaskManagementServiceMock,
                triggerTaskMessageProducerMock));

    when(featureTogglesConfigPropertiesMock.isReplayEngineEnabled()).thenReturn(true);

    doReturn(this.workflowRootTaskTriggeredReplayerSpy)
        .when(applicationContextMock)
        .getBean(WorkflowRootTaskTriggeredReplayer.class);
    System.out.println();
  }

  @Test
  void whenWorkflowRootCreatedReceived_shouldProcessEventWithHandler() throws InterruptedException {
    // given
    final var workflowRoot =
        WorkflowRoot.createWorkflowRoot(kyleReeseCustomerRequest())
            .addWorkflowConfiguration(WorkflowModelFixture.sampleTwoTasksConfiguration())
            .createExecutionPlan()
            .configured()
            .triggerFirstTask();

    final var workflowRootTaskTriggered =
        (WorkflowRootTaskTriggered) workflowRoot.aggregateDomainEvents().get(1).unwrap();

    final var storedEvent =
        StoredEvent.createStoredEvent(
            workflowRoot.getId(),
            workflowRootTaskTriggered.getClass().getName(),
            eventSerializationDelegate.serializeEvent(workflowRootTaskTriggered),
            LocalDateTime.now(),
            eventSerializationDelegate.getSerializationEngine());

    final var storedEventVo = StoredEventMapper.INSTANCE.toService(storedEvent);
    storedEventVo.setDomainEvent(workflowRootTaskTriggered);
    // mock config

    doReturn(Mono.just(criteriaEvaluationResultFixture()))
        .when(taskActivationCriteriaService)
        .matchTaskCriteria(any());

    doReturn(Mono.just(storedEventVo)).when(eventStoreServiceMock).append(any());
    doReturn(Mono.just(storedEventVo.published())).when(eventStoreServiceMock).updateStatus(any());
    doReturn(Mono.just(workflowFirstRegisterTaskResult(workflowRoot.getId())))
        .when(workflowTaskManagementServiceMock)
        .registerTaskActivationResult(any());
    doReturn(TriggerTaskDto.builder().build()).when(triggerTaskMessageProducerMock).send(any());

    // result will have always the last state in MainReplayer pipe once
    // same context will flow across entire replay processor, i. e., we just need one reference
    final var mainReplayContextVoResultCaptorPublish =
        new ResultCaptor<Mono<MainReplayContextVo>>();
    doAnswer(mainReplayContextVoResultCaptorPublish)
        .when(workflowRootTaskTriggeredReplayerSpy)
        .publish(any());

    mainReplayServiceSpy.init();
    // when
    mainReplayContextVoReplayProcessor.onNext(MainReplayContextVo.createContext(storedEventVo));

    TimeUnit.MILLISECONDS.sleep(100);

    // then

    verify(mainReplayServiceSpy, times(1)).init();
    verify(workflowRootTaskTriggeredReplayerSpy, times(1)).doReplay(any());
    verify(workflowRootTaskTriggeredReplayerSpy, times(1)).activateTaskIfConfigured(any());
    verify(workflowRootTaskTriggeredReplayerSpy, times(1)).doReplay(any());
    verify(workflowRootTaskTriggeredReplayerSpy, times(1)).publish(any());
    verify(workflowRootTaskTriggeredReplayerSpy, times(1)).saveMatchTaskCriteriaResult(any());
    verify(workflowRootTaskTriggeredReplayerSpy, times(1)).mapMatchResult(any());
    verify(workflowRootTaskTriggeredReplayerSpy, times(1)).assessTaskActivation(any(), any());
    verify(triggerTaskMessageProducerMock, times(1)).send(any());

    final var contextVoResultCaptorResult =
            mainReplayContextVoResultCaptorPublish.getResult().block();
    assertThat(contextVoResultCaptorResult)
            .isNotNull()
            .hasFieldOrPropertyWithValue(
                    "matchResult",
                    MainReplayContextVo.MatchResult.builder()
                            .matchResultType(MainReplayContextVo.MatchResultEnum.MATCHED)
                            .build())
            .hasFieldOrPropertyWithValue(
                    "replayResult",
                    MainReplayContextVo.ReplayResult.builder()
                            .replayResultType(MainReplayContextVo.ReplayResultEnum.PUBLISHED)
                            .build())
            .hasNoNullFieldsOrProperties();
    assertThat(contextVoResultCaptorResult.getCriteriaEvaluationResult()).isPresent();
    assertThat(contextVoResultCaptorResult.getMainStoredEventReplayerCallback()).isPresent();
    assertThat(contextVoResultCaptorResult.getMainStoredEventReplayerCallback().get())
            .isInstanceOf(WorkflowRootTaskTriggeredReplayer.class);

  }

  private CriteriaEvaluationResult criteriaEvaluationResultFixture() {
    return CriteriaEvaluationResult.builder()
        .customerId(kyleReeseCustomerRequest().getCustomerId())
        .criteriaResultList(
            List.of(
                CriteriaEvaluationResult.CriterionResult.builder()
                    .resultType(
                        CriteriaEvaluationResult.CriterionResult.CriterionResultType.MATCHED)
                    .id(ConfiguredTaskCriteriaMockFactory.TASK_CRITERIA_ASC.get().getId())
                    .name(ConfiguredTaskCriteriaMockFactory.TASK_CRITERIA_ASC.get().getName())
                    .value("8.5")
                    .build(),
                CriteriaEvaluationResult.CriterionResult.builder()
                    .resultType(
                        CriteriaEvaluationResult.CriterionResult.CriterionResultType.MATCHED)
                    .id(ConfiguredTaskCriteriaMockFactory.TASK_CRITERIA_DESC.get().getId())
                    .name(ConfiguredTaskCriteriaMockFactory.TASK_CRITERIA_DESC.get().getName())
                    .value("2")
                    .build()))
        .build();
  }

  private WorkflowVO workflowFirstRegisterTaskResult(String workflowId) {
    final var workflowVO = new WorkflowVO();
    workflowVO.setId(workflowId);
    workflowVO.setConfigurationId(WorkflowModelFixture.sampleTwoTasksConfiguration().getId());
    workflowVO.setCreatedOn(LocalDateTime.now());
    workflowVO.setCustomerId(kyleReeseCustomerRequest().getCustomerId());
    workflowVO.setCustomerRequestId(kyleReeseCustomerRequest().getId());
    workflowVO.setResult(WorkflowVO.Result.WORKFLOW_ONGOING);
    workflowVO.setStatus(WorkflowVO.Status.ORCHESTRATING);
    return workflowVO;
  }
}
