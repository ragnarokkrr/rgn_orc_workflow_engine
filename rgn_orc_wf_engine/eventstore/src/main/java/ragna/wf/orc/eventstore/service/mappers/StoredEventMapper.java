package ragna.wf.orc.eventstore.service.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ragna.wf.orc.eventstore.model.StoredEvent;
import ragna.wf.orc.eventstore.service.vo.StoredEventVo;

@Mapper
public interface StoredEventMapper {
  StoredEventMapper INSTANCE = Mappers.getMapper(StoredEventMapper.class);

  StoredEventVo toService(StoredEvent storedEvent);

  StoredEvent toModel(StoredEventVo storedEventVo);
}
