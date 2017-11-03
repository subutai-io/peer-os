package io.subutai.core.identity.api.exception;


public class UserExistsException extends Exception
{
    public UserExistsException( final String message )
    {
        super( message );
    }
}
