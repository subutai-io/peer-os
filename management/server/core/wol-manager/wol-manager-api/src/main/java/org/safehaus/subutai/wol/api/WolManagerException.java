package org.safehaus.subutai.wol.api;


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
