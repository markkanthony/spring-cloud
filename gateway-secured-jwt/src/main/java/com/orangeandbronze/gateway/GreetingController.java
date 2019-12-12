package com.orangeandbronze.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GreetingController {
	
    private Logger logger = LoggerFactory.getLogger(getClass());

	private OAuth2RestTemplate restTemplate;

    @Autowired
	public GreetingController(OAuth2RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

    // TODO 01: Use declarative REST client via Feign (see FortuneServiceClient)
    // TODO 01a: Remove the OAuth2RestTemplate field and related code
    // TODO 01b: Use a FortuneServiceClient that is dependency injected
    // Define FortuneServiceClient as a field variable and have it
    // initialized in the constructor via constructor argument.

	@GetMapping("/greeting")
	public String getGreeting(Model model) {
		logger.debug("Adding greeting");
		model.addAttribute("msg", "Greetings!!!");
		
		logger.debug("Adding fortune");
		// TODO 01c: Use the FortuneServiceClient field to retrieve a fortune
		model.addAttribute("fortune", restTemplate.getForObject("http://fortune-service", String.class));

		// resolves to the greeting view
		return "greeting";
	}

}
