package com.orangeandbronze.gateway;

import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableOAuth2Sso
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	public void configure(WebSecurity web) throws Exception {
		// Allow access to CSS and UI library
		web.ignoring().antMatchers("/pui-v1.10.0/**", "/app.css");
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// Allow access to home page
		http.authorizeRequests().antMatchers("/").permitAll()
			.and().authorizeRequests().anyRequest().authenticated();
	}

}
