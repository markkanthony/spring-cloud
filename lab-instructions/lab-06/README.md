# Spring Cloud Netflix (Feign): Declarative REST Client

Estimated Time: 15 minutes

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

### Setup `greeting-feign`

1) Review the $SPRING_CLOUD_LABS_HOME/greeting-feign/pom.xml file. By adding `spring-cloud-starter-feign` to the classpath this application can create feign clients.

```xml
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-feign</artifactId>
</dependency>
```

2) Review the the following file: `$SPRING_CLOUD_LABS_HOME/greeting-feign/src/main/java/com/orangeandbronze/greeting/FortuneServiceClient.java`.  Notice the `@FeignClient`.  For this interface, we don't need to write the implementation.

```java
@FeignClient("fortune-service")
public interface FortuneServiceClient {

	 @RequestMapping(method = RequestMethod.GET, value = "/")
	 String getFortune();
}

```

3) Review the the following file: `$SPRING_CLOUD_LABS_HOME/greeting-feign/src/main/java/com/orangeandbronze/greeting/GreetingController.java`.  Notice the `FortuneServiceClient` being autowired in.

```java
@Controller
public class GreetingController {

	Logger logger = LoggerFactory
			.getLogger(GreetingController.class);


	@Autowired
	private FortuneServiceClient fortuneServiceClient;

	@RequestMapping("/")
	String getGreeting(Model model) {

		logger.debug("Adding greeting");
		model.addAttribute("msg", "Greetings!!!");

        String fortune = fortuneServiceClient.getFortune();

		logger.debug("Adding fortune");
		model.addAttribute("fortune", fortune);

		// resolves to the greeting view
		return "greeting";
	}

}

```

3) Review the the following file: `$SPRING_CLOUD_LABS_HOME/greeting-feign/src/main/java/com/orangeandbronze/GreetingFeignApplication.java`.  Notice the `@EnableFeignClients` annotation.  This enables feign client creation.

```java
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class GreetingRibbonFeignApplication {

    public static void main(String[] args) {
        SpringApplication.run(GreetingRibbonFeignApplication.class, args);
    }

}

```


2) Open a new terminal window.  Start the `greeting-feign` app.

 ```bash
$ cd $SPRING_CLOUD_LABS_HOME/greeting-feign
$ mvn clean spring-boot:run
```

3) After a few moments, check the `service-registry` [dashboard](http://localhost:8761).  Confirm the `greeting-feign` app is registered.


4) [Browse](http://localhost:8080/) to the `greeting-feign` application.  Confirm you are seeing fortunes.  Refresh as desired.  Also review the terminal output for the `greeting-feign` app.

