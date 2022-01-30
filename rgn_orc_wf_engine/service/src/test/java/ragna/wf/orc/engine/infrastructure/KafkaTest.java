package ragna.wf.orc.engine.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SpringExtension.class)
@Testcontainers
@SpringBootTest(
    properties = "test.topic=embedded-test-topic",
    classes = {KafkaTest.App.class, KafkaTest.KafkaConsumer.class, KafkaTest.KafkaProducer.class})
@Import(KafkaTest.KafkaTestContainersConfiguration.class)
public class KafkaTest {
  @Container
  public static KafkaContainer kafka =
      new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:5.4.3"));

  @Autowired public KafkaTemplate<String, String> template;

  @Autowired private KafkaConsumer consumer;

  @Autowired private KafkaProducer producer;

  @Value("${test.topic}")
  private String topic;

  @Test
  public void givenKafkaDockerContainer_whenSendingtoDefaultTemplate_thenMessageReceived()
      throws Exception {
    template.send(topic, "Sending with default template");
    consumer.getLatch().await(10000, TimeUnit.MILLISECONDS);

    assertThat(consumer.getLatch().getCount()).isEqualTo(0L);
    assertThat(consumer.getPayload()).contains("embedded-test-topic");
  }

  @Test
  public void givenKafkaDockerContainer_whenSendingtoSimpleProducer_thenMessageReceived()
      throws Exception {
    producer.send(topic, "Sending with own controller");
    consumer.getLatch().await(10000, TimeUnit.MILLISECONDS);

    assertThat(consumer.getLatch().getCount()).isEqualTo(0L);
    assertThat(consumer.getPayload()).contains("embedded-test-topic");
  }

  @TestConfiguration
  static class KafkaTestContainersConfiguration {

    @Bean
    ConcurrentKafkaListenerContainerFactory<Integer, String> kafkaListenerContainerFactory() {
      ConcurrentKafkaListenerContainerFactory<Integer, String> factory =
          new ConcurrentKafkaListenerContainerFactory<>();
      factory.setConsumerFactory(consumerFactory());
      return factory;
    }

    @Bean
    public ConsumerFactory<Integer, String> consumerFactory() {
      return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    @Bean
    public Map<String, Object> consumerConfigs() {
      Map<String, Object> props = new HashMap<>();
      props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
      props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
      props.put(ConsumerConfig.GROUP_ID_CONFIG, "ragna");
      props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
      props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
      return props;
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
      Map<String, Object> configProps = new HashMap<>();
      configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
      configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
      configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
      return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
      return new KafkaTemplate<>(producerFactory());
    }
  }

  @Slf4j
  @Component
  static class KafkaConsumer {

    private CountDownLatch latch = new CountDownLatch(1);
    private String payload = null;

    @KafkaListener(topics = "${test.topic}")
    public void receive(ConsumerRecord<?, ?> consumerRecord) {
      log.info("received payload='{}'", consumerRecord.toString());
      setPayload(consumerRecord.toString());
      latch.countDown();
    }

    public CountDownLatch getLatch() {
      return latch;
    }

    public String getPayload() {
      return payload;
    }

    private void setPayload(String payload) {
      this.payload = payload;
    }
  }

  @Slf4j
  @Component
  static class KafkaProducer {

    @Autowired private KafkaTemplate<String, String> kafkaTemplate;

    public void send(String topic, String payload) {
      log.info("sending payload='{}' to topic='{}'", payload, topic);
      kafkaTemplate.send(topic, payload);
    }
  }

  @SpringBootApplication
  static class App {}
}
