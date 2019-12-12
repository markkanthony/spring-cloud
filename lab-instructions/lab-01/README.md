# Dynamic Reconfiguration with Spring Cloud Config

Estimated Time: 60 minutes

## What You Will Learn

* How to set up a git repository to hold configuration data
* How to set up a config server (`config-server`) with a git backend
* How to set up a client (`greeting-config`) to pull configuration from the `config-server`
* How to change log levels for a running application (`greeting-config`)
* How to use `@ConfigurationProperties` to capture configuration changes (`greeting-config`)
* How to use `@RefreshScope` to capture configuration changes (`greeting-config`)
* How to override configuration values by profile (`greeting-config`)
* How to use Spring Cloud Service to provision and configure a Config Server
* How to use Cloud Bus to notify applications (`greeting-config`) to refresh configuration at scale

## Exercises

### Set up a Git repository to contain configuration files

To start, we need a repository to hold our configuration files

1) Create a new Git repository (e.g. `config-repo`) in your account.

2) After creating the new repository, clone in to your local workspace (you may want to create a common location for your Git repos, such as ~/repos):

```bash
$ cd [location of your github repos, e.g. ~/repos]
$ git clone <Your config-repo - HTTP/S clone URL>
$ cd config-repo
```

Notice that this repository is basically empty. This repository will be the source of configuration data.

### Set up `config-server`

1) Review the following file: `$SPRING_CLOUD_LABS_HOME/config-server/pom.xml`
By adding `spring-cloud-config-server` to the classpath, this application is eligible to embed a config-server.

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-config-server</artifactId>
</dependency>
```
2) Review the following file:`$SPRING_CLOUD_LABS_HOME/config-server/src/main/java/com/orangeandbronze/ConfigServerApplication.java`

```java
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

        public static void main(String[] args) {
                SpringApplication.run(ConfigServerApplication.class, args);
        }
}
```
Note the `@EnableConfigServer` annotation.  That embeds the config-server.

3) Set the Git repository for the `config-server`. This will be the source of the configuration data. Edit the `$SPRING_CLOUD_LABS_HOME/config-server/src/main/resources/application.yml` file.

```yml
 server:
     port: 8888

 spring:
     cloud:
         config:
             server:
                 git:
                     uri: http://<git-url>/<username>/config-repo.git #<-- CHANGE ME
```
Make sure to substitute your forked `config-repo` repository. Do not use the literal above.

4) Open a terminal window and start the `config-server`.

```bash
$ cd $SPRING_CLOUD_LABS_HOME/config-server
$ mvn clean spring-boot:run
```

Your config-server will be running locally once you see a "Started ConfigServerApplication..." message. You will not be returned to a command prompt and must leave this window open.

5) Let's add some configuration.  Edit your `config-repo`.  Create a file called `hello-world.yml`.  Add the content below to the file and push the changes back to Git.  Be sure to substitute your name for `<Your name>`.

```yml
name: <Your Name>
```

6) Confirm the `config-server` is up and configured with a backing git repository by calling one of its [endpoints](http://projects.spring.io/spring-cloud/docs/1.0.3/spring-cloud.html#_quick_start).  Because the returned payload is JSON, we recommend using something that will pretty-print the document.  A good tool for this is the Chrome [JSON Formatter](https://chrome.google.com/webstore/detail/json-formatter/bcjindcccaagfpapjjmafapmmgkkhgoa?hl=en) plug-in.

Open a browser window and fetch the following url: [http://localhost:8888/hello-world/default](http://localhost:8888/hello-world/default)

***What Just Happened?***

The `config-server` exposes several [endpoints](http://projects.spring.io/spring-cloud/docs/1.0.3/spring-cloud.html#_quick_start) to fetch configuration.

In this case, we are manually calling one of those endpoints (`/{application}/{profile}[/{label}]`) to fetch configuration.  We substituted our example client application `hello-world` as the `{application}` and the `default` profile as the `{profile}`.  We didn't specify the label to use so `master` is assumed.  In the returned document, we see the configuration file `hello-world.yml` listed as a `propertySource` with the associated key/value pair.  This is just an example, as you move through the lab you will add configuration for `greeting-config` (our client application).


### Set up `greeting-config`

1) Review the following file: `$SPRING_CLOUD_LABS_HOME/greeting-config/pom.xml`
By adding `spring-cloud-config-client` to the classpath, this application will consume configuration from the config-server.  `greeting-config` is a config client.

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-config-client</artifactId>
</dependency>
```

