package com.orangeandbronze.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

// TODO 02: Provide a fallback via Hystrix
// TODO 02a: Add fallback to @FeignClient annotation
// A FallbackFortuneServiceClient class is provided for you
//
// TODO 02b: Enable Feign Hystrix support by setting some properties
//     feign.hystrix.enabled=true
//     # See https://github.com/spring-cloud/spring-cloud-netflix/issues/1328
//     hystrix.command.default.execution.isolation.strategy=SEMAPHORE
//
@FeignClient("fortune-service")
public interface FortuneServiceClient {

	@GetMapping("/")
	String getFortune();

}

/*
 * When using Feign with Hystrix fallbacks, there are
 * multiple beans in the ApplicationContext of the same
 * type. This will cause @Autowired to not work because
 * there isn’t exactly one bean, or one marked as primary.
 * To work around this, Spring Cloud Netflix marks all
 * Feign instances as @Primary, so Spring Framework will
 * know which bean to inject.
 */
@Component
class FallbackFortuneServiceClient implements FortuneServiceClient {

    private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public String getFortune() {
        logger.debug("Default fortune used.");
        return "This fortune is no good. Try another.";
	}

}

