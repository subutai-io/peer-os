package io.subutai.bazaar.share.event.payload;


import com.fasterxml.jackson.annotation.JsonProperty;


public class CustomPayload extends Payload
{
    @JsonProperty( value = "message", required = true )
    protected String message;


    public CustomPayload( final String message )
    {
        this.message = message;
    }


    CustomPayload()
    {
    }


    public String getMessage()
    {
        return message;
    }


    @Override
    public String toString()
    {
        return "StringPayload{" + "message='" + message + '\'' + '}';
    }
}
