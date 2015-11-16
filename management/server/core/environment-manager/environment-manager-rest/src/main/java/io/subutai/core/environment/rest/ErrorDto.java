package io.subutai.core.environment.rest;


import org.codehaus.jackson.annotate.JsonProperty;


public class ErrorDto
{
    private static final String ERROR_KEY = "ERROR";

    @JsonProperty( ERROR_KEY )
    private String error;


    public ErrorDto( final String error )
    {
        this.error = error;
    }
}
