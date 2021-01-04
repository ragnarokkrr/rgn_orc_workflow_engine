package ragna.wf.orc.engine.infrastructure.clients.criterion1;

import lombok.RequiredArgsConstructor;
import org.fissore.slf4j.FluentLogger;
import org.fissore.slf4j.FluentLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ragna.wf.orc.engine.domain.tasks.TaskActivationCriterion1Service;
import ragna.wf.orc.engine.domain.tasks.vo.Criterion1Query;
import ragna.wf.orc.engine.domain.tasks.vo.Criterion1ResponseVo;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TaskActivationCriterion1Mock implements TaskActivationCriterion1Service {
  private static final FluentLogger LOGGER =
      FluentLoggerFactory.getLogger(TaskActivationCriterion1Mock.class);
  public static final long VALUE = 4L;

  @Override
  public Mono<Criterion1ResponseVo> findByCustomerId(final Criterion1Query criterion1RequestReq) {
    LOGGER
        .info()
        .log(
            "Calling 3rd party MOCK CRIT_01 SERVICE Req.: {}. Res.: {} ",
            criterion1RequestReq,
            VALUE);

    return Mono.just(Criterion1ResponseVo.builder().value(VALUE).build());
  }
}
