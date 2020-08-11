package ragna.wf.orc.eventstore.service.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ragna.wf.orc.eventstore.model.SerializationEngine;
import ragna.wf.orc.eventstore.model.StoredEventStatus;
import ragna.wf.orc.eventstore.service.vo.NotificationLogVo;

@Mapper
public interface NotificationLogVoMapper {
  NotificationLogVoMapper INSTANCE = Mappers.getMapper(NotificationLogVoMapper.class);

  NotificationLogVo.NotificationVo.StoredEventStatus toService(StoredEventStatus storedEventStatus);

  NotificationLogVo.NotificationVo.SerializationEngine toService(
          SerializationEngine serializationEngine);
}
