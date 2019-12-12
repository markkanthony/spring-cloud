package com.orangeandbronze.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;

// TODO 01d: Enable Feign clients (add @EnableFeignClients) 
@SpringBootApplication
@EnableDiscoveryClient
@EnableZuulProxy
// @EnableOAuth2Sso <-- moved to a WebSecurityConfigurerAdapter
public class GatewayApplication {

    public static void main(String... args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @LoadBalanced
    @Bean
    public OAuth2RestTemplate restTemplate(
    		OAuth2ClientContext oauth2ClientContext,
    		OAuth2ProtectedResourceDetails resource) {
    	return new OAuth2RestTemplate(resource, oauth2ClientContext);
    }

    // TODO 01e: Uncomment the code below to add OAuth2 to the Feign client
    // This enables the Feign client to relay the authorization headers.
    /*
    @Bean
    public RequestInterceptor oauth2FeignRequestInterceptor(
    		OAuth2ClientContext oauth2ClientContext,
    		OAuth2ProtectedResourceDetails resource){
        return new OAuth2FeignRequestInterceptor(
        		oauth2ClientContext, resource);
    }
    */

}
