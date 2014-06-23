package org.safehaus.subutai.impl.manager.exception;


/**
 * Created by bahadyr on 6/24/14.
 */
public class EnvironmentManagerException extends Exception {

    private String message = null;


    public EnvironmentManagerException() {
        super();
    }


    public EnvironmentManagerException( String message ) {
        super( message );
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }

    public String getMessage() {
        return message;
    }
}
