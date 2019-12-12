# Spring Cloud Security with OAuth 2 and JWT

Estimated Time: 25 minutes

## Exercises

### Start the  `config-server`,  `service-registry`

1) Start the `config-server` in a terminal window.  You may have a terminal windows still open from previous labs.  They may be reused for this lab.

```bash
$ cd $SPRING_CLOUD_LABS_HOME/config-server
$ mvn clean spring-boot:run
```

2) Start the `service-registry`

```bash
$ cd $SPRING_CLOUD_LABS_HOME/service-registry
$ mvn clean spring-boot:run
```

### Setup `auth-server-jwt`

An external authorization server (OAuth 2) could be used (e.g. Google, Facebook, GitHub). But for this exercise, we use a similar authorization server from the previous lab exercise.

1) Review the `$SPRING_CLOUD_LABS_HOME/auth-server-jwt/pom.xml` file. By adding `spring-security-jwt` to the classpath, this application can use JSON Web Tokens (JWT).

```xml
<dependency>
    <groupId>org.springframework.security.oauth</groupId>
    <artifactId>spring-security-oauth2</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-jwt</artifactId>
</dependency>

```

2) Review the following file: `$SPRING_CLOUD_LABS_HOME/auth-server-jwt/src/main/java/com/orangeandbronze/OAuthConfig.java`.  It configures a JWT token enhancer and token store.

```java
@Configuration
public class OAuthConfig extends AuthorizationServerConfigurerAdapter {
  ...
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
    security
      .tokenKeyAccess("permitAll()")
        // allow anyone to get public key (since we're using public-private encryption)
        // public key can be retrieved from "/oauth/token_key"
      .checkTokenAccess("isAuthenticated()")
        // only allow authenticated ones to decode the now encrypted access token
    ;
  }

  ...
}

```

The configuration is a `AuthorizationServerConfigurer`. It overrides a method to customize `AuthorizationServerEndpointsConfigurer`. The endpoints are made to use the JWT-capable token store and token enhancer. This exercise will use a public-private key pair.

3) Review the `$SPRING_CLOUD_LABS_HOME/auth-server-jwt/src/main/resources/application.properties` file. Properties were added to configure the keystore that will be used.

4) Use the `keytool` to create a key pair and store them in a key store.

```bash
keytool -genkeypair -keyalg RSA \
    -keystore <jks-file-name> -storepass <jks-file-password>
    -alias <key-pair-name> -keypass <key-pair-password>
    -dname "..."
```

Place the generated file in `auth-server-jwt/src/main/resources`. Adjust `application.properties` to match the key pair that was generated.

For example,

```bash
keytool -genkeypair -keyalg RSA \
    -keystore jwt.jks -storepass secret \
    -alias jwt -keypass secret \
    -dname "CN=jwt"
```

```
jwt.keystore.classPathResource=jwt.jks
jwt.keystore.password=secret
jwt.keystore.keyPair=jwt
jwt.keystore.keyPair.password=secret
```

5) Open a new terminal window.  Start the `auth-server-jwt` app.

```bash
$ cd $SPRING_CLOUD_LABS_HOME/auth-server-jwt
$ mvn clean spring-boot:run
```

To test the authorization server execute a simple POST to the /uaa/oauth/token endpoint, you will need to set the following headers:

i) Authorization : Basic + a base64 encoded string of the clientId:clientSecret, on this example it will be acme:acmesecret
ii) Content-Type : application/x-www-form-urlencoded
iii) Payload: grant_type=password&username=<username>&password=<password>

```bash
curl -XPOST -H "Content-Type: application/x-www-form-urlencoded" \
            -H "Authorization: Basic YWNtZTphY21lc2VjcmV0" \
             http://localhost:9999/uaa/oauth/token \
             -d "grant_type=password&username=user&password=secret"
```

You should get back an authentication token (like the one below). Notice that the access token is much longer. It is now encrypted using a private key.

