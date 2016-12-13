package io.subutai.core.appender;


public abstract class SubutaiLogEvent
{
    protected long timeStamp;
    protected String loggerName;
    protected String renderedMessage;


    public SubutaiLogEvent( final long timeStamp, final String loggerName, final String renderedMessage )
    {
        this.timeStamp = timeStamp;
        this.loggerName = loggerName;
        this.renderedMessage = renderedMessage;
    }


    public long getTimeStamp()
    {
        return timeStamp;
    }


    public String getLoggerName()
    {
        return loggerName;
    }


    public String getRenderedMessage()
    {
        return renderedMessage;
    }
}
