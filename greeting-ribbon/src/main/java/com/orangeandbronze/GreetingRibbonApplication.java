package com.orangeandbronze;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableDiscoveryClient
public class GreetingRibbonApplication {

    public static void main(String[] args) {
        SpringApplication.run(GreetingRibbonApplication.class, args);
    }

    // TODO 01: Define a RestTemplate bean
    // TODO 02: Annotate it as @LoadBalanced
    @Bean
    @LoadBalanced
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
