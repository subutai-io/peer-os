package org.safehaus.subutai.impl.manager.exception;


/**
 * Created by bahadyr on 6/24/14.
 */
public class InstanceCreateException extends EnvironmentManagerException {

    private String message;


    public InstanceCreateException( final String s ) {
        super( s );
        this.message = message;
    }


    public String getMessage() {
        return message;
    }


    @Override
    public String toString() {
        return "InstanceCreateException{" +
                "message='" + message + '\'' +
                '}';
    }
}
