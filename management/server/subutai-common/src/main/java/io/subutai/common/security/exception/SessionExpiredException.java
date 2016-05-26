package io.subutai.common.security.exception;


/**
 *  User session expired
 */
public class SessionExpiredException extends SystemSecurityException
{
    public SessionExpiredException()
    {
        super();
    }


    public SessionExpiredException( final String message )
    {
        super( message );
    }


    public SessionExpiredException( final String message, final Throwable cause )
    {
        super( message, cause );
    }


    public SessionExpiredException( final Throwable cause )
    {
        super( cause );
    }


    @Override
    public String toString()
    {
        return super.toString();
    }
}
