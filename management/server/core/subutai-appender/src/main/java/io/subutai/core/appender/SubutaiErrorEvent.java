package io.subutai.core.appender;


public class SubutaiErrorEvent extends SubutaiLogEvent
{
    final String stackTrace;


    public SubutaiErrorEvent( final long timeStamp, final String loggerName, final String renderedMessage,
                              final String stackTrace )
    {
        super( timeStamp, loggerName, renderedMessage );
        this.stackTrace = stackTrace;
    }


    public String getStackTrace()
    {
        return stackTrace;
    }
}
