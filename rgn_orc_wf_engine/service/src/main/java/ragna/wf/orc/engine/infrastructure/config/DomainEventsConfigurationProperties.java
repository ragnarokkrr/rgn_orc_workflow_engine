package ragna.wf.orc.engine.infrastructure.config;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("orc.events")
@ConstructorBinding
@RequiredArgsConstructor
@Data
public class DomainEventsConfigurationProperties {
  private Integer replayInitialDelaySecs = 30;
}
