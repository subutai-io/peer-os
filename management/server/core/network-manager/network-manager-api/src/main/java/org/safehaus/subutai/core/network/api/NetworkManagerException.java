package org.safehaus.subutai.core.network.api;


/**
 * Exception thrown by NetworkManager
 */
public class NetworkManagerException extends Exception
{
    public NetworkManagerException( final String message )
    {
        super( message );
    }


    public NetworkManagerException( final Throwable cause )
    {
        super( cause );
    }
}
