package io.subutai.core.registration.api.exception;


/**
 * Created by talas on 8/28/15.
 */
public class NodeRegistrationException extends Exception
{
    public NodeRegistrationException()
    {
        super();
    }


    public NodeRegistrationException( final String message )
    {
        super( message );
    }


    public NodeRegistrationException( final String message, final Throwable cause )
    {
        super( message, cause );
    }


    public NodeRegistrationException( final Throwable cause )
    {
        super( cause );
    }


    protected NodeRegistrationException( final String message, final Throwable cause, final boolean enableSuppression,
                                         final boolean writableStackTrace )
    {
        super( message, cause, enableSuppression, writableStackTrace );
    }
}
