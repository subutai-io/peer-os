package io.subutai.common.security.exception;


/**
 * Invalid Username or password (Token verification failed)
 */
public class InvalidLoginException extends SystemSecurityException
{

    public InvalidLoginException()
    {
        super();
    }


    public InvalidLoginException( final String message )
    {
        super( message );
    }


    public InvalidLoginException( final String message, final Throwable cause )
    {
        super( message, cause );
    }


    public InvalidLoginException( final Throwable cause )
    {
        super( cause );
    }


    @Override
    public String toString()
    {
        return super.toString();
    }
}
