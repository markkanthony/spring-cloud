# Spring Cloud Netflix (Zuul): Gateway API (a.k.a. Edge Server)

Estimated Time: 25 minutes

## Exercises

### Start the  `config-server`,  `service-registry`, and `fortune-service`

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

3) Start the `fortune-service`

```bash
$ cd $SPRING_CLOUD_LABS_HOME/fortune-service
$ mvn clean spring-boot:run
```

### Setup `gateway-app`

1) Review the $SPRING_CLOUD_LABS_HOME/gateway-app/pom.xml file. By adding `spring-cloud-starter-zuul` to the classpath this application acts as a gateway.

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-zuul</artifactId>
</dependency>
```

2) Review the following file: `$SPRING_CLOUD_LABS_HOME/gateway-app/src/main/java/com/orangeandbronze/gateway/GatewayApplication.java`.  Note the use of the `@EnableZuulProxy` annotation. This creates a Zuul proxy application.


```java
@SpringBootApplication
@EnableZuulProxy
@EnableDiscoveryClient
public class GatewayApplication {

    public static void main(String... args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

}

```

3) Review the following file: `$SPRING_CLOUD_LABS_HOME/gateway-app/src/main/resources/application.yml`. It contains entries for Zuul routes.

```yml
zuul:
    routes:
        fortune: # <-- NAME (from request path)
            serviceId: fortune-service

```


4) Open a new terminal window.  Start the `gateway-app` app.

 ```bash
$ cd $SPRING_CLOUD_LABS_HOME/gateway-app
$ mvn clean spring-boot:run
```

5) After the a few moments, check the `service-registry` [dashboard](http://localhost:8761).  Confirm the `gateway-app` app is registered.


### Setup `greeting-frontend`

1) Review the `$SPRING_CLOUD_LABS_HOME/greeting-frontend/pom.xml` file. It has dependencies that make it a configuration client and eureka client (for service discovery).

2) Review the `$SPRING_CLOUD_LABS_HOME/greeting-frontend/src/main/java/com/orangeandbronze/greetingfrontend/HomeController.java`. 

```java
@Controller
public class HomeController {

    private final EurekaClient discoveryClient;

    public HomeController(EurekaClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @GetMapping("/")
    public ModelAndView index() {
        ModelAndView modelAndView = new ModelAndView("index");

        Map<String, Object> model = modelAndView.getModel();
        model.put("apiServerUrl", format("%s/fortune", getGatewayUrl()));

        return modelAndView;
    }

    private String getGatewayUrl() {
        return discoveryClient
            .getNextServerFromEureka("gateway-application", false)
            .getHomePageUrl();
    }

}

```

3) Open a new terminal window.  Start the `greeting-frontend` app.

 ```bash
$ cd $SPRING_CLOUD_LABS_HOME/greeting-frontend
$ mvn clean spring-boot:run
```

4) [Browse](http://localhost:8791/) to the `greeting-frontend` application.  Confirm you are seeing fortunes.  Refresh as desired.  Also review the terminal output for the `fortune-service` app.

5) When done, stop the `config-server`, `service-registry`, `fortune-service`, `gateway-app`, and `greeting-frontend` applications.

***What Just Happened?***

`greeting-frontend` discovered the `gateway-app` application through the `service-registry` application.  It then rendered HTML containing JavaScript that makes Ajax/XHR to the `gateway-app`'s `/fortune`. The `gateway-app` routes this to the `fortune-service`.

