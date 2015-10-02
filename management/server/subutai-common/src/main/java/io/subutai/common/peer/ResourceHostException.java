package io.subutai.common.peer;


public class ResourceHostException extends Exception
{
    public ResourceHostException( final String message )
    {
        super( message );
    }


    public ResourceHostException( final Throwable cause )
    {
        super( cause );
    }


    public ResourceHostException( final String message, final Throwable cause )
    {
        super( message, cause );
    }
}
