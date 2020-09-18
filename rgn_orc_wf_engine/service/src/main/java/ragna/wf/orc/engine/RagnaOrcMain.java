package ragna.wf.orc.engine;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoDatabase;
import org.bson.Document;
import org.fissore.slf4j.FluentLogger;
import org.fissore.slf4j.FluentLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import ragna.wf.orc.common.data.mongodb.test.SubscriberHelpers;

import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class RagnaOrcMain {
  private static final FluentLogger LOGGER = FluentLoggerFactory.getLogger(RagnaOrcMain.class);
  @Autowired private MongoClient mongoClient;

  public static void main(String[] args) {
    // ReactorDebugAgent.init();
    SpringApplication.run(RagnaOrcMain.class, args);
  }

  @Bean
  CommandLineRunner preLoadMongo() throws Exception {
    return args -> {
      LOGGER.info().log("INITIALIZING DB");
      final MongoDatabase adminDatabase = mongoClient.getDatabase("admin");
//      adminDatabase
  //        .runCommand(new Document("setFeatureCompatibilityVersion", "4.2"))
    //      .subscribe(new SubscriberHelpers.PrintDocumentSubscriber());

      //adminDatabase
      //    .runCommand(
      //        new Document().append("featureCompatibilityVersion", 1).append("getParameter", 1))
      //    .subscribe(new SubscriberHelpers.PrintDocumentSubscriber());
      LOGGER.info().log("INITIALIZING DB - done!");
      TimeUnit.SECONDS.sleep(3);
    };
  }
}
