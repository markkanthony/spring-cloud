package com.orangeandbronze;

import java.security.Principal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

/**
 * <p>This is the authorization server (OAuth 2.0). It provides
 * user account and authentication (or UAA for short).
 * </p><p>
 * This can be replaced with an external authorization
 * server with providers like Facebook, Google, Github, and others.
 * </p>
 */
@SpringBootApplication
@EnableAuthorizationServer
@RestController
@EnableResourceServer
public class AuthServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthServerApplication.class, args);
	}

	/*
	 * Instead of creating another controller class, we made
	 * this class a REST controller (see RestController annotation
	 * on this class). We also added the EnableResourceServer
	 * annotation, which tells Spring to authenticate requests
	 * to this REST controller.
	 */
    @GetMapping("/user")
    public Principal user(Principal user) {
    	return user;
    }

    @Configuration
    public static class AuthenticationManagerConfig
    		extends GlobalAuthenticationConfigurerAdapter {
    	@Override
    	public void init(AuthenticationManagerBuilder auth) throws Exception {
            auth.inMemoryAuthentication()
            	.withUser("user").password("secret").roles("USER", "ADMIN");
    	}

    }

    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter crlf = new CommonsRequestLoggingFilter();
        crlf.setIncludeClientInfo(true);
        crlf.setIncludeQueryString(true);
        crlf.setIncludePayload(true);
        return crlf;
    }

}
