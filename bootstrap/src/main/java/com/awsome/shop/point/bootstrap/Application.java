package com.awsome.shop.point.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * AIOps Service Application Entry Point
 *
 * @author catface996
 * @since 2025-11-21
 */
@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = "com.awsome.shop.point")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
