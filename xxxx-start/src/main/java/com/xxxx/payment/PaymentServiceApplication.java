package com.xxxx.payment;

import com.xxxx.ddd.controller.http.PaymentController;
import com.xxxx.ddd.domain.model.entity.PaymentTransaction;
import com.xxxx.ddd.infrastructure.persistence.mapper.PaymentJPAMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableScheduling
@ComponentScan(
    basePackages = {
        "com.xxxx.ddd.application.service.payment",
        "com.xxxx.ddd.application.mapper",
        "com.xxxx.ddd.domain.service.impl",
        "com.xxxx.ddd.infrastructure",
        "com.xxxx.ddd.controller.config"
    },
    includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        PaymentController.class
    })
)
@EntityScan(
    basePackageClasses = {
        PaymentTransaction.class
    }
)
@EnableJpaRepositories(
    basePackages = "com.xxxx.ddd.infrastructure.persistence.mapper",
    includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        PaymentJPAMapper.class
    })
)
public class PaymentServiceApplication {
    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "payment");
        SpringApplication.run(PaymentServiceApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
