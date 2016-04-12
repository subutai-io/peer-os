package io.subutai.core.registration.api.exception;


public class HostRegistrationException extends Exception
{
    public HostRegistrationException()
    {
        super();
    }


    public HostRegistrationException( final String message )
    {
        super( message );
    }


    public HostRegistrationException( final String message, final Throwable cause )
    {
        super( message, cause );
    }


    public HostRegistrationException( final Throwable cause )
    {
        super( cause );
    }


    protected HostRegistrationException( final String message, final Throwable cause, final boolean enableSuppression,
                                         final boolean writableStackTrace )
    {
        super( message, cause, enableSuppression, writableStackTrace );
    }
}
