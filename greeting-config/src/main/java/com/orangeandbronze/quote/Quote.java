package com.orangeandbronze.quote;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Quote {
	
	@JsonProperty("content")
    private String text;
    
    @JsonProperty("title")
    private String author;

    public String getText() {
        return text;
    }

    public String getAuthor() {
        return author;
    }
}
