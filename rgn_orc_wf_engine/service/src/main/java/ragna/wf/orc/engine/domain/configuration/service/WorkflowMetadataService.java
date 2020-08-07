package ragna.wf.orc.engine.domain.configuration.service;

import ragna.wf.orc.engine.domain.configuration.service.vo.ConfigurationRequest;
import ragna.wf.orc.engine.domain.configuration.service.vo.ConfigurationVO;
import reactor.core.publisher.Mono;

public interface WorkflowMetadataService {

  Mono<ConfigurationVO> peekConfigurationForWorkflow(
      final ConfigurationRequest configurationRequest);
}
