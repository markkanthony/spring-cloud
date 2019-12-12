# Spring Cloud Netflix (Ribbon): Client-Side Load Balancing

Estimated Time: 25 minutes

## What You Will Learn

* How to use Ribbon as a client side load balancer
* How to use a Ribbon enabled `RestTemplate`

## Exercises

### Start the `config-server`, `service-registry`, and `fortune-service`

1) Start the `config-server` in a terminal window.  You may have terminal windows still open from previous labs.  They may be reused for this lab.

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

### Set up `greeting-ribbon`

***No additions to the pom.xml***

In this case, we don't need to explicitly include Ribbon support in the `pom.xml`.  Ribbon support is pulled in through transitive dependencies (dependencies of the dependencies we have already defined).

1) Review the the following file: `$SPRING_CLOUD_LABS_HOME/greeting-ribbon/src/main/java/com/orangeandbronze/greeting/GreetingController.java`.  Notice the `loadBalancerClient`.  It is a client side load balancer (Ribbon).  Review the `fetchFortuneServiceUrl()` method.  Ribbon is integrated with Eureka so that it can discover services as well.  Notice how the `loadBalancerClient` chooses a service instance by name.

```java
@Controller
public class GreetingController {

    Logger logger = LoggerFactory
            .getLogger(GreetingController.class);




    @Autowired
    private LoadBalancerClient loadBalancerClient;

    @RequestMapping("/")
    String getGreeting(Model model){

        logger.debug("Adding greeting");
        model.addAttribute("msg", "Greetings!!!");


        RestTemplate restTemplate = new RestTemplate();
        String fortune = restTemplate.getForObject(fetchFortuneServiceUrl(), String.class);

        logger.debug("Adding fortune");
        model.addAttribute("fortune", fortune);

        // resolves to the greeting view
        return "greeting";
    }

    private String fetchFortuneServiceUrl() {
        ServiceInstance instance = loadBalancerClient.choose("fortune-service");

        logger.debug("uri: {}",instance.getUri().toString());
        logger.debug("serviceId: {}", instance.getServiceId());


        return instance.getUri().toString();
    }

}

```

2) Open a new terminal window.  Start the `greeting-ribbon` app.

```bash
$ cd $SPRING_CLOUD_LABS_HOME/greeting-ribbon
$ mvn clean spring-boot:run
```

3) After a few moments, check the `service-registry` [dashboard](http://localhost:8761).  Confirm the `greeting-ribbon` app is registered.


4) [Browse](http://localhost:8080/) to the `greeting-ribbon` application.  Confirm you are seeing fortunes.  Refresh as desired.  Also review the terminal output for the `greeting-ribbon` app.  See the `uri` and `serviceId` being logged.

5) Stop the `greeting-ribbon` application.

### Simplify `greeting-ribbon`

Follow `TODO` instructions to modify `GreetingController` to have a `RestTemplate` injected.

1) Review the the following file: `$SPRING_CLOUD_LABS_HOME/greeting-ribbon/src/main/java/com/orangeandbronze/greeting/GreetingController.java`.  Notice the `RestTemplate`.  It is not the usual `RestTemplate`, it is load balanced by Ribbon.  The `@LoadBalanced` annotation is a qualifier to ensure we get the load balanced `RestTemplate` injected.  This further simplifies application code.

```java
@Controller
public class GreetingController {

    Logger logger = LoggerFactory
            .getLogger(GreetingController.class);




    @Autowired
    @LoadBalanced
    private RestTemplate restTemplate;

    @RequestMapping("/")
    String getGreeting(Model model) {

        logger.debug("Adding greeting");
        model.addAttribute("msg", "Greetings!!!");

        String fortune = restTemplate.getForObject("http://fortune-service", String.class);

        logger.debug("Adding fortune");
        model.addAttribute("fortune", fortune);

        // resolves to the greeting view
        return "greeting";
    }

}

```

2) Open a terminal window (or re-use an existing one).  Start the `greeting-ribbon` app again.

```bash
$ cd $SPRING_CLOUD_LABS_HOME/greeting-ribbon
$ mvn clean spring-boot:run
```

3) After a few moments, check the `service-registry` [dashboard](http://localhost:8761).  Confirm the `greeting-ribbon` app is registered.

4) [Browse](http://localhost:8080/) to the `greeting-ribbon` application.  
Confirm you are seeing fortunes.  
Refresh as desired.  
Also review the terminal output for the 
`greeting-ribbon` app.

5) Experiment by running another 
`fortune-service` on a different port. 
See if the `greeting-ribbon` app is 
able to load balance between the 
`fortune-service` instances.

```bash
$ cd $SPRING_CLOUD_LABS_HOME/fortune-service
$ mvn clean spring-boot:run -Dserver.port=8788
```

6) Experiment by shutting down one 
`fortune-service` instance 
(and keeping only one running). 
See if the `greeting-ribbon` app
 is able to reach the `fortune-service`
  instance that is up and running.

7) If `spring-retry` is in the classpath, Spring Cloud will automatically retry failed requests.

```xml
<!-- Add this to support retry -->        
<dependency>
    <groupId>org.springframework.retry</groupId>
    <artifactId>spring-retry</artifactId>
</dependency>
```

8) When done stop the `config-server`, `service-registry`, `fortune-service` and `greeting-ribbon` applications.

***What Just Happened?***

By adding `@LoadBalanced` annotation, we get a load balanced `RestTemplate` injected. This uses Netflix Ribbon for client-side load balancing. With Spring Retry, it will automatically retry failed requests.
