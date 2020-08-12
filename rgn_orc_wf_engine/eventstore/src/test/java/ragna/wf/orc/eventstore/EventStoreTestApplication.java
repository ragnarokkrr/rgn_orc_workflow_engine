package ragna.wf.orc.eventstore;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.ReactiveMongoTransactionManager;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.reactive.TransactionalOperator;
import ragna.wf.orc.eventstore.config.EventStoreConfiguration;

@SpringBootApplication
@EnableReactiveMongoRepositories
@EnableMongoRepositories
@EnableTransactionManagement
public class EventStoreTestApplication {
  @Configuration
  @Import(EventStoreConfiguration.class)
  static class Config {
    @Bean
    TransactionalOperator transactionalOperator(
        final ReactiveTransactionManager reactiveTransactionManager) {
      return TransactionalOperator.create(reactiveTransactionManager);
    }

    @Bean
    ReactiveTransactionManager reactiveTransactionManager(
        final ReactiveMongoDatabaseFactory reactiveMongoDatabaseFactory) {
      return new ReactiveMongoTransactionManager(reactiveMongoDatabaseFactory);
    }
  }
}
