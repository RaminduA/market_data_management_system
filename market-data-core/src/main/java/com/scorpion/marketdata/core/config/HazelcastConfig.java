package com.scorpion.marketdata.core.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class HazelcastConfig {

//    @Bean
//    public Config hazelcastConfig() {
//        return new Config().setInstanceName("hazelcast-instance")
//                .addMapConfig(new MapConfig().setName("market-data-cache")
//                        .setTimeToLiveSeconds(3600));
//    }
}
