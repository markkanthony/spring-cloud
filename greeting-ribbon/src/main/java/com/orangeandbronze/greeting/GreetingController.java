package com.orangeandbronze.greeting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

@Controller
public class GreetingController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    // TODO 03a: Remove LoadBalancerClient
    // TODO 03b: Use a RestTemplate that is dependency injected
    // Define the RestTemplate as a field variable and have it
    // initialized with a constructor parameter.
    @Autowired
    private RestTemplate restTemplate;
    


    @RequestMapping("/")
    public String getGreeting(Model model) {
        logger.debug("Adding greeting");
        model.addAttribute("msg", "Greetings!!!");

        // TODO 03c: Use the injected RestTemplate instead of local object
        // Instead of calling fetchFortuneServiceUrl, use "http://fortune-service" (the application name).
        // Remove or comment out unused private method.

//        RestTemplate restTemplate = new RestTemplate();
		String fortune = restTemplate.getForObject("http://fortune-service", String.class);

        logger.debug("Adding fortune");
        model.addAttribute("fortune", fortune);

        // resolves to the greeting view
        return "greeting";
    }

//    private String fetchFortuneServiceUrl() {
//        ServiceInstance instance = loadBalancerClient.choose("fortune-service");
//
//        logger.debug("uri: {}", instance.getUri().toString());
//        logger.debug("serviceId: {}", instance.getServiceId());
//
//        return instance.getUri().toString();
//    }

}
