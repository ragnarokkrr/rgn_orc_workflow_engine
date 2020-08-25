package ragna.wf.orc.engine.infrastructure.clients.criterion1;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ragna.wf.orc.engine.domain.tasks.TaskActivationCriterion1Service;
import ragna.wf.orc.engine.domain.tasks.vo.Criterion1Query;
import ragna.wf.orc.engine.domain.tasks.vo.Criterion1ResponseVo;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TaskActivationCriterion1Mock implements TaskActivationCriterion1Service {

  public static final long VALUE = 4L;

  @Override
  public Mono<Criterion1ResponseVo> findByCustomerId(final Criterion1Query criterion1RequestReq) {
    return Mono.just(Criterion1ResponseVo.builder().value(VALUE).build());
  }
}
