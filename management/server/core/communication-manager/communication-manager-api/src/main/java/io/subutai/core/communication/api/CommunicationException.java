package io.subutai.core.communication.api;


/**
 * Created by tzhamakeev on 8/5/15.
 */
public class CommunicationException extends Exception
{

    public CommunicationException( final String message, final Throwable cause )
    {
        super( message, cause );
    }


    public CommunicationException( final String message )
    {
        super( message );
    }
}
