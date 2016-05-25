package io.subutai.common.security.exception;


/**
 *  General Subutai Security Exception
 */
public class SystemSecurityException extends SecurityException
{
    public SystemSecurityException()
    {
        super();
    }


    public SystemSecurityException( final String message )
    {
        super( message );
    }


    public SystemSecurityException( final String message, final Throwable cause )
    {
        super( message, cause );
    }


    public SystemSecurityException( final Throwable cause )
    {
        super( cause );
    }


    @Override
    public String toString()
    {
        return super.toString();
    }

}
