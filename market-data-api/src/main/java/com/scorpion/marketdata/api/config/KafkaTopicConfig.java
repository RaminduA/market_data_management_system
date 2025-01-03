package com.scorpion.marketdata.api.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic marketDataUpdateTopic() {
        return TopicBuilder
                .name("market-data-update")
                .partitions(10)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic marketDataQuerySpecificTopic() {
        return TopicBuilder
                .name("market-data-query-specific")
                .partitions(10)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic marketDataQueryConsolidatedTopic() {
        return TopicBuilder
                .name("market-data-query-consolidated")
                .partitions(10)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic marketDataQueryConsolidatedBatchTopic() {
        return TopicBuilder
                .name("market-data-query-consolidated-batch")
                .partitions(10)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic marketDataDeleteTopic() {
        return TopicBuilder
                .name("market-data-delete")
                .partitions(10)
                .replicas(3)
                .build();
    }

    /*@Bean
    public NewTopic marketDataResponseTopic() {
        return TopicBuilder
                .name("market-data-response")
                .partitions(10)
                .replicas(3)
                .build();
    }*/
}
