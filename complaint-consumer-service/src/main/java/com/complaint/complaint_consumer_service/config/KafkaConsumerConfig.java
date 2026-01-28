package com.complaint.complaint_consumer_service.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;
import org.springframework.beans.factory.annotation.Value;
import org.apache.kafka.common.TopicPartition;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, ComplaintEventDTO> consumerFactory() {
        // Configuration for Kafka Consumer
        Map<String, Object> props = new HashMap<>();
        // Address of Kafka broker
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
       
        // Deserializers for key and value
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        // Group ID determines which consumer group this client belongs to
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "complaint-consumers");
        return new DefaultKafkaConsumerFactory<>(props);
    }


    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
       
        factory.setConsumerFactory(consumerFactory());
       
        return factory;
    }


    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<Object, Object> kafkaTemplate) {
        // 1. Define where the failed messages go (defaults to topicname.DLT)
        DeadLetterPublishingRecoverer recoverer =
             new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record,ex) -> new TopicPartition(record.topic() + ".DLQ", record.partition()));

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, new FixedBackOff(2000L, 3));
            
          
        handler.addNotRetryableExceptions(
                com.fasterxml.jackson.databind.exc.MismatchedInputException.class,
                com.fasterxml.jackson.core.JsonParseException.class,
                IllegalArgumentException.class
        );

        return handler;
    }
}
    