2) Review the `$SPRING_CLOUD_LABS_HOME/greeting-config/src/main/resources/bootstrap.yml`

```yml
spring:
    application:
        name: greeting-config
```


`spring.application.name` defines the name of the application.  This value is used in several places within Spring Cloud: locating configuration files by name, service discovery/registration by name, etc.  In this lab, it will be used to locate config files for the `greeting-config` application.

Absent from the `bootstrap.yml` is the `spring.cloud.config.uri`, which defines how `greeting-config` reaches the `config-server`. Since there is no `spring.cloud.config.uri` defined in this file, the default value of `http://localhost:8888` is used.  Notice that this is the same host and port of the `config-server` application.

3) Open a new terminal window.  Start the `greeting-config` application:

```bash
$ cd $SPRING_CLOUD_LABS_HOME/greeting-config
$ mvn clean spring-boot:run
```

4) Confirm the `greeting-config` app is up.  Browse to [http://localhost:8080](http://localhost:8080). You should see the message "Greetings!!!".

***What Just Happened?***

At this point, you connected the `greeting-config` application with the `config-server`.  This can be confirmed by reviewing the logs of the `greeting-config` application.

`greeting-config` log output:

```
INFO 15706 --- [lication.main()] b.c.PropertySourceBootstrapConfiguration :
Located property source: CompositePropertySource [name='configService', propertySources=[]]
```

There is still no configuration in the git repo, but at this point we have everything wired (`greeting-config --> config-server --> config-repo` repo) so we can add configuration parameters/values and see the effects in out client application `greeting-config`.

Configuration parameters/values will be added as we move through the lab.

5) Stop the `greeting-config` application

### Changing Logging Levels

As your first use of the `config-server`, you will change the logging level of the greeting-config application.

1) View the `getGreeting()` method of the `GreetingController` class (`$SPRING_CLOUD_LABS_HOME/greeting-config/src/main/java/com/orangeandbronze/greeting/GreetingController.java`).

```java
@RequestMapping("/")
String getGreeting(Model model) {

    logger.debug("Adding greeting");
    model.addAttribute("msg", "Greetings!!!");

    if (greetingProperties.isDisplayFortune()) {
        logger.debug("Adding fortune");
        model.addAttribute("fortune", fortuneService.getFortune());
    }

    // resolves to the greeting view
    return "greeting";
}
```
We want to see these debug messages.  By default only log levels of `ERROR`, `WARN` and `INFO` will be logged. You will change the log level to `DEBUG` using configuration. All log output will be directed to `System.out` and `System.error` by default, so logs will be output to the terminal window(s).

2) In your `config-repo` repo.  Add the content below to the `greeting-config.yml` file and push the changes back to Git.

```yml
management:
    security:
        enabled: false

logging: # <----New sections below
    level:
        root: WARN
        com:
            orangeandbronze: DEBUG

greeting:
    displayFortune: false

quoteServiceURL: http://quotesondesign.com/wp-json/posts?filter[orderby]=rand&filter[posts_per_page]=1

```

We have added several configuration parameters that will be used throughout this lab.  For this exercise, we have set the log level for classes in the `com.orangeandbronze` package to `DEBUG`.

3) While watching the `greeting-config` terminal, refresh the [http://localhost:8080](http://localhost:8080/) url.  Notice there are no `DEBUG` logs yet.

4) Does the `config-server` see the change in your git repo?  Let's check what the `config-server` is serving.  Browse to [http://localhost:8888/greeting-config/default](http://localhost:8888/greeting-config/default)

The propertySources value has changed!  The `config-server` has picked up the changes to the git repo. (If you don't see the change, verify that you have pushed the greeting-config.yml to GitHub.)

5) Review the following file: `$SPRING_CLOUD_LABS_HOME/greeting-config/pom.xml`.  For the `greeting-config` application to pick up the configuration changes, it must include the `actuator` dependency.  The `actuator` adds several additional endpoints to the application for operational visibility and tasks that need to be carried out.  In this case, we have added the actuator so that we can use the `/refresh` endpoint, which allows us to refresh the application config on demand.

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

6) For the `greeting-config` application to pick up the configuration changes, it must be told to do so.  Notify `greeting-config` app to pick up the new config by POSTing to the `greeting-config` `/refresh` endpoint.  Open a new terminal window and execute the following:

