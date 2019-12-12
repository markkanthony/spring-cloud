# Spring Cloud Sleuth and Zipkin: Distributed Tracing

Estimated Time: 15 minutes

## Exercises

### Start the  `config-server`,  and `service-registry`

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

### Setup `greeting-trace` and `fortune-trace`

1) Review the `$SPRING_CLOUD_LABS_HOME/greeting-trace/pom.xml` and `$SPRING_CLOUD_LABS_HOME/fortune-trace/pom.xml` files. By adding `spring-cloud-starter-sleuth` to the classpath, common communication channels are automatically instrumented to add information that allows tracing requests from its arrival and each individual hop along the way.

```xml
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-sleuth</artifactId>
</dependency>
```

2) Open a new terminal window.  Start the `fortune-trace` app.

```bash
$ cd $SPRING_CLOUD_LABS_HOME/fortune-trace
$ mvn clean spring-boot:run
```

3) Open a new terminal window.  Start the `greeting-trace` app.

```bash
$ cd $SPRING_CLOUD_LABS_HOME/greeting-trace
$ mvn clean spring-boot:run
```

4) [Browse](http://localhost:8080/) to the `greeting-trace` application.  Confirm you are seeing fortunes.  Refresh as desired.  Also review the terminal output for the `greeting-trace` app. You should notice that the logs include a trace ID and span ID.

```
2017-12-11 17:12:45.116 DEBUG [greeting-trace,73b62c0f90d11e06,73b62c0f90d11e06,false] 7744 --- [nio-8080-exec-1] c.o.GreetingController     : ...
```

5) Also reivew the terminal output for the `fortune-trace` app. You should notice that the logs include a trace ID and span ID. The trace ID would be the same for a request, as it flows from one microservice to another.

```
2017-12-11 17:12:45.112 DEBUG [fortune-service,73b62c0f90d11e06,d9e82c0f90d182d6,false] 3700 --- [nio-8080-exec-1] c.o.fortune.FortuneController     : ...
```

***What Just Happened?***

As a request flows from one component to another in a system, through ingress and egress points, tracers add logic where possible to perpetuate a unique trace ID that’s generated when the first request is made. As a request arrives at a component along its journey, a new span ID is assigned for that component and added to the trace. A trace represents the whole journey of a request, and a span is each individual hop along the way, each request.

### Generate Zipkin-compatible Traces

1) Update the `$SPRING_CLOUD_LABS_HOME/greeting-trace/pom.xml` and `$SPRING_CLOUD_LABS_HOME/fortune-trace/pom.xml` files. Replace `spring-cloud-starter-sleuth` with `spring-cloud-starter-zipkin`. This adds `spring-cloud-sleuth-zipkin` to the classpath. With `spring-cloud-sleuth-zipkin` in the classpath, the application's Sleuth traces will be published to Zipkin for analysis.

```xml
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-zipkin</artifactId>
</dependency>
```

2) If `spring-cloud-sleuth-zipkin` is in the classpath then the app will generate and collect Zipkin-compatible traces. By default it sends them via HTTP to a Zipkin server on localhost (port 9411). You can configure the location of the service using `spring.zipkin.baseUrl`.

3) Start the `fortune-trace` in a terminal window.

```bash
$ cd $SPRING_CLOUD_LABS_HOME/fortune-trace
$ mvn clean spring-boot:run
```

3) Start the `greeting-trace` in a terminal window.

```bash
$ cd $SPRING_CLOUD_LABS_HOME/greeting-trace
$ mvn clean spring-boot:run
```

### Setup `zipkin-server`

1) Review the following file: `$SPRING_CLOUD_LABS_HOME/zipkin-server/src/main/java/com/orangeandbronze/TraceServerApplication.java` Note the `@EnableZipkinServer` annotation.  That embeds the Zipkin server.

```java
@SpringBootApplication
@EnableZipkinServer
public class TraceServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(TraceServerApplication.class, args);
    }
}
```

2) Also review `$SPRING_CLOUD_LABS_HOME/zipkin-server/src/main/resources/application`. Notice that the application will listen on port 9411.

3) Open a new terminal window.  Start the `zipkin-server` app.

```bash
$ cd $SPRING_CLOUD_LABS_HOME/zipkin-server
$ mvn clean spring-boot:run
```

4) [Browse](http://localhost:9411/) to the Zipkin server. Confirm that you are able to see its user interface.

5) [Browse](http://localhost:8080/) to the `greeting-trace` application.  Confirm you are seeing fortunes.  Refresh as desired.

6) Come back to the Zipkin server. See if you can find traces from the `greeting-service` and `fortune-service`.

***What Just Happened?***

The Sleuth-enabled applications published Zipkin-compatible traces via HTTP to the Zipkin server (listening on port 9411). The Zipkin server analyzed the traces and measured the time it took on each component/microservice.
