package ragna.wf.orc.common.data.mongodb.sequences;

import static org.springframework.data.mongodb.core.FindAndModifyOptions.options;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.util.concurrent.CountDownLatch;
import lombok.RequiredArgsConstructor;
import org.fissore.slf4j.FluentLogger;
import org.fissore.slf4j.FluentLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MongoSequenceGenerator {
  private static final FluentLogger LOGGER =
      FluentLoggerFactory.getLogger(MongoSequenceGenerator.class);
  private final ReactiveMongoOperations mongoOperations;
  private final TransactionalOperator transactionalOperator;

  @Transactional(propagation = Propagation.NESTED)
  public Mono<Long> generateSequence(final String seqName) {
    final var latch = new CountDownLatch(1);
    final long[] counter = {-1L};
    return transactionalOperator
        .transactional(
            this.mongoOperations.findAndModify(
                query(where("_id").is(seqName)),
                new Update().inc("seq", 1),
                options().returnNew(true).upsert(true),
                DatabaseSequence.class))
        .map(DatabaseSequence::getSeq);
  }
}
