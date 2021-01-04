package ragna.wf.orc.common.data.mongodb.utils;

import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import reactor.core.publisher.Flux;

public class MongoDbUtils {
  private MongoDbUtils() {}

  public static Flux<Pair<String, Boolean>> reCreateCollections(
      final ReactiveMongoOperations reactiveMongoOperations) {
    return reCreateCollections(getCollectionNames(), reactiveMongoOperations);
  }

  public static Flux<Pair<String, Boolean>> reCreateCollections(
      final List<String> collections, final ReactiveMongoOperations reactiveMongoOperations) {
    return Flux.fromIterable(collections)
        .flatMap(
            collectionName ->
                reactiveMongoOperations
                    .collectionExists(collectionName)
                    .map(exists -> Pair.of(collectionName, exists)))
        .flatMap(
            collectionExistsPair -> {
              if (!collectionExistsPair.getRight()) {
                return reactiveMongoOperations
                    .createCollection(collectionExistsPair.getLeft())
                    .map(documentMongoCollection -> Pair.of(collectionExistsPair.getLeft(), false));
              }
              return reactiveMongoOperations
                  .dropCollection(collectionExistsPair.getLeft())
                  .then(
                      reactiveMongoOperations
                          .createCollection(collectionExistsPair.getLeft())
                          .map(
                              documentMongoCollection ->
                                  Pair.of(collectionExistsPair.getLeft(), true)));
            });
  }

  public static List<String> getCollectionNames() {
    return List.of("workflows", "stored_events", "database_sequences");
  }
}
