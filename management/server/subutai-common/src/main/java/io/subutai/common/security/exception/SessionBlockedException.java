package io.subutai.common.security.exception;


/**
 * Block user on unsuccessful 3 authentications
 */
public class SessionBlockedException extends SystemSecurityException
{
    public SessionBlockedException()
    {
        super();
    }


    public SessionBlockedException( final String message )
    {
        super( message );
    }


    public SessionBlockedException( final String message, final Throwable cause )
    {
        super( message, cause );
    }


    public SessionBlockedException( final Throwable cause )
    {
        super( cause );
    }


    @Override
    public String toString()
    {
        return super.toString();
    }
}
