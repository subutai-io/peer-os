package org.safehaus.subutai.core.monitor.api;


/**
 * Exception thrown by Monitor module
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
