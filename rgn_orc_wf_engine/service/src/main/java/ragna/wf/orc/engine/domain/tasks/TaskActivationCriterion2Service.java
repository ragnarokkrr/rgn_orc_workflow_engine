package ragna.wf.orc.engine.domain.tasks;

import ragna.wf.orc.engine.domain.tasks.vo.Criterion2Query;
import ragna.wf.orc.engine.domain.tasks.vo.Criterion2ResponseVo;
import reactor.core.publisher.Mono;

public interface TaskActivationCriterion2Service {

  Mono<Criterion2ResponseVo> findByCustomerId(Criterion2Query criterion2Request);
}
