package ragna.wf.orc.engine.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(ragna.wf.orc.eventstore.config.EventStoreConfiguration.class)
public class EventStoreConfiguration {}
