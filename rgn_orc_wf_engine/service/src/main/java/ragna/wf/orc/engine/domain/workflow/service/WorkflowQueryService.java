package ragna.wf.orc.engine.domain.workflow.service;

import lombok.RequiredArgsConstructor;
import org.fissore.slf4j.FluentLogger;
import org.fissore.slf4j.FluentLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ragna.wf.orc.engine.domain.workflow.repository.WorkflowRepository;
import ragna.wf.orc.engine.domain.workflow.service.mapper.WorkflowMapper;
import ragna.wf.orc.engine.domain.workflow.service.vo.WorkflowVO;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class WorkflowQueryService {
  private static final FluentLogger LOGGER =
      FluentLoggerFactory.getLogger(WorkflowQueryService.class);
  private final WorkflowRepository workflowRepository;

  public Mono<WorkflowVO> findById(final String id) {
    return workflowRepository.findById(id).map(WorkflowMapper.INSTANCE::toService);
  }

  public Mono<WorkflowVO> findByCustomerRequestId(final String customerRequestId) {
    return workflowRepository
        .findByCustomerRequest(customerRequestId)
        .map(WorkflowMapper.INSTANCE::toService);
  }
}
