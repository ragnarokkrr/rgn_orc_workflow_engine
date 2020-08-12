package ragna.wf.orc.engine.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.ReactiveMongoTransactionManager;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.reactive.TransactionalOperator;

@Configuration
@EnableTransactionManagement
@EnableReactiveMongoRepositories({
  "ragna.wf.orc.engine.domain.workflow.repository",
  "ragna.wf.orc.eventstore.repository"
})
public class MongoRepositoriesConfiguration {

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
