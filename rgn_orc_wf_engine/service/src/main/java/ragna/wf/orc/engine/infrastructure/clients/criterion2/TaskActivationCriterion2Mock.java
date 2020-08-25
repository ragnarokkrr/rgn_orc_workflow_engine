package ragna.wf.orc.engine.infrastructure.clients.criterion2;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ragna.wf.orc.engine.domain.tasks.TaskActivationCriterion2Service;
import ragna.wf.orc.engine.domain.tasks.vo.Criterion2Query;
import ragna.wf.orc.engine.domain.tasks.vo.Criterion2ResponseVo;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TaskActivationCriterion2Mock implements TaskActivationCriterion2Service {

  public static final long VALUE = 5L;

  @Override
  public Mono<Criterion2ResponseVo> findByCustomerId(final Criterion2Query criterion2Request) {
    return Mono.just(Criterion2ResponseVo.builder().value(VALUE).build());
  }
}
