package org.safehaus.subutai.core.env.api.exception;


public class EnvironmentDestructionException extends Exception
{
    public EnvironmentDestructionException( final Throwable cause )
    {
        super( cause );
    }


    public EnvironmentDestructionException( final String message )
    {
        super( message );
    }
}
