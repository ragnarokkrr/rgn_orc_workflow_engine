package ragna.wf.orc.datapipeline;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.app.mongodb.source.MongodbSourceProperties;
import org.springframework.cloud.stream.app.trigger.TriggerConfiguration;
import org.springframework.cloud.stream.app.trigger.TriggerPropertiesMaxMessagesDefaultUnlimited;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.expression.Expression;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlowBuilder;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.mongodb.inbound.MongoDbMessageSource;
import org.springframework.messaging.MessageChannel;

@EnableBinding(Source.class)
@EnableConfigurationProperties({
  MongodbSourceProperties.class,
  TriggerPropertiesMaxMessagesDefaultUnlimited.class
})
@Import(TriggerConfiguration.class)
public class MongodbSourceConfiguration {

  @Autowired private MongodbSourceProperties config;

  @Autowired
  @Qualifier(Source.OUTPUT)
  private MessageChannel output;

  @Autowired private MongoTemplate mongoTemplate;

  @Bean
  public IntegrationFlow startFlow() throws Exception {
    IntegrationFlowBuilder flow = IntegrationFlows.from(mongoSource());
    if (config.isSplit()) {
      flow.split();
    }
    flow.channel(output);
    return flow.get();
  }

  /**
   * The inheritors can consider to override this method for their purpose or just adjust options
   * for the returned instance
   *
   * @return a {@link MongoDbMessageSource} instance
   */
  protected MongoDbMessageSource mongoSource() {
    Expression queryExpression =
        (this.config.getQueryExpression() != null
            ? this.config.getQueryExpression()
            : new LiteralExpression(this.config.getQuery()));
    MongoDbMessageSource mongoDbMessageSource =
        new MongoDbMessageSource(this.mongoTemplate, queryExpression);
    mongoDbMessageSource.setCollectionNameExpression(
        new LiteralExpression(this.config.getCollection()));
    mongoDbMessageSource.setEntityClass(String.class);
    return mongoDbMessageSource;
  }
}
