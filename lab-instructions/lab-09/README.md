# Spring Cloud Security with OAuth 2

Estimated Time: 45 minutes

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

### Setup `auth-server`

An external authorization server (OAuth 2) could be used (e.g. Google, Facebook, GitHub). But for this exercise, a simple authorization server will be created. Creating one with Spring Boot is easy.

1) Review the `$SPRING_CLOUD_LABS_HOME/auth-server/pom.xml` file. By adding `spring-security-oauth2` to the classpath, this application can use `@EnableAuthorizationServer`.

```xml
<dependency>
    <groupId>org.springframework.security.oauth</groupId>
    <artifactId>spring-security-oauth2</artifactId>
</dependency>

```

2) Review the following file: `$SPRING_CLOUD_LABS_HOME/auth-server/src/main/java/com/orangeandbronze/AuthSererApplication.java`.  Note the use of the `@EnableAuthorizationServer` annotation. This makes the application an authorization server (in OAuth 2). It configures `/oauth/authorize` and `/oauth/token` endpoints.

```java
@SpringBootApplication
@EnableAuthorizationServer
@EnableResourceServer
@RestController
public class AuthServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServerApplication.class, args);
    }

    @GetMapping("/user")
    public Principal user(Principal user) {
        return user;
    }

    @Configuration
    protected static class AuthenticationManagerConfiguration extends GlobalAuthenticationConfigurerAdapter {

        @Override
        public void init(AuthenticationManagerBuilder auth) throws Exception {
            auth.inMemoryAuthentication()
                    .withUser("user").password("secret").roles("USER", "ADMIN");
        }

    }

}

```

Notice that a URI "/user" is provided (via `@RestController` and `@GetMapping` annotations). This returns the currently authenticated user. This URI needs to be a protected/secured resource. So, the `@EnableResourceServer` annotation is added. This application is not just an *authorization server*, but also a *resource server* (see OAuth 2 roles).

This simplistic authorization server has one user account (username: user, password: secret).

3) Review the `$SPRING_CLOUD_LABS_HOME/auth-server/src/main/resources/application.properties` file.

```
security.oauth2.client.clientId: acme
security.oauth2.client.clientSecret: acmesecret
security.oauth2.client.authorized-grant-types: authorization_code,refresh_token,password
security.oauth2.client.scope: openid
```

The properties further customize the `@EnableAuthorizationServer` by configuring an in-memory client details service that has a single client `acme` registered (with `acmesecret` as its secret). This is obviously not realistic because it only has one client. Nevertheless, this will work for our exercise. Great for demos too!

4) Open a new terminal window.  Start the `auth-server` app.

```bash
$ cd $SPRING_CLOUD_LABS_HOME/auth-server
$ mvn clean spring-boot:run
```

To test the authorization server execute a simple POST to the /uaa/oauth/token endpoint, you will need to set the following headers:

1) Authorization : Basic + a base64 encoded string of the clientId:clientSecret, on this example it will be acme:acmesecret

2) Content-Type : application/x-www-form-urlencoded

3) Payload: grant_type=password&username=<username>&password=<password>

```bash
curl -XPOST -H "Content-Type: application/x-www-form-urlencoded" \
            -H "Authorization: Basic YWNtZTphY21lc2VjcmV0" \
             http://localhost:9999/uaa/oauth/token \
             -d "grant_type=password&username=user&password=secret"
```

You should get back an authentication token (like the one below).

```json
{
   "access_token":"661aac97-55ca-49a0-b8b6-a4a1d8cb63de",
   "token_type":"bearer",
   "refresh_token":"9a605803-4013-4818-ae24-22de7b399018",
   "expires_in":43199,
   "scope":"openid"
}
```


### Setup `fortune-secured`

This is a similar service as `fortune-service`. The only difference is that it is secured.

1) Review the `$SPRING_CLOUD_LABS_HOME/fortune-secured/pom.xml` file. By adding `spring-cloud-starter-security` and `spring-security-oauth2` to the classpath this application can use `@EnableResourceServer`.

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.security.oauth</groupId>
    <artifactId>spring-security-oauth2</artifactId>
</dependency>
```

2) Review the following file: `$SPRING_CLOUD_LABS_HOME/fortune-secured/src/main/java/com/orangeandbronze/FortuneServiceApplication.java`.  Note the use of the `@EnableResourceServer` annotation. This creates a resource server application (an OAuth 2 resource server).


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

3) Review the following file: `$SPRING_CLOUD_LABS_HOME/fortune-secured/src/main/resources/application.yml`. It contains an entry for OAuth 2 user-info-uri.

```yml
security:
    oauth2:
        resource:
            userInfoUri: http://localhost:9999/uaa/user # <-- on auth-server

```

4) Open a new terminal window.  Start the `fortune-secured` service.

```bash
$ cd $SPRING_CLOUD_LABS_HOME/fortune-secured
$ mvn clean spring-boot:run
```

5) After a few moments, access the endpoint at `http://locahost:8787` and you get back an unauthorized response:

```xml
<oauth>
  <error_description>
  Full authentication is required to access this resource
  </error_description>
  <error>unauthorized</error>
</oauth>

```

***What Just Happened?***

The `@EnableResourceServer` secured the `fortune-service`. A security filter is configured to check for a token that will be used to retrieve information about the user. Remember the `security.oauth2.resource.userInfoUri` that was set? That is the URI that will be used to retrieve the user information. This URI is currently served by the `auth-server`, and it requires an authenticated user.

As of the moment, when `http://localhost:8787` was accessed, no token was provided. Thus, an "unauthorized" error is returned.


6) To gain access, let's obtain a valid token from the `auth-server`. Remember how you tested the authorization server in the previous exercise?

