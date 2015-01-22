package org.safehaus.subutai.core.env.api.exception;


public class EnvironmentNotFoundException extends Exception
{
    public EnvironmentNotFoundException()
    {
    }


    public EnvironmentNotFoundException( final String message )
    {
        super( message );
    }
}
