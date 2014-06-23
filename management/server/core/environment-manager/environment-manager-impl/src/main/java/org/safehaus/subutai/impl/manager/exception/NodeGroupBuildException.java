package org.safehaus.subutai.impl.manager.exception;


/**
 * Created by bahadyr on 6/24/14.
 */
public class NodeGroupBuildException extends EnvironmentManagerException {

    private String message;

    public NodeGroupBuildException( final String s ) {
        super(s);
        this.message = s;
    }


    public String getMessage() {
        return message;
    }


    @Override
    public String toString() {
        return "NodeGroupBuildException{" +
                "message='" + message + '\'' +
                '}';
    }
}
