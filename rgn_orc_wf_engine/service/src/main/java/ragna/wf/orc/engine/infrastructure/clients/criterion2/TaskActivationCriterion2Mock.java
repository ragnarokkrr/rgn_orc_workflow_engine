package ragna.wf.orc.engine.infrastructure.clients.criterion2;

import lombok.RequiredArgsConstructor;
import org.fissore.slf4j.FluentLogger;
import org.fissore.slf4j.FluentLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ragna.wf.orc.engine.domain.tasks.TaskActivationCriterion2Service;
import ragna.wf.orc.engine.domain.tasks.vo.Criterion2Query;
import ragna.wf.orc.engine.domain.tasks.vo.Criterion2ResponseVo;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TaskActivationCriterion2Mock implements TaskActivationCriterion2Service {
  private static final FluentLogger LOGGER =
      FluentLoggerFactory.getLogger(TaskActivationCriterion2Mock.class);
  public static final long VALUE = 5L;

  @Override
  public Mono<Criterion2ResponseVo> findByCustomerId(final Criterion2Query criterion2Request) {
    LOGGER
        .info()
        .log(
            "Calling 3rd party MOCK CRIT_02 SERVICE. Req.: {}. Res.: {} ",
            criterion2Request,
            VALUE);
    return Mono.just(Criterion2ResponseVo.builder().value(VALUE).build());
  }
}
