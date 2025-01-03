package com.scorpion.marketdata.core.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic marketDataResponseTopic() {
        return TopicBuilder
                .name("market-data-response")
                .partitions(10)
                .replicas(3)
                .build();
    }
}