```json
{
   "access_token":"eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzY29wZSI6WyJyZXNvdXJjZS1zZXJ2ZXItcmVhZCIsInJlc291cmNlLXNlcnZlci13cml0ZSJdLCJleHAiOjE0NzU3NTE0NzYsImF1dGhvcml0aWVzIjpbIlJPTEVfUlNfUkVBRCJdLCJqdGkiOiJkOGI1NTc1MS01YzJkLTRhNjItYmFlMy0yYjM0YTNjMzQ0NDkiLCJjbGllbnRfaWQiOiJzZXJ2aWNlLWFjY291bnQtMSJ9.d5eP533cYORNBt73vbXRSPowOefWvysoBr2lkazhcEjIK6wTRDv9-uO4Bi6CmRW6sBqo8ijiyPHBo596cyZpg6O94vRfI4FnFuqi9qzPc8B6CSeMoWJNf7g6sJUsK1jrTZBs8_84MBmy2nDxC8DEYkOqwsBvh0FX9wOd3pLTlgl5_sh63D1E2RJsGhskYJb4ql9LZTuBI7KWV0MMYHTZ1QeaOWLMpnbalid5TSERHOsTMKgQNrJTC8ioet_lQJnXTbYIk2VkINyFX80-RIobN4djlzs8oLEbkHWRT4t_O5vbc56AyvOaQZTPM8_C96VMLIOTuOrzP3rC3t7x7qp90A",
   "token_type":"bearer",
   "refresh_token":"...",
   "expires_in":43199,
   "scope":"openid"
}
```

### Setup `fortune-secured-jwt`

This is a similar service as the one in `fortune-secured` project. The only difference is that it is using JWT.

1) Review the `$SPRING_CLOUD_LABS_HOME/fortune-secured-jwt/pom.xml` file. By adding `spring-security-jwt` it can expect a JWT (not just an access token).

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.security.oauth</groupId>
    <artifactId>spring-security-oauth2</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-jwt</artifactId>
</dependency>
```

2) Review the following file: `$SPRING_CLOUD_LABS_HOME/fortune-secured-jwt/src/main/java/com/orangeandbronze/FortuneServiceApplication.java`. No changes here. The changes are all in the configuration.


```java
@SpringBootApplication
@EnableResourceServer
@EnableDiscoveryClient
public class FortuneServiceApplication {

    public static void main(String... args) {
        SpringApplication.run(FortuneServiceApplication.class, args);
    }

}

```

3) Review the following file: `$SPRING_CLOUD_LABS_HOME/fortune-secured-jwt/src/main/resources/application.yml`. Instead of a user-info-uri, it now is configured with a URI that returns the public key that will be used to decode the JWT.

```yml
security:
    oauth2:
        resource:
#            userInfoUri: http://localhost:9999/uaa/user
            jwt:
                key-uri: http://localhost:9999/uaa/oauth/token_key

```

4) Open a new terminal window.  Start the `fortune-secured-jwt` service.

```bash
$ cd $SPRING_CLOUD_LABS_HOME/fortune-secured-jwt
$ mvn clean spring-boot:run
```

5) After a few moments, access the endpoint at `http://locahost:8787` and you still get back an unauthorized response:

```xml
<oauth>
  <error_description>
  Full authentication is required to access this resource
  </error_description>
  <error>unauthorized</error>
</oauth>

```

***What Just Happened?***

The `@EnableResourceServer` secured the `fortune-service`. A security filter is configured to check for a token that will be used to retrieve information about the user. Remember the `security.oauth2.resource.jwt.keyUri` that was set? That is the URI that will be used to retrieve the public key to convert the access token to JSON web token (JWT). This URI is currently served by the `auth-server`, and it requires an authenticated user.

As of the moment, when `http://localhost:8787` was accessed, no token was provided. Thus, an "unauthorized" error is returned.


6) To gain access, let's obtain a valid token from the `auth-server-jwt`. Remember how you tested the authorization server in the previous exercise?

```bash
curl -XPOST -H "Content-Type: application/x-www-form-urlencoded" \
            -H "Authorization: Basic YWNtZTphY21lc2VjcmV0" \
             http://localhost:9999/uaa/oauth/token \
             -d "grant_type=password&username=user&password=secret"
```

You should get back an authentication token (like the one below). The access token can be quite long.

```json
{
   "access_token":"eyJhbGci&hellip;p90A",
   "token_type":"bearer",
   "refresh_token":"&hellip;",
   "expires_in":43199,
   "scope":"openid"
}
```

Grab the `access_token` from that result and now invoke the following command to call your service, replace the `Authorization` header with the proper token from the previous step:

```bash
export TOKEN="eyJhbGci&hellip;p90A"
curl -XGET -H "Authorization: bearer $TOKEN" http://localhost:8787/
```

This is all that is needed to protect a resource using oAuth2 with JWT.


### Setup `gateway-secured-jwt`

This is similar to `gateway-secured` from another exercise. This time, the gateway will also serve an unsecured HTML page and a secured HTML page that consumes a back-end service (`fortune-secured-jwt`).

1) Review the `$SPRING_CLOUD_LABS_HOME/gateway-secured-jwt/pom.xml` file. By adding `spring-security-jwt`, it can recognize JWT (JSON Web Tokens).

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.security.oauth</groupId>
    <artifactId>spring-security-oauth2</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-jwt</artifactId>
