package ragna.wf.orc.eventstore.config;

import com.mongodb.BasicDBList;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoDatabase;
/*
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongoCmdOptionsBuilder;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

import org.bson.Document;
 */
import org.springframework.boot.autoconfigure.AbstractDependsOnBeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import ragna.wf.orc.common.data.mongodb.test.SubscriberHelpers;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

// https://apisimulator.io/spring-boot-auto-configuration-embedded-mongodb-transactions/
// https://mongodb.github.io/mongo-java-driver/4.0/driver/getting-started/quick-start/
// UPGRADE: https://github.com/flapdoodle-oss/de.flapdoodle.embed.mongo/issues/325
// ERROR: https://github.com/flapdoodle-oss/de.flapdoodle.embed.mongo/issues/324
/*
@Profile("embedMongoWithTx")
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore({MongoAutoConfiguration.class})
@ConditionalOnClass({MongoClient.class, MongodStarter.class})
@Import({
  EmbeddedMongoAutoConfiguration.class,
  EmbeddedMongoWithTransactionsConfig.DependenciesConfiguration.class
})
*/
public class EmbeddedMongoWithTransactionsConfig {

  // You may get a warning in the log upon shutdown like this:
  // "...Destroy method 'stop' on bean with name 'embeddedMongoServer' threw an
  // exception: java.lang.IllegalStateException: Couldn't kill mongod process!..."
  // That seems harmless as the MongoD process shuts down and frees up the port.
  // There are multiple related issues logged on GitHub:
  // https://github.com/flapdoodle-oss/de.flapdoodle.embed.mongo/issues?q=is%3Aissue+Couldn%27t+kill+mongod+process%21

  public static final int DFLT_PORT_NUMBER = 27017;
  public static final String DFLT_REPLICASET_NAME = "rs0";
  public static final int DFLT_STOP_TIMEOUT_MILLIS = 200;
/*
  private Version.Main mFeatureAwareVersion = Version.Main.V4_0;
  private int mPortNumber = DFLT_PORT_NUMBER;
  private String mReplicaSetName = DFLT_REPLICASET_NAME;
  private long mStopTimeoutMillis = DFLT_STOP_TIMEOUT_MILLIS;

  @Bean
  public IMongodConfig mongodConfig() throws IOException, UnknownHostException {

    return new MongodConfigBuilder()
        .version(mFeatureAwareVersion)
        .withLaunchArgument("--replSet", mReplicaSetName)
        .withLaunchArgument("--bind_ip", "127.0.0.1")
        .stopTimeoutInMillis(mStopTimeoutMillis)
        .cmdOptions(new MongoCmdOptionsBuilder().useNoJournal(false).build())
        .net(new Net(mPortNumber, Network.localhostIsIPv6()))
        .build();
  }

  /**
   * Initializes a new replica set. Based on code from
   * https://github.com/flapdoodle-oss/de.flapdoodle.embed.mongo/issues/257
   * /
  class EmbeddedMongoReplicaSetInitialization {

    EmbeddedMongoReplicaSetInitialization() throws Exception {
      MongoClient mongoClient = null;
      try {
        final BasicDBList members = new BasicDBList();
        members.add(new Document("_id", 0).append("host", "localhost:" + mPortNumber));

        final Document replSetConfig = new Document("_id", mReplicaSetName);
        replSetConfig.put("members", members);

        final var mHost = "127.0.0.1";

        mongoClient =
            MongoClients.create(
                MongoClientSettings.builder()
                    .applyToClusterSettings(
                        builder ->
                            builder.hosts(Arrays.asList(new ServerAddress(mHost, mPortNumber))))
                    .build());

        final MongoDatabase adminDatabase = mongoClient.getDatabase("admin");
        adminDatabase
            .runCommand(new Document("replSetInitiate", replSetConfig))
            .subscribe(new SubscriberHelpers.PrintDocumentSubscriber());

        adminDatabase
            .runCommand(new Document("setFeatureCompatibilityVersion", "4.0"))
            .subscribe(new SubscriberHelpers.PrintDocumentSubscriber());

        adminDatabase
                .runCommand(new Document("setParameter", 1)
                .append("maxTransactionLockRequestTimeoutMillis", 5000))
                .subscribe(new SubscriberHelpers.PrintDocumentSubscriber());

        TimeUnit.SECONDS.sleep(3);
      } finally {
        if (mongoClient != null) {
          mongoClient.close();
        }
      }
    }
  }

  @Bean
  EmbeddedMongoReplicaSetInitialization embeddedMongoReplicaSetInitialization() throws Exception {
    return new EmbeddedMongoReplicaSetInitialization();
  }

  /**
   * Additional configuration to ensure that the replica set initialization happens after the {@link
   * MongodExecutable} bean is created. That's it - after the database is started.
   * /
  @ConditionalOnClass({MongoClient.class, MongodStarter.class})
  protected static class DependenciesConfiguration
      extends AbstractDependsOnBeanFactoryPostProcessor {

    DependenciesConfiguration() {
      super(EmbeddedMongoReplicaSetInitialization.class, null, MongodExecutable.class);
    }
  }

 */
}
