package org.safehaus.subutai.common.environment;


public class EnvironmentModificationException extends Exception
{
    public EnvironmentModificationException( final Throwable cause )
    {
        super( cause );
    }


    public EnvironmentModificationException( final String message )
    {
        super( message );
    }
}
