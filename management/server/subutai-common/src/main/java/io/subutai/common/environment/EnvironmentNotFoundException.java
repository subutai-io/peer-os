package io.subutai.common.environment;


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
