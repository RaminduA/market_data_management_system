package com.scorpion.marketdata.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class MarketDataCoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(MarketDataCoreApplication.class, args);
	}

}
