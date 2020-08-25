package ragna.wf.orc.eventstore.service.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ragna.wf.orc.eventstore.model.StoredEvent;
import ragna.wf.orc.eventstore.service.vo.StoredEventVo;

@Mapper
public interface StoredEventMapper {
  StoredEventMapper INSTANCE = Mappers.getMapper(StoredEventMapper.class);

  @Mapping(target = "domainEvent", ignore = true)
  StoredEventVo toService(StoredEvent storedEvent);

  @Mapping(target = "failed", ignore = true)
  StoredEvent toModel(StoredEventVo storedEventVo);
}
