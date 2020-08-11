package ragna.wf.orc.eventstore.rest.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ragna.wf.orc.eventstore.rest.dto.NotificationLogDto;
import ragna.wf.orc.eventstore.service.vo.NotificationLogVo;

@Mapper
public interface NotificationLogDtoMapper {
  NotificationLogDtoMapper INSTANCE = Mappers.getMapper(NotificationLogDtoMapper.class);

  NotificationLogDto toRest(NotificationLogVo notificationLogVo);

  NotificationLogDto.NotificationDto toRest(NotificationLogVo.NotificationVo notificationVo);
}
