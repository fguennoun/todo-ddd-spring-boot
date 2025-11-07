package com.example.todo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Todo DDD Reference Application
 *
 * This application demonstrates professional Spring Boot development
 * with Domain-Driven Design (DDD) architecture, including:
 *
 * - Complete DDD layers (Domain, Application, Infrastructure, Presentation)
 * - Security with JWT authentication
 * - Comprehensive testing strategy
 * - Observability and monitoring
 * - Docker containerization
 * - CI/CD pipeline
 *
 * @author Todo Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableAsync
@EnableTransactionManagement
public class TodoApplication {

    public static void main(String[] args) {
        SpringApplication.run(TodoApplication.class, args);
    }
}
