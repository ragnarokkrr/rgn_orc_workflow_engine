package ragna.wf.orc.engine.domain.workflow.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ragna.wf.orc.engine.domain.configuration.service.vo.ConfigurationVO;
import ragna.wf.orc.engine.domain.workflow.model.Configuration;

@Mapper
public interface ConfigurationMapper {
  ConfigurationMapper INSTANCE = Mappers.getMapper(ConfigurationMapper.class);

  Configuration toModel(ConfigurationVO configurationVO);
}
