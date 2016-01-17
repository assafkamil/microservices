package com.samples.microservices.monitoring;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MonitoringConfiguration {
    @Bean
    public AmazonCloudWatch amazonCloudWatch() {
        return new AmazonCloudWatchClient();
    }
}
