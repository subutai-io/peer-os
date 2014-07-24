package org.safehaus.subutai.api.manager.exception;


/**
 * Created by bahadyr on 6/24/14.
 */
public class EnvironmentBuildException extends EnvironmentManagerException {

    private String message;


    public EnvironmentBuildException( final String message ) {
        super( message );
        this.message = message;
    }


    public String getMessage() {
        return message;
    }


    @Override
    public String toString() {
        return "EnvironmentBuildException{" +
                "message='" + message + '\'' +
                '}';
    }
}
