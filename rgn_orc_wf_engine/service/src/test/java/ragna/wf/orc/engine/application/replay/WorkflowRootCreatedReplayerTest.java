package ragna.wf.orc.engine.application.replay;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;
import static ragna.wf.orc.engine.domain.workflow.model.WorkflowModelFixture.kyleReeseCustomerRequest;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import ragna.wf.orc.common.serialization.DefaultKryoContext;
import ragna.wf.orc.engine.domain.workflow.model.WorkflowModelFixture;
import ragna.wf.orc.engine.domain.workflow.model.WorkflowRoot;
import ragna.wf.orc.engine.domain.workflow.model.events.WorkflowRootCreated;
import ragna.wf.orc.engine.domain.workflow.service.WorkflowTaskManagementService;
import ragna.wf.orc.engine.domain.workflow.service.vo.WorkflowVO;
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

class WorkflowRootCreatedReplayerTest {
  private MainReplayService mainReplayServiceSpy;
  private WorkflowRootCreatedReplayer workflowRootCreatedReplayerSpy;
  private ReplayProcessor<MainReplayContextVo> mainReplayContextVoReplayProcessor;
  private ReplayProcessor<SecondaryReplayContextVo> sideReplayContextVoReplayProcessor;
  private EventSerializationDelegate eventSerializationDelegate;
  private EventStoreService eventStoreServiceMock;
  private WorkflowTaskManagementService workflowTaskManagementServiceMock;

  @BeforeEach
  public void init() {
    eventSerializationDelegate =
        new EventSerializationDelegate(DefaultKryoContext.kryoContextWithDefaultSerializers());
    mainReplayContextVoReplayProcessor = ReplayProcessor.create();
    // TODO fix mockmaker to enable spy
    sideReplayContextVoReplayProcessor = ReplayProcessor.create();

    eventStoreServiceMock = mock(EventStoreService.class);
    workflowTaskManagementServiceMock = mock(WorkflowTaskManagementService.class);

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

    this.workflowRootCreatedReplayerSpy =
        spy(new WorkflowRootCreatedReplayer(workflowTaskManagementServiceMock));

    when(featureTogglesConfigPropertiesMock.isReplayEngineEnabled()).thenReturn(true);

    doReturn(this.workflowRootCreatedReplayerSpy)
        .when(applicationContextMock)
        .getBean(WorkflowRootCreatedReplayer.class);
  }

  @Test
  void whenWorkflowRootCreatedReceived_shouldProcessEventWithHandler() throws InterruptedException {
    // given
    final var workflowRoot =
        WorkflowRoot.createWorkflowRoot(kyleReeseCustomerRequest())
            .addWorkflowConfiguration(WorkflowModelFixture.sampleTwoTasksConfiguration())
            .createExecutionPlan()
            .configured();

    final var workflowRootCreated =
        (WorkflowRootCreated) workflowRoot.aggregateDomainEvents().iterator().next().unwrap();

    final var storedEvent =
        StoredEvent.createStoredEvent(
            workflowRoot.getId(),
            workflowRootCreated.getClass().getName(),
            eventSerializationDelegate.serializeEvent(workflowRootCreated),
            LocalDateTime.now(),
            eventSerializationDelegate.getSerializationEngine());

    final var storedEventVo = StoredEventMapper.INSTANCE.toService(storedEvent);
    storedEventVo.setDomainEvent(workflowRootCreated);

    doReturn(Mono.just(storedEventVo)).when(eventStoreServiceMock).append(any());
    doReturn(Mono.just(storedEventVo.processed())).when(eventStoreServiceMock).updateStatus(any());
    doReturn(Mono.just(workflowFirstTaskTriggeredFixture(workflowRoot.getId())))
        .when(workflowTaskManagementServiceMock)
        .triggerFirstTask(any());

    final var mainReplayContextVoResultCaptor = new ResultCaptor<Mono<MainReplayContextVo>>();
    doAnswer(mainReplayContextVoResultCaptor).when(workflowRootCreatedReplayerSpy).doReplay(any());

    mainReplayServiceSpy.init();

    // when
    mainReplayContextVoReplayProcessor.onNext(MainReplayContextVo.createContext(storedEventVo));

    TimeUnit.MILLISECONDS.sleep(100);

    // then
    verify(workflowRootCreatedReplayerSpy, times(1)).doReplay(any());
    verify(mainReplayServiceSpy, times(1)).init();
    // TODO fix mockmaker to enable spy
    // verify(sideReplayContextVoReplayProcessor, atLeast(1)).onNext(any());

    final var contextVoResultCaptorResult = mainReplayContextVoResultCaptor.getResult().block();
    assertThat(contextVoResultCaptorResult)
        .isNotNull()
        .hasFieldOrPropertyWithValue(
            "matchResult",
            MainReplayContextVo.MatchResult.builder()
                .matchResultType(MainReplayContextVo.MatchResultEnum.DEFAULT)
                .build())
        .hasFieldOrPropertyWithValue(
            "replayResult",
            MainReplayContextVo.ReplayResult.builder()
                .replayResultType(MainReplayContextVo.ReplayResultEnum.PROCESSED)
                .build())
        .hasNoNullFieldsOrProperties();
    assertThat(contextVoResultCaptorResult.getCriteriaEvaluationResult()).isEmpty();
    assertThat(contextVoResultCaptorResult.getMainStoredEventReplayerCallback()).isPresent();
    assertThat(contextVoResultCaptorResult.getMainStoredEventReplayerCallback().get())
        .isInstanceOf(WorkflowRootCreatedReplayer.class);
  }

  private WorkflowVO workflowFirstTaskTriggeredFixture(String workflowId) {
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
