package io.subutai.core.bazaarmanager.api.exception;


public class BazaarManagerException extends Exception
{
    public BazaarManagerException( final String message, final Throwable cause )
    {
        super( message, cause );
    }


    public BazaarManagerException( final String message )
    {
        super( message );
    }


    public BazaarManagerException( final Throwable cause )
    {
        super( cause );
    }
}
