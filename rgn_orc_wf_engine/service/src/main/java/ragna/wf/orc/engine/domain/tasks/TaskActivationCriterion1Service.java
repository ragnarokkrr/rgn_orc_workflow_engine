package ragna.wf.orc.engine.domain.tasks;

import ragna.wf.orc.engine.domain.tasks.vo.Criterion1Query;
import ragna.wf.orc.engine.domain.tasks.vo.Criterion1ResponseVo;
import reactor.core.publisher.Mono;

public interface TaskActivationCriterion1Service {

  Mono<Criterion1ResponseVo> findByCustomerId(Criterion1Query criterion1RequestReq);
}