```bash
$ curl -X POST http://localhost:8080/refresh
```

7) Refresh the `greeting-config` [http://localhost:8080](http://localhost:8080/) url while viewing the `greeting-config` terminal.  You should see the debug line "Adding greeting"

Congratulations! You have used the config-server to change the logging level of the greeting-config application.

### Turning on a Feature with `@ConfigurationProperties`

Use of `@ConfigurationProperties` is a common way to externalize, group, and validate configuration in Spring applications.  `@ConfigurationProperties` beans are automatically rebound when application config is refreshed.

1) Review `$SPRING_CLOUD_LABS_HOME/greeting-config/src/main/java/com/orangeandbronze/greeting/GreetingProperties.java`.  Use of the `@ConfigurationProperties` annotation allows for reading of configuration values.  Configuration keys are a combination of the `prefix` and the field names.  In this case, there is one field (`displayFortune`).  Therefore `greeting.displayFortune` is used to turn the display of fortunes on/off.  Remaining code is typical getter/setters for the fields.

```java
@ConfigurationProperties(prefix="greeting")
public class GreetingProperties {

    private boolean displayFortune;

    public boolean isDisplayFortune() {
        return displayFortune;
    }

    public void setDisplayFortune(boolean displayFortune) {
        this.displayFortune = displayFortune;
    }
}
```

2) Review `$SPRING_CLOUD_LABS_HOME/greeting-config/src/main/java/com/orangeandbronze/greeting/GreetingController.java`.  Note how the `greetingProperties.isDisplayFortune()` is used to turn the display of fortunes on/off.  There are times when you want to turn features on/off on demand.  In this case, we want the fortune feature "on" with our greeting.

```java
@EnableConfigurationProperties(GreetingProperties.class)
public class GreetingController {

    Logger logger = LoggerFactory
            .getLogger(GreetingController.class);


    @Autowired
    GreetingProperties greetingProperties;

    @Autowired
    FortuneService fortuneService;

    @RequestMapping("/")
    String getGreeting(Model model){

        logger.debug("Adding greeting");
        model.addAttribute("msg", "Greetings!!!");

        if (greetingProperties.isDisplayFortune()) {
            logger.debug("Adding fortune");
            model.addAttribute("fortune", fortuneService.getFortune());
        }

        // resolves to the greeting view
        return "greeting";
    }

}

```

3) Edit your `config-repo`.   Change `greeting.displayFortune` from `false` to `true` in the `greeting-config.yml` and push the changes back to GitHub.

```yml
management:
    security:
        enabled: false

. . .

greeting:
    displayFortune: true # <--- Change to true

quoteServiceURL: http://quotesondesign.com/wp-json/posts?filter[orderby]=rand&filter[posts_per_page]=1

```

4) Notify `greeting-config` app to pick up the new config by POSTing to the `/refresh` endpoint.

```bash
$ curl -X POST http://localhost:8080/refresh
```

5) Then refresh the [http://localhost:8080](http://localhost:8080/) url and see the fortune included.

Congratulations! You have turned on a feature using the config-server.

### Reinitializing Beans with `@RefreshScope`

Now you will use the `config-server` to obtain a service URI rather than hardcoding it your application code.

Beans annotated with the `@RefreshScope` will be recreated when refreshed so they can pick up new config values.

1) Review `$SPRING_CLOUD_LABS_HOME/greeting-config/src/main/java/com/orangeandbronze/quote/QuoteService.java`.  `QuoteService.java` uses the `@RefreshScope` annotation. Beans with the `@RefreshScope` annotation will be recreated when refreshing configuration.  The `@Value` annotation allows for injecting the value of the quoteServiceURL configuration parameter.

In this case, we are using a third party service to get quotes.  We want to keep our environments aligned with the third party.  So we are going to override configuration values by profile (next section).

```java
@Service
@RefreshScope
public class QuoteService {
    Logger logger = LoggerFactory
            .getLogger(QuoteController.class);

    @Value("${quoteServiceURL}")
    private String quoteServiceURL;

    public String getQuoteServiceURI() {
        return quoteServiceURL;
    }

    public Quote getQuote(){
        logger.info("quoteServiceURL: {}", quoteServiceURL);
        RestTemplate restTemplate = new RestTemplate();
        Quote quote = restTemplate.getForObject(
                quoteServiceURL, Quote.class);
        return quote;
    }
}
```

