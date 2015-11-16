package io.subutai.core.identity.rest;


import org.codehaus.jackson.annotate.JsonProperty;


public class Token
{
    @JsonProperty("token")
    private String value;


    public Token( final String value )
    {
        this.value = value;
    }
}
