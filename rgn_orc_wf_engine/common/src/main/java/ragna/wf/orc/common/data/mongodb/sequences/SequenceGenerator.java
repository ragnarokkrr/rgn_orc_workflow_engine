package ragna.wf.orc.common.data.mongodb.sequences;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import ragna.wf.orc.common.exceptions.ErrorCode;
import ragna.wf.orc.common.exceptions.eventstore.EventStoreException;

import java.util.concurrent.CountDownLatch;

import static org.springframework.data.mongodb.core.FindAndModifyOptions.options;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SequenceGenerator {

    private final ReactiveMongoOperations mongoOperations;

    public long generateSequence(final String seqName) {
        final var latch = new CountDownLatch(1);
        final long[] counter = {-1L};
        this.mongoOperations
                .findAndModify(
                        query(where("_id").is(seqName)),
                        new Update().inc("seq", 1),
                        options().returnNew(true).upsert(true),
                        DatabaseSequence.class)
                .doOnSuccess(
                        databaseSequence -> {
                            counter[0] = databaseSequence.getSeq();
                            latch.countDown();
                        })
                .subscribe();

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
