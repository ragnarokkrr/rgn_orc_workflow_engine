package ragna.wf.orc.engine.infrastructure.configuration;

import javax.validation.Validation;
import javax.validation.Validator;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class SpringBootTestsConfiguration {
  @Bean("validator")
  public Validator validator() {
    return (Validation.buildDefaultValidatorFactory().getValidator());
  }
}
