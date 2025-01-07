package com.nowait.config;

import jakarta.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ThreadPoolConfig {

    @Bean
    public ExecutorService cachedThreadPool() {
        return Executors.newCachedThreadPool();
    }

    @PreDestroy
    public void shutdownThreadPool() {
        if (cachedThreadPool() != null && !cachedThreadPool().isShutdown()) {
            cachedThreadPool().shutdown();
        }
    }
}
