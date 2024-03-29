package com.orangeandbronze.quote;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RefreshScope
public class QuoteService {

    private Logger logger = LoggerFactory.getLogger(getClass());
    private String quoteServiceURL;
    private RestTemplate restTemplate;

    @Autowired
    public QuoteService(@Value("${quoteServiceURL:}") String quoteServiceURL) {
        this.quoteServiceURL = quoteServiceURL;
        this.restTemplate = new RestTemplate();
    }

    public String getQuoteServiceURL() {
        return quoteServiceURL;
    }

    public Quote getQuote() {
        logger.info("quoteServiceURL: {}", quoteServiceURL);
        Quote[] quotes = restTemplate.getForObject(quoteServiceURL, Quote[].class);
        return getOne(quotes);
    }

	private Quote getOne(Quote[] quotes) {
		return ArrayUtils.isNotEmpty(quotes) ? quotes[0] : null;
	}

}
