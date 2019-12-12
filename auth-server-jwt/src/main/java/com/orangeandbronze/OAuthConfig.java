package com.orangeandbronze;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

@Configuration
public class OAuthConfig extends AuthorizationServerConfigurerAdapter {

	@Value("${jwt.keystore.classPathResource:'jwt.jks'}")
	private String keyStoreClassPathResource;
	@Value("${jwt.keystore.password:''}")
	private String keyStorePassword;
	@Value("${jwt.keystore.keyPair:'jwt'}")
	private String keyStoreKeyPair;
	@Value("${jwt.keystore.keyPair.password:'jwt'}")
	private String keyStoreKeyPairPassword;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Bean
	TokenStore jwtTokenStore() {
		return new JwtTokenStore(jwtTokenEnhancer());
	}

	/**
	 * Converts the token to JWT (JSON Web Token).
	 * @return a JWT token converter
	 */
	@Bean
	JwtAccessTokenConverter jwtTokenEnhancer() {
		JwtAccessTokenConverter converter = new JwtAccessTokenConverter();

		// when using symmetric encryption
		// converter.setSigningKey(...);

		// we opted to use asymmetric (public-private) encryption
		// the public-private key-pair is stored in a file (key store)
		KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(
				new ClassPathResource(keyStoreClassPathResource), keyStorePassword.toCharArray());
		converter.setKeyPair(keyStoreKeyFactory.getKeyPair(
				keyStoreKeyPair, keyStoreKeyPairPassword.toCharArray()));
		return converter;
	}

	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		endpoints.tokenStore(jwtTokenStore())
			.tokenEnhancer(jwtTokenEnhancer())
			.authenticationManager(authenticationManager);
	}

	@Override
	public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
		// @formatter:off
		security
			.tokenKeyAccess("permitAll()")
				// allow anyone to get public key (since we're using public-private encryption)
				// public key can be retrieved from "/oauth/token_key"
			.checkTokenAccess("isAuthenticated()")
				// only allow authenticated ones to decode the now encrypted access token
		;
		// @formatter:on
	}

	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		// @formatter:off
		clients.inMemory()
			.withClient("acme")
				.secret("acmesecret")
				.authorizedGrantTypes("authorization_code", "refresh_token", "password")
				.scopes("openid");
		// @formatter:on
	}

}
