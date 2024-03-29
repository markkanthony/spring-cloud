package com.orangeandbronze.greeting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.orangeandbronze.fortune.FortuneService;

@Controller
public class GreetingController {

    private Logger logger = LoggerFactory.getLogger(getClass());
    private FortuneService fortuneService;

    @Autowired
    public GreetingController(FortuneService fortuneService) {
        this.fortuneService = fortuneService;
    }


    @RequestMapping("/")
    public String getGreeting(Model model) {
        logger.debug("Adding greeting");
        model.addAttribute("msg", "Greetings!!!");

        String fortune = fortuneService.getFortune();

        logger.debug("Adding fortune");
        model.addAttribute("fortune", fortune);

        // resolves to the greeting view
        return "greeting";
    }

}
