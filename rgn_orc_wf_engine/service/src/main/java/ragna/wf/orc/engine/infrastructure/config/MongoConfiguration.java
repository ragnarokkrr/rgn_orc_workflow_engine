package ragna.wf.orc.engine.infrastructure.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@Configuration
@EnableAutoConfiguration
@EnableReactiveMongoRepositories(basePackages = "ragna.wf.orc.engine.domain.workflow.repository")
@EnableMongoRepositories
public class MongoConfiguration {

}
