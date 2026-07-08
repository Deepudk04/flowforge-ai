package com.flowforge;

import com.flowforge.config.FlowForgeProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(FlowForgeProperties.class)
public class FlowForgeApplication {
    public static void main(String[] args) {
        SpringApplication.run(FlowForgeApplication.class, args);
    }
}