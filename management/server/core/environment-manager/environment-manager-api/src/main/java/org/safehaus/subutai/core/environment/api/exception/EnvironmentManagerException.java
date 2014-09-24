package org.safehaus.subutai.core.environment.api.exception;


/**
 * Created by bahadyr on 6/24/14.
 */
public class EnvironmentManagerException extends Exception
{

    private final String message;


    public EnvironmentManagerException( String message )
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
        return "EnvironmentManagerException{" +
                "message='" + message + '\'' +
                '}';
    }
}
