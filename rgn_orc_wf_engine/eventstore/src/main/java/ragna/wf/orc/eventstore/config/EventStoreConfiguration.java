package ragna.wf.orc.eventstore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import ragna.wf.orc.common.serialization.DefaultKryoContext;
import ragna.wf.orc.common.serialization.KryoContext;

@Configuration
@ComponentScan(
    basePackages = {"ragna.wf.orc.common.data.mongodb.sequences", "ragna.wf.orc.eventstore"})
public class EventStoreConfiguration {

  @Bean
  KryoContext kryoContext() {
    return DefaultKryoContext.kryoContextWithDefaultSerializers();
  }
}
