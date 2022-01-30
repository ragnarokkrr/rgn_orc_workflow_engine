package ragna.wf.orc.engine.infrastructure;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@Testcontainers
@SpringBootTest(
    classes = {
      KafkaCloudStreamTest.App.class,
      KafkaCloudStreamTest.InputConsumer.class,
      KafkaCloudStreamTest.OutputProducer.class
    })
@Import(KafkaCloudStreamTest.KafkaTestContainersConfiguration.class)
public class KafkaCloudStreamTest {
  @Container
  public static KafkaContainer kafka =
      new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:5.4.3"));

  @Autowired private OutputProducer outputProducer;
  @Autowired private InputConsumer inputConsumer;
  @Autowired CountDownLatch myCountDownLatch;

  @BeforeAll
  static void setup() {
    System.setProperty("spring.cloud.stream.kafka.binder.brokers", kafka.getBootstrapServers());

    System.setProperty("spring.cloud.stream.bindings.test-output.destination", "test-channel");
    System.setProperty(
        "spring.cloud.stream.bindings.test-output.partitionKeyExpression", "payload.id");

    System.setProperty("spring.cloud.stream.bindings.test-input.destination", "test-channel");
    System.setProperty("spring.cloud.stream.bindings.test-input.contentType", "application/json");
    System.setProperty(
        "spring.cloud.stream.bindings.test-input.group", "${spring.application.name}");
    System.setProperty("spring.cloud.stream.bindings.test-input.consumer.partitioned", "true");
  }

  @Test
  void testKafkaChannel() throws InterruptedException {
    outputProducer.send(Person.builder().id("1").name("John Connor").build());
    myCountDownLatch.await();
    assertThat(inputConsumer.getReceivedPerson())
        .isNotEmpty()
        .contains(Person.builder().id("1").name("John Connor").build());
  }

  @TestConfiguration
  static class KafkaTestContainersConfiguration {
    @Bean
    public CountDownLatch myCountDownLatch() {
      return new CountDownLatch(1);
    }
  }

  interface TestChannels {

    String TEST_OUTPUT = "test-channel";
    String TEST_INPUT = "test-channel";

    @Output(TEST_OUTPUT)
    MessageChannel outputChannel();

    @Input(TEST_INPUT)
    SubscribableChannel inputChannel();
  }

  @Component
  @EnableBinding(TestChannels.class)
  @Slf4j
  @RequiredArgsConstructor
  static class OutputProducer {
    private final TestChannels testChannels;

    public void send(Person person) {
      log.info("Sending message: {}", person);
      final var personMessageBuilder = MessageBuilder.withPayload(person);
      final var personMessage = personMessageBuilder.build();
      final var messageSent = this.testChannels.outputChannel().send(personMessage);
      log.info("Message sent! ({}): {}", messageSent, person);
    }
  }

  @Component
  @EnableBinding(TestChannels.class)
  @Slf4j
  @RequiredArgsConstructor
  static class InputConsumer {
    private final CountDownLatch countDownLatch;
    @Getter private Optional<Person> receivedPerson = Optional.empty();

    @StreamListener(target = TestChannels.TEST_INPUT)
    public void consume(Person person) {
      log.info("Received person! {}", person);
      this.receivedPerson = Optional.of(person);
      this.countDownLatch.countDown();
    }
  }

  @SpringBootApplication
  static class App {}
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class Person {
  private String id;
  private String name;
}
