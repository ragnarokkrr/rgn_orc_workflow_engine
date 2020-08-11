package ragna.wf.orc.eventstore.rest;

import lombok.RequiredArgsConstructor;
import org.fissore.slf4j.FluentLogger;
import org.fissore.slf4j.FluentLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ragna.wf.orc.eventstore.rest.dto.NotificationLogDto;
import ragna.wf.orc.eventstore.rest.mappers.NotificationLogDtoMapper;
import ragna.wf.orc.eventstore.service.NotificationService;
import ragna.wf.orc.eventstore.service.vo.NotificationLogVo;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/notifications")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NotificationController {
  private static final FluentLogger LOGGER =
          FluentLoggerFactory.getLogger(NotificationController.class);
  private final NotificationService notificationService;

  @GetMapping
  public Mono<ResponseEntity<NotificationLogDto>> getCurrenNotificationLog() {
    return this.notificationService
            .currentNotificationLog()
            .map(NotificationLogDtoMapper.INSTANCE::toRest)
            .doOnNext(
                    notificationLogDto ->
                            LOGGER
                                    .debug()
                                    .log("Notification found: {}", notificationLogDto.getNotificationId()))
            .doOnError(
                    throwable -> LOGGER.error().log("Error finding current NotificationLog.", throwable))
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @GetMapping("{notificationId}")
  public Mono<ResponseEntity<NotificationLogDto>> getNotificationLog(
          @PathVariable("notificationId") @Valid @NotEmpty(message = "notificationId cannot be empty") final String notificationId) {
    return this.notificationService
            .findNotificationLog(notificationId)
            .map(NotificationLogDtoMapper.INSTANCE::toRest)
            .doOnNext(
                    notificationLogDto ->
                            LOGGER
                                    .debug()
                                    .log("Notification found: {}", notificationLogDto.getNotificationId()))
            .doOnError(
                    throwable ->
                            LOGGER
                                    .error()
                                    .log(
                                            String.format(
                                                    "Error finding current NotificationLog: '%s'", notificationId),
                                            throwable))
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @GetMapping(":findByObjectId")
  public Mono<ResponseEntity<List<NotificationLogDto.NotificationDto>>>
  getNotificationLogByObjectId(
          @PathVariable("objectId") @Valid @NotEmpty(message = "objectId cannot be empty") final String objectId) {
    return this.notificationService
            .findNotificationsByObjectId(objectId)
            .map(this::mapToRest)
            .doOnNext(
                    notificationLogs -> LOGGER.debug().log("Notification (byObjectId) found: {}", objectId))
            .doOnError(
                    throwable ->
                            LOGGER
                                    .error()
                                    .log(
                                            String.format("Error finding current NotificationLog: '%s'", objectId),
                                            throwable))
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  private List<NotificationLogDto.NotificationDto> mapToRest(
          List<NotificationLogVo.NotificationVo> notificationVos) {
    return notificationVos.stream()
            .map(NotificationLogDtoMapper.INSTANCE::toRest)
            .collect(Collectors.toList());
  }
}