```bash
curl -XPOST -H "Content-Type: application/x-www-form-urlencoded" \
            -H "Authorization: Basic YWNtZTphY21lc2VjcmV0" \
             http://localhost:9999/uaa/oauth/token \
             -d "grant_type=password&username=user&password=secret"
```

You should get back an authentication token (like the one below).

```json
{
   "access_token":"661aac97-55ca-49a0-b8b6-a4a1d8cb63de",
   "token_type":"bearer",
   "refresh_token":"9a605803-4013-4818-ae24-22de7b399018",
   "expires_in":43199,
   "scope":"openid"
}
```

Grab the `access_token` from that result and now invoke the following command to call your service, replace the `Authorization` header with the proper token from the previous step:

```bash
curl -XGET -H "Authorization: bearer 5d04666c-11c2-4a4c-9dae-f0def0acc9c1" http://localhost:8787/
```

This is all that is needed to protect a resource to require oAuth2 tokens to be accessed.


### Setup `gateway-secured`

1) Review the `$SPRING_CLOUD_LABS_HOME/gateway-secured/pom.xml` file. By adding `spring-security-oauth2`, `OAuth2ProxyAutoConfiguration` becomes available. This configures token relay. Since the app acts as a gateway (thanks to `@EnableZuulProxy`), it needs to be able to relay any valid tokens to downstream services (like `fortune-service`).

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.security.oauth</groupId>
    <artifactId>spring-security-oauth2</artifactId>
</dependency>
```

2) Review the `$SPRING_CLOUD_LABS_HOME/gateway-secured/src/main/java/com/orangeandbronze/gateway/GatewayApplication.java`. The `@EnableOAuth2Sso` configures the application for SSO (single sign-on). It configures an `OAuth2ClientAuthenticationProcessingFilter` among other things.

All incoming requests must be authenticated. If not yet authenticated, the `OAuth2ClientAuthenticationProcessingFilter` initiates an OAuth authorization flow. It can redirect the browser to a designated authorization server. The user can then authenticate and authorize access to his/her user account. In doing so, the client gets a valid token.

```java
@SpringBootApplication
@EnableDiscoveryClient
@EnableZuulProxy
@EnableOAuth2Sso
public class GatewayApplication {

    public static void main(String... args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

}
```

3) Review the $SPRING_CLOUD_LABS_HOME/gateway-secured/src/main/resources/application.yml file.

```yml
security:
  oauth2:    
    client:
      clientId: acme
      clientSecret: acmesecret
      userAuthorizationUri: http://localhost:9999/uaa/oauth/authorize
      accessTokenUri: http://localhost:9999/uaa/oauth/token
    resource:
      userInfoUri: http://localhost:9999/uaa/user

```

These properties enable the gateway app to act as an OAuth 2 client. It will try to get a valid token from the authorization server with the user's (a.k.a. resource owner's) permission, of course!

As mentioned, a 3rd-party authorization server like Google can be used. Here's how the properties can look like:

```yml
security:
  oauth2:    
    client:
      clientId: # client-id obtained from authorization server
      clientSecret: # client-secret obtained from authorization server
      userAuthorizationUri: https://accounts.google.com/o/oauth2/v2/auth
      accessTokenUri: https://www.googleapis.com/oauth2/v4/token
    resource:
      userInfoUri: https://www.googleapis.com/oauth2/v3/userinfo

```

Please refer to the 3rd-party's documentation. Like [here](https://developers.google.com/identity/protocols/OpenIDConnect) and [here](https://developers.google.com/identity/protocols/OAuth2WebServer) for Google. The URIs of the authorization, token, and userinfo endpoints may be retrieved from the [Discovery document for Google's OpenID Connect](https://accounts.google.com/.well-known/openid-configuration) service.

Here's how the properties can look like when using Facebook as external authorization server.

```yml
security:
  oauth2:
    client:
      clientId: # obtained from authorization server
      clientSecret: # obtained from authorization server
      userAuthorizationUri: https://www.facebook.com/dialog/oauth
      accessTokenUri: https://graph.facebook.com/oauth/access_token
      tokenName: oauth_token
      authenticationScheme: query
      clientAuthenticationScheme: form
    resource:
      userInfoUri: https://graph.facebook.com/me
```

4) Open a new terminal window.  Start the `gateway-secured` app.

```bash
$ cd $SPRING_CLOUD_LABS_HOME/gateway-secured
$ mvn clean spring-boot:run
```

5) After a few moments, access `http://localhost:8790/fortune`. The gateway application would authenticate, authorize, and forward the request to `fortune-service`.

6) When done, stop the `config-server`, `service-registry`, `fortune-service`, `gateway-app`, and `auth-server` applications.

***What Just Happened?***

The `gateway-app` (now with `@EnableOAuth2Sso`) receives the request when `http://localhost:8790/fortune` was accessed. It checked if the incoming request was authenticated, by looking for the authorization header. If not present, it will proceed to request for one from the authorization server (`auth-server`). This will redirect the user to a login page on the authorization server (unless the user has already been logged-in). Next, the authorization server will ask the user to either allow or deny access to his/her user account (which will be used to access a protected resource). When the user allows access, the authorization server redirects back to the client (in this case, the `gateway-app` which is now secured) with an authorization code. The client then exchanges the authorization code for an access token. This token will then be used as the authorization header for downstream requests.

The downstream service (`fortune-service` in this case) uses the authorization header to get the authenticated user information from the authorization server (via `security.oauth2.resource.userInfoUri`). Again, the authorization server looks for the authorization header. This time, it finds one. Checks that the token is valid, and returns the user information. The service allows access and returns a fortune.

