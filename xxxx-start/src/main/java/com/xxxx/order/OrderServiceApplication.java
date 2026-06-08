package com.xxxx.order;

import com.xxxx.ddd.controller.http.TicketOrderController;
import com.xxxx.ddd.controller.http.BookingController;
import com.xxxx.ddd.controller.http.OrderMQController;
import com.xxxx.ddd.domain.model.entity.TickerOrder;
import com.xxxx.ddd.domain.model.entity.OrderQueue;
import com.xxxx.ddd.domain.model.entity.Booking;
import com.xxxx.ddd.infrastructure.persistence.mapper.OrderQueueJPAMapper;
import com.xxxx.ddd.infrastructure.persistence.mapper.TicketOrderJPAMapper;
import com.xxxx.ddd.infrastructure.persistence.mapper.BookingJPAMapper;
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
        "com.xxxx.ddd.application.service.order",
        "com.xxxx.ddd.application.service.booking",
        "com.xxxx.ddd.application.service.placeorder",
        "com.xxxx.ddd.application.mapper",
        "com.xxxx.ddd.domain.service.impl",
        "com.xxxx.ddd.infrastructure",
        "com.xxxx.ddd.controller.config"
    },
    includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        TicketOrderController.class,
        BookingController.class,
        OrderMQController.class
    })
)
@EntityScan(
    basePackageClasses = {
        TickerOrder.class,
        OrderQueue.class,
        Booking.class
    }
)
@EnableJpaRepositories(
    basePackages = "com.xxxx.ddd.infrastructure.persistence.mapper",
    includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        OrderQueueJPAMapper.class,
        TicketOrderJPAMapper.class,
        BookingJPAMapper.class
    })
)
public class OrderServiceApplication {
    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "order");
        SpringApplication.run(OrderServiceApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
