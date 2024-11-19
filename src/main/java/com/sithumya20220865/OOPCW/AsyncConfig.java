package com.sithumya20220865.OOPCW;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);   // Minimum number of threads
        executor.setMaxPoolSize(20);    // Maximum number of threads
        executor.setQueueCapacity(100); // Queue capacity for waiting tasks
        executor.setThreadNamePrefix("async-"); // Prefix for thread names
        executor.initialize();
        return executor;
    }
}
