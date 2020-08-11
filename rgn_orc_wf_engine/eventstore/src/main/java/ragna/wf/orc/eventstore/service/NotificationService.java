package ragna.wf.orc.eventstore.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.fissore.slf4j.FluentLogger;
import org.fissore.slf4j.FluentLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ragna.wf.orc.common.exceptions.eventstore.SerializationException;
import ragna.wf.orc.eventstore.model.StoredEvent;
import ragna.wf.orc.eventstore.repository.StoredEventRepository;
import ragna.wf.orc.eventstore.service.mappers.NotificationLogVoMapper;
import ragna.wf.orc.eventstore.service.vo.NotificationLogVo;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NotificationService {
  private static final FluentLogger LOGGER =
          FluentLoggerFactory.getLogger(NotificationService.class);
  private static final long LOG_NOTIFICATION_COUNT = 5;

  private final StoredEventRepository storedEventRepository;
  private final EventSerializationDelegate eventSerializationDelegate;
  private final ObjectMapper objectMapper;

  public Mono<Long> count() {
    return this.storedEventRepository.count();
  }

  public Mono<NotificationLogVo> currentNotificationLog() {
    return this.calculateCurrentNotificationId()
            .map(NotificationLogVo.NotificationLogId::encode)
            .flatMap(this::findNotificationLog);
  }

  public Mono<NotificationLogVo> findNotificationLog(final String encodedNotificationLogId) {
    final var notificationLogId =
            NotificationLogVo.NotificationLogId.createNotificationLogId(encodedNotificationLogId);

    return this.storedEventRepository
            .findByEventIdBetween(notificationLogId.getLow(), notificationLogId.getHigh())
            .map(this::mapToNotification)
            .collectList()
            .zipWith(
                    this.count(),
                    ((notifications, count) ->
                            this.buildNotificationLog(notificationLogId, notifications, count)));
  }

  public Mono<List<NotificationLogVo.NotificationVo>> findNotificationsByObjectId(
          final String objectId) {
    return storedEventRepository
            .findByObjectIdOrderByIdAsc(objectId)
            .map(this::mapToNotification)
            .collectList();
  }

  private NotificationLogVo.NotificationVo mapToNotification(StoredEvent storedEvent) {
    return NotificationLogVo.NotificationVo.builder()
            .id(storedEvent.getId())
            .objectId(storedEvent.getObjectId())
            .status(NotificationLogVoMapper.INSTANCE.toService(storedEvent.getEventStatus()))
            .serializationEngine(
                    NotificationLogVoMapper.INSTANCE.toService(storedEvent.getSerializationEngine()))
            .occurredOn(storedEvent.getOccurredOn())
            .processingOn(storedEvent.getProcessingOn())
            .processedOn(storedEvent.getProcessedOn())
            .typedName(storedEvent.getTypedName())
            .payload(this.payloadToJsonString(storedEvent))
            .build();
  }

  private NotificationLogVo buildNotificationLog(
          final NotificationLogVo.NotificationLogId notificationLogId,
          final List<NotificationLogVo.NotificationVo> notifications,
          final long count) {
    final var archived = notificationLogId.getHigh() < count;

    return NotificationLogVo.builder()
            .notificationId(notificationLogId.encode())
            .notifications(notifications)
            .next(notificationLogId.next(defaultLogNotificationCount()).encode())
            .previous(notificationLogId.previous(defaultLogNotificationCount()).encode())
            .archived(archived)
            .build();
  }

  private long defaultLogNotificationCount() {
    return LOG_NOTIFICATION_COUNT;
  }

  private Mono<NotificationLogVo.NotificationLogId> calculateCurrentNotificationId() {

    return count()
            .map(this::calculateNotificationLogId)
            .doOnError(throwable -> LOGGER.error().log("Failed to find current", throwable));
  }

  private NotificationLogVo.NotificationLogId calculateNotificationLogId(Long count) {
    final var remainder = count % defaultLogNotificationCount();
    final var low = count - remainder + 1;
    final var high = low + defaultLogNotificationCount() - 1;

    return NotificationLogVo.NotificationLogId.builder().low(low).high(high).build();
  }

  private String payloadToJsonString(final StoredEvent storedEvent) {

    final var object =
            this.eventSerializationDelegate.deserialize(
                    storedEvent.getTypedName(), storedEvent.getPayload());

    return Try.of(() -> this.objectMapper.writeValueAsString(object))
            .getOrElseThrow(
                    t ->
                            new SerializationException(
                                    String.format("Cannot convert to JsonString: %s", storedEvent.getTypedName()),
                                    t));
  }
}
