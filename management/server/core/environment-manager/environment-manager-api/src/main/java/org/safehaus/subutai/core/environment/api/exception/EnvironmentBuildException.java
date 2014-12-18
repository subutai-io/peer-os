package org.safehaus.subutai.core.environment.api.exception;


/**
 * Created by bahadyr on 6/24/14.
 */
public class EnvironmentBuildException extends EnvironmentManagerException
{

    private final String message;




    public EnvironmentBuildException( final String message )
    {
        super( message );
        this.message = message;
    }


    public String getMessage()
    {
        return message;
    }


    @Override
    public String toString()
    {
        return "EnvironmentBuildException{" +
                "message='" + message + '\'' +
                '}';
    }
}
