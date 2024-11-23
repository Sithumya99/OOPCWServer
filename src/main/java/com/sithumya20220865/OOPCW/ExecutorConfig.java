package com.sithumya20220865.OOPCW;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorConfig {

    @Bean
    public Executor taskExecutor() {
        return new DelegatingSecurityContextExecutor(
                Executors.newFixedThreadPool(10)
        );
    }
}
