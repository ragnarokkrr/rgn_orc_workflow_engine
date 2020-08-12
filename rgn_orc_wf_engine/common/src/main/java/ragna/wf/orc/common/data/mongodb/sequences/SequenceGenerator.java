package ragna.wf.orc.common.data.mongodb.sequences;

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
import ragna.wf.orc.common.exceptions.ErrorCode;
import ragna.wf.orc.common.exceptions.eventstore.EventStoreException;

import java.util.concurrent.CountDownLatch;

import static org.springframework.data.mongodb.core.FindAndModifyOptions.options;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SequenceGenerator {
  private static final FluentLogger LOGGER = FluentLoggerFactory.getLogger(SequenceGenerator.class);
  private final ReactiveMongoOperations mongoOperations;
  private final TransactionalOperator transactionalOperator;

  @Transactional(propagation = Propagation.NESTED)
  /* TODO FIX sequence generator TX issue
  The document is updated at Mongo db, but the Tx hangs and the doOnSuccess, doOnTerminate callbaclks were never triggered.
  after several minutes we have a transaction error
  BIGGEST PROBLEM: PAGINATION IS CALCULATED BASED ON ID
  perhaps should change to blocking mongoOperations
  */
  public long generateSequence(final String seqName) {
    final var latch = new CountDownLatch(1);
    final long[] counter = {-1L};
    //transactionalOperator
     //   .transactional(
            this.mongoOperations.findAndModify(
                query(where("_id").is(seqName)),
                new Update().inc("seq", 1),
                options().returnNew(true).upsert(true),
                DatabaseSequence.class)//)
        .doOnSubscribe(
            subscription -> LOGGER.info().log("Subscribing Sequence generator {}", seqName))
        .doOnSuccess(
            databaseSequence -> {
              counter[0] = databaseSequence.getSeq();
            })
        .doOnTerminate(latch::countDown)
        .subscribe(
            databaseSequence -> LOGGER.info().log("Sequence generator {}", databaseSequence));

    try {
      latch.await();
    } catch (InterruptedException e) {
      throw new EventStoreException(
          String.format("Can't generate Stored Event Sequence: %s", seqName),
          ErrorCode.EVS_GENERAL_ERROR,
          e);
    }
    return counter[0] <= 0 ? 1 : counter[0];
  }
}
