package io.subutai.common.security.exception;


/**
 *  Password or TOKEN lifetime expired , user should update password/token
 */
public class IdentityExpiredException extends SystemSecurityException
{
    public IdentityExpiredException()
    {
        super();
    }


    public IdentityExpiredException( final String message )
    {
        super( message );
    }


    public IdentityExpiredException( final String message, final Throwable cause )
    {
        super( message, cause );
    }


    public IdentityExpiredException( final Throwable cause )
    {
        super( cause );
    }


    @Override
    public String toString()
    {
        return super.toString();
    }
}
