package ragna.wf.orc.engine.domain.metadata.service;

import ragna.wf.orc.engine.domain.metadata.service.vo.ConfigurationRequest;
import ragna.wf.orc.engine.domain.metadata.service.vo.ConfigurationVO;
import reactor.core.publisher.Mono;

public interface WorkflowMetadataService {

  Mono<ConfigurationVO> peekConfigurationForWorkflow(
      final ConfigurationRequest configurationRequest);
}