</dependency>
```

2) Review the `$SPRING_CLOUD_LABS_HOME/gateway-secured-jwt/src/main/java/com/orangeandbronze/gateway/GatewayApplication.java`. The `@EnableOAuth2Sso` has been moved to a `WebSecurityConfigurer` to customize the web-based security and allow access to the home page, and require authenticated access for the rest.

```java
@SpringBootApplication
@EnableDiscoveryClient
@EnableZuulProxy
// @EnableOAuth2Sso <-- moved to a WebSecurityConfigurer
public class GatewayApplication {

    public static void main(String... args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

}

```

```java
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
    http
      .authorizeRequests()
        .antMatchers("/").permitAll()
      .authorizeRequests()
        .anyRequest().authenticated();
  }

}

```


3) Review the $SPRING_CLOUD_LABS_HOME/gateway-secured-jwt/src/main/resources/application.yml file. Similar to the `fortune-service`, instead of a `user-info-uri`, it is now configured to get the public key to decode the JWT.

```yml
security:
  oauth2:    
    client:
      clientId: acme
      clientSecret: acmesecret
      userAuthorizationUri: http://localhost:9999/uaa/oauth/authorize
      accessTokenUri: http://localhost:9999/uaa/oauth/token
    resource:
#      userInfoUri: http://localhost:9999/uaa/user
      jwt:
        key-uri: http://localhost:9999/uaa/oauth/token_key

```

These properties enable the gateway app to act as an OAuth 2 client. It will try to get a valid token from the authorization server with the user's (a.k.a. resource owner's) permission, of course!

4) Open a new terminal window.  Start the `gateway-secured-jwt` app.

```bash
$ cd $SPRING_CLOUD_LABS_HOME/gateway-secured-jwt
$ mvn clean spring-boot:run
```

5) After a few moments, access `http://localhost:8790/`. The gateway application would return a home page. Since this resource does not require authorized access, it does not require the user to authenticate and authorize. Next, access `http://localhost:8790/greeting`. This time, the resource requires authorized access. The system redirects the user to authenticate (if the user has not done so yet), authorize, and get back to the gateway app which then forwards a request to `fortune-service` with a JWT.

6) When done, stop the `config-server`, `service-registry`, `fortune-service`, `gateway-app`, and `auth-server` applications.

***What Just Happened?***

The `gateway-app` receives the request when `http://localhost:8790/` was accessed. Since the request does not require authorized access, the home page is simply rendered.

When `http://localhost:8790/greeting` was accessed, it looks for the authorization header, since the requested resource requires authorized access. If not present, it will proceed to request for one from the authorization server (`auth-server`). This will redirect the user to a login page on the authorization server (unless the user has already been logged-in). Next, the authorization server will ask the user to either allow or deny access to his/her user account (which will be used to access a protected resource). When the user allows access, the authorization server redirects back to the client (in this case, the `gateway-app`) with an authorization code. The client then exchanges the authorization code for an access token (now a JWT). This token will then be used as the authorization header for downstream requests.

The downstream service (`fortune-service` in this case) uses the authorization header to get the authenticated user information. But this time, the header is a JWT which already contains user information.

When the `fortune-service` started, it retrieved the public key using the URI `security.oauth2.resource.jwt.keyUri`. It uses the public key to convert the access token to a JSON web token (JWT), and the user information contained within is used to allow/deny acess. Note that with JWT, the `fortune-service` (and other downstream services) no longer needs to access the authorization server for each request. The needed user information is already present in the authorization header. Thanks to the JWT standard, it is now safely sent with the request.

Note that the *gateway* behaves much like a secured web-app (using Spring Security). It keeps the user authentication in session (i.e. stores `SecurityContext` between requests). However, the downstream services (e.g. annotated with `@EnableResourceServer`) do not keep the authentication in session. Without JWT, the access token needs to be used to get user information from the authorization server for each request. This can affect performance. Now with JWT, the downstream services no longer need to get user information from the authorization server. The downstream service simply converts the token to JSON using the provided public key.

### Enhance `gateway-secured-jwt` with declarative REST client and circuit-breaker fallback

As of the moment, it is using an `OAuth2RestTemplate` to call downstream services (like the `fortune-service`). We will now enhance it to use a declarative REST client (Feign) with circuit-breaker and fallback (Hystrix) while keeping it secured.

Follow the `TODO` instructions in `gateway-secured-jwt`. After completing the changes, re-run the gateway, and test that everything is working. It should still be able to access the fortune service.
