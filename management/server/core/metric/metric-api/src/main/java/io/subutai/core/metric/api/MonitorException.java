package io.subutai.core.metric.api;


/**
 * Exception thrown by Monitor
 */
public class MonitorException extends Exception
{
    public MonitorException( final Throwable cause )
    {
        super( cause );
    }


    public MonitorException( final String message )
    {
        super( message );
    }
}
