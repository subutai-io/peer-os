package io.subutai.bazaar.share.dto;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


public class SubutaiSystemLog
{
    public enum LogType
    {
        ERROR
    }


    public enum LogSource
    {
        PEER, P2P, RH, CH
    }


    @JsonProperty( value = "sourceType" )
    private LogSource sourceType;

    @JsonProperty( value = "sourceName" )
    private String sourceName;

    @JsonProperty( value = "type" )
    private LogType type;

    @JsonProperty( value = "timeStamp" )
    private long timeStamp;

    @JsonProperty( value = "loggerName" )
    private String loggerName;

    @JsonProperty( value = "renderedMessage" )
    private String renderedMessage;

    @JsonProperty( value = "stackTrace" )
    private String stackTrace;


    @JsonCreator
    public SubutaiSystemLog( @JsonProperty( value = "sourceType" ) final LogSource sourceType,
                             @JsonProperty( value = "sourceName" ) final String sourceName,
                             @JsonProperty( value = "type" ) final LogType type,
                             @JsonProperty( value = "timeStamp" ) final long timeStamp,
                             @JsonProperty( value = "loggerName" ) final String loggerName,
                             @JsonProperty( value = "renderedMessage" ) final String renderedMessage,
                             @JsonProperty( value = "stackTrace" ) final String stackTrace )
    {
        this.sourceType = sourceType;
        this.sourceName = sourceName;
        this.type = type;
        this.timeStamp = timeStamp;
        this.loggerName = loggerName;
        this.renderedMessage = renderedMessage;
        this.stackTrace = stackTrace;
    }


    @Override
    public String toString()
    {
        return "SubutaiLogEvent{" + "timeStamp=" + timeStamp + ", loggerName='" + loggerName + '\''
                + ", renderedMessage='" + renderedMessage + '\'' + ", stackTrace='" + stackTrace + '\'' + '}';
    }


    public LogSource getSourceType()
    {
        return sourceType;
    }


    public void setSourceType( final LogSource sourceType )
    {
        this.sourceType = sourceType;
    }


    public String getSourceName()
    {
        return sourceName;
    }


    public void setSourceName( final String sourceName )
    {
        this.sourceName = sourceName;
    }


    public LogType getType()
    {
        return type;
    }


    public void setType( final LogType type )
    {
        this.type = type;
    }


    public long getTimeStamp()
    {
        return timeStamp;
    }


    public void setTimeStamp( final long timeStamp )
    {
        this.timeStamp = timeStamp;
    }


    public String getLoggerName()
    {
        return loggerName;
    }


    public void setLoggerName( final String loggerName )
    {
        this.loggerName = loggerName;
    }


    public String getRenderedMessage()
    {
        return renderedMessage;
    }


    public void setRenderedMessage( final String renderedMessage )
    {
        this.renderedMessage = renderedMessage;
    }


    public String getStackTrace()
    {
        return stackTrace;
    }


    public void setStackTrace( final String stackTrace )
    {
        this.stackTrace = stackTrace;
    }
}
