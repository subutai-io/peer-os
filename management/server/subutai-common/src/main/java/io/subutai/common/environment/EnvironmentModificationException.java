package io.subutai.common.environment;

//todo move to io.subutai.core.environment.api.exception package
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
