package io.subutai.bazaar.share.event.payload;


import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;


public class LogPayload extends Payload
{
    public enum Level
    {
        TRACE, DEBUG, INFO, WARN, ERROR, FATAL
    }


    @JsonProperty( value = "source", required = true )
    protected String source;

    @JsonProperty( value = "message", required = true )
    protected String message;

    @JsonProperty( value = "level", required = true )
    protected Level level;


    public LogPayload( final String source, final String message, final Level level )
    {
        this.source = source;
        this.message = message;
        this.level = level;
    }


    LogPayload()
    {
    }


    public String getSource()
    {
        return source;
    }


    public String getMessage()
    {
        return message;
    }


    public Level getLevel()
    {
        return level;
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
        final LogPayload that = ( LogPayload ) o;
        return Objects.equals( source, that.source ) && Objects.equals( message, that.message ) && level == that.level;
    }


    @Override
    public int hashCode()
    {

        return Objects.hash( source, message, level );
    }


    @Override
    public String toString()
    {
        return "LogPayload{" + "source='" + source + '\'' + ", message='" + message + '\'' + ", level=" + level + '}';
    }
}
