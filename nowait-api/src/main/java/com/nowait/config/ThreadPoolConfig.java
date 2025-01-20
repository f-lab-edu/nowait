package com.nowait.config;

import jakarta.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ThreadPoolConfig {

    @Bean
    public ExecutorService threadPool() {
        return Executors.newCachedThreadPool();
    }

    @PreDestroy
    public void gracefulShutdown() {
        if (threadPool() != null && !threadPool().isShutdown()) {
            threadPool().shutdown();
            try {
                if (!threadPool().awaitTermination(30, TimeUnit.SECONDS)) {
                    threadPool().shutdownNow();
                }
            } catch (InterruptedException e) {
                threadPool().shutdownNow();
            }
        }
    }
}
