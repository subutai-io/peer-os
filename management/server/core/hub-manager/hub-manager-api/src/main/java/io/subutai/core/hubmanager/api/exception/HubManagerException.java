package io.subutai.core.hubmanager.api.exception;


public class HubManagerException extends Exception
{
    public HubManagerException( final String message, final Throwable cause )
    {
        super( message, cause );
    }


    public HubManagerException( final String message )
    {
        super( message );
    }


    public HubManagerException( final Throwable cause )
    {
        super( cause );
    }
}
