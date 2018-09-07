package io.subutai.bazaar.share.event.payload;


import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;


public class ProgressPayload extends Payload
{
    @JsonProperty( value = "step", required = true )
    private String step;

    @JsonProperty( value = "message", required = true )
    private String message;

    @JsonProperty( value = "value", required = true )
    private double value;


    public ProgressPayload( final String step, final String message, final double value )
    {
        this.step = step;
        this.message = message;
        this.value = value;
    }


    private ProgressPayload()
    {
    }


    public String getStep()
    {
        return step;
    }


    public String getMessage()
    {
        return message;
    }


    public double getValue()
    {
        return value;
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( o == null || getClass() != o.getClass() )
        {
            return false;
        }
        final ProgressPayload that = ( ProgressPayload ) o;
        return Double.compare( that.value, value ) == 0 && Objects.equals( step, that.step ) && Objects
                .equals( message, that.message );
    }


    @Override
    public int hashCode()
    {

        return Objects.hash( step, message, value );
    }


    @Override
    public String toString()
    {
        return "ProgressPayload{" + "step='" + step + '\'' + ", message='" + message + '\'' + ", value=" + value + '}';
    }
}