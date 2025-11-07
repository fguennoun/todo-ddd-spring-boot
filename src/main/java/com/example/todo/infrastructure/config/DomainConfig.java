package com.example.todo.infrastructure.config;

import com.example.todo.domain.repository.TodoRepository;
import com.example.todo.domain.service.TodoDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * DomainConfig
 *
 * Configuration des services et composants du domaine.
 *
 * @author Todo Team
 */
@Configuration
@EnableAsync
public class DomainConfig {

    /**
     * Configuration du TodoDomainService
     */
    @Bean
    public TodoDomainService todoDomainService(TodoRepository todoRepository) {
        return new TodoDomainService(todoRepository);
    }

    /**
     * Configuration de l'executor pour les tâches asynchrones
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(10000);
        executor.setThreadNamePrefix("TodoApp-");
        executor.initialize();
        return executor;
    }
}
