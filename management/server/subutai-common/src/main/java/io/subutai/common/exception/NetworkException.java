package io.subutai.common.exception;


/**
 * General network exception
 */
public class NetworkException extends Exception
{
    public NetworkException( final Throwable cause )
    {
        super( cause );
    }


    public NetworkException( final String s )
    {
        super( s );
    }
}
