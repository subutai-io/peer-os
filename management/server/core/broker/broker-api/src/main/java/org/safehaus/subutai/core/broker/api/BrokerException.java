package org.safehaus.subutai.core.broker.api;


/**
 * Exception thrown by Broker methods
 */
public class BrokerException extends Exception
{
    public BrokerException( final String message )
    {
        super( message );
    }


    public BrokerException( final Throwable cause )
    {
        super( cause );
    }
}
