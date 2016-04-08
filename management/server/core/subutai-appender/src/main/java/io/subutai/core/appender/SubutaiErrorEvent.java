package io.subutai.core.appender;


public class SubutaiErrorEvent
{
    final long timeStamp;
    final String loggerName;
    final String renderedMessage;
    final String stackTrace;


    public SubutaiErrorEvent( final long timeStamp, final String loggerName, final String renderedMessage,
                              final String stackTrace )
    {
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


    public String getStackTrace()
    {
        return stackTrace;
    }
}
