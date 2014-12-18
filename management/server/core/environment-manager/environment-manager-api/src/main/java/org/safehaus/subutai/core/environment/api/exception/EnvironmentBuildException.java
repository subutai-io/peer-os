package org.safehaus.subutai.core.environment.api.exception;


public class EnvironmentBuildException extends EnvironmentManagerException
{

    private final String message;


    public EnvironmentBuildException( final String message )
    {
        super( message );
        this.message = message;
    }


    @Override
    public String getMessage()
    {
        return message;
    }


    @Override
    public String toString()
    {
        return "EnvironmentBuildException{"
                + "message='" + message + '\''
                + '}';
    }
}

