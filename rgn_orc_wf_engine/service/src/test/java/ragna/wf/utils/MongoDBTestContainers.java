package ragna.wf.utils;

import org.testcontainers.containers.MongoDBContainer;

public class MongoDBTestContainers {
  public static final int MONGODB_INTERNAL_PORT = 27017;
  public static final String MONGODB_DOCKER_REPLICASET = "docker-rs";

  public static void setSpringDataProperties(MongoDBContainer mongoDBContainer){
    System.setProperty("spring.autoconfigure.exclude", "org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration");
    System.setProperty("spring.data.mongodb.database", "test");
    System.setProperty("spring.data.mongodb.host", "localhost");
    System.setProperty("spring.data.mongodb.port", mongoDBContainer.getMappedPort(MongoDBTestContainers.MONGODB_INTERNAL_PORT).toString());
    System.setProperty("spring.data.mongodb.replicaSet", MONGODB_DOCKER_REPLICASET);
  }

  private MongoDBTestContainers() {
  }
}
