package com.xxxx.ticket;

import com.xxxx.ddd.controller.http.TicketController;
import com.xxxx.ddd.controller.http.TicketDetailController;
import com.xxxx.ddd.controller.http.HiController;
import com.xxxx.ddd.domain.model.entity.Ticket;
import com.xxxx.ddd.domain.model.entity.TicketDetail;
import com.xxxx.ddd.infrastructure.persistence.mapper.TicketJPAMapper;
import com.xxxx.ddd.infrastructure.persistence.mapper.TicketDetailJPAMapper;
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
        "com.xxxx.ddd.application.service.ticket",
        "com.xxxx.ddd.application.mapper",
        "com.xxxx.ddd.domain.service.impl",
        "com.xxxx.ddd.infrastructure",
        "com.xxxx.ddd.controller.config"
    },
    includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        TicketController.class,
        TicketDetailController.class,
        HiController.class
    })
)
@EntityScan(
    basePackageClasses = {
        Ticket.class,
        TicketDetail.class
    }
)
@EnableJpaRepositories(
    basePackages = "com.xxxx.ddd.infrastructure.persistence.mapper",
    includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        TicketJPAMapper.class,
        TicketDetailJPAMapper.class
    })
)
public class TicketServiceApplication {
    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "ticket");
        SpringApplication.run(TicketServiceApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
