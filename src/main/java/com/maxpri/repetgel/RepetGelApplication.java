package com.maxpri.repetgel;

import com.maxpri.repetgel.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
@EnableScheduling
@EnableJpaAuditing
public class RepetGelApplication {

    public static void main(String[] args) {
        SpringApplication.run(RepetGelApplication.class, args);
    }

}