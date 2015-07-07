package io.subutai.core.wol.api;


public class WolManagerException extends Exception
{
    public WolManagerException( final String message )
    {
        super( message );
    }


    public WolManagerException( final Throwable cause )
    {
        super( cause );
    }
}
