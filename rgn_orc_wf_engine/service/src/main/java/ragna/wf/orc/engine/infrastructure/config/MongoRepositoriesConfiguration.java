package ragna.wf.orc.engine.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableReactiveMongoRepositories({
        "ragna.wf.orc.engine.domain.workflow.repository",
        "ragna.wf.orc.eventstore.repository"
})
public class MongoRepositoriesConfiguration {
}
