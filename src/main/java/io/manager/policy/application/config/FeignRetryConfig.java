package io.manager.policy.application.config;

import feign.Retryer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignRetryConfig {

    @Value("${spring.cloud.openfeign.client.config.default.delay:100}")
    private Integer delay;

    @Value("${spring.cloud.openfeign.client.config.default.maxAttempts:3}")
    private Integer maxAttempts;

    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(delay, delay, maxAttempts);
    }

}