package com.arplanet.adlappnmns;


import ch.qos.logback.classic.LoggerContext;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@Slf4j
@EnableScheduling
@PropertySource("classpath:git.properties")
public class AdlAppsNmnsApplication {
	public static void main(String[] args) {
		SpringApplication.run(AdlAppsNmnsApplication.class, args);
	}
}