2) Review `$SPRING_CLOUD_LABS_HOME/greeting-config/src/main/java/com/orangeandbronze/quote/QuoteController.java`.  `QuoteController` calls the `QuoteService` for quotes.

```java
@Controller
public class QuoteController {

    Logger logger = LoggerFactory
            .getLogger(QuoteController.class);

    @Autowired
    private QuoteService quoteService;

    @RequestMapping("/random-quote")
    String getView(Model model) {

        model.addAttribute("quote", quoteService.getQuote());
        model.addAttribute("uri", quoteService.getQuoteServiceURI());
        return "quote";
    }
}
```

3) In your browser, hit the [http://localhost:8080/random-quote](http://localhost:8080/random-quote) url. Note where the data is being served from: `http://quotesondesign.com/wp-json/posts?filter[orderby]=rand&filter[posts_per_page]=1`

### Override Configuration Values By Profile

1) Stop the `greeting-config` application using Command-C or CTRL-C in the terminal window.

2) Set the active profile to qa for the `greeting-config` application.  In the example below, we use an environment variable to set the active profile.

```bash
[mac, linux]
$ SPRING_PROFILES_ACTIVE=qa mvn clean spring-boot:run

[windows]
$ set SPRING_PROFILES_ACTIVE=qa
$ mvn clean spring-boot:run
```

3) Make sure the profile is set by browsing to the [http://localhost:8080/env](http://localhost:8080/env) endpoint (provided by `actuator`).  Under profiles `qa` should be listed.

4) In your `config-repo` repository, create a new file: `greeting-config-qa.yml`. Fill it in with the following content:

```yml
quoteServiceURL: http://quotesondesign.com/wp-json/posts?filter[orderby]=rand&filter[posts_per_page]=1
```

Make sure to commit and push to GitHub.

5) Browse to [http://localhost:8080/random-quote](http://localhost:8080/random-quote).  Quotes are still being served from `http://quotesondesign.com/wp-json/posts?filter[orderby]=rand&filter[posts_per_page]=1`.

6) Refresh the application configuration values

```bash
$ curl -X POST http://localhost:8080/refresh
```

7) Refresh the [http://localhost:8080/random-quote](http://localhost:8080/random-quote) url.  Quotes are now being served from QA.

8) Stop the `greeting-config` application.

***What Just Happened?***

Configuration from `greeting-config.yml` was overridden by a configuration file that was more specific (`greeting-config-qa.yml`).


### Refreshing Application Configuration at Scale with Cloud Bus

Until now you have been notifying your application to pick up new configuration by POSTing to the `/refresh` endpoint.

When running several instances of your application, this poses several problems:

* Refreshing each individual instance is time consuming and too much overhead
* When running on Cloud Foundry you don't have control over which instances you hit when sending the POST request due to load balancing provided by the `router`

Cloud Bus addresses the issues listed above by providing a single endpoint to refresh all application instances via a pub/sub notification.

1) Include the cloud bus dependency in the  `$SPRING_CLOUD_LABS_HOME/greeting-config/pom.xml`.

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-bus-amqp</artifactId>
</dependency>
```

2) Repackage the `greeting-config` application:

```bash
$ mvn clean package
```

3) Run two or more instances of the application.

```bash
$ mvn spring-boot:run -Dserver.port=8080
$ mvn spring-boot:run -Dserver.port=8081
$ mvn spring-boot:run -Dserver.port=8082
...
```

4) Turn logging down.  In your `config-repo` repo edit the `greeting-config.yml`.  Set the log level to `INFO`.  Make sure to push back to Git repository.
```yml
logging:
    level:
        root: WARN
        com:
            orangeandbronze: INFO # <---- from DEBUG to INFO
```

5) Notify applications to pickup the change.  Send a POST to the `greeting-config` `/bus/refresh` endpoint.  Use your `greeting-config` URL (similar to the one below).
```bash
$ curl -X POST http://localhost:8080/bus/refresh
```

6) Refresh the `greeting-config` `/` endpoint several times in your browser. No more logs!

***What Just Happened?***

By POSTing to the `/bus/refresh` endpoint of one `greeting-config` instance, the instance sends a message to the message bus (RabbitMQ server in this case). Since other instances also subscribe to the bus, they will also receive the message. The message prompts it to reload application configuration from the configuration server. Now, we are able to refresh every instance by simply POSTing to one instance.

