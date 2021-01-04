package ragna.wf.orc.eventstore.config;

import org.springframework.util.Assert;
import org.testcontainers.containers.MongoDBContainer;

public class MongoDBTestContainers {
  public static final int MONGODB_INTERNAL_PORT = 27017;
  public static final String MONGODB_DOCKER_REPLICASET = "docker-rs";
  public static final String MONGODB_DB = "test";

  public static MongoDBContainer defaultMongoContainer() {
    return new RgnMongoContainer("mongo:4.2").withReuse(true);
  }

  public static void setSpringDataProperties(MongoDBContainer mongoDBContainer) {
    Assert.isTrue(
        mongoDBContainer instanceof RgnMongoContainer,
        "Mongo container IS NOT instance of RgnMongoContainer");
    System.setProperty(
        "spring.autoconfigure.exclude",
        "org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration");
    System.setProperty("spring.data.mongodb.database", MONGODB_DB);
    System.setProperty("spring.data.mongodb.host", mongoDBContainer.getHost());
    System.setProperty(
        "spring.data.mongodb.port", ((RgnMongoContainer) mongoDBContainer).getPort().toString());
    System.setProperty("spring.data.mongodb.replicaSet", MONGODB_DOCKER_REPLICASET);
  }

  private MongoDBTestContainers() {}
}
