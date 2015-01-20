package org.safehaus.subutai.common.peer;


import org.safehaus.subutai.common.exception.SubutaiException;


/**
 * Exception thrown by peer methods
 */
public class PeerException extends SubutaiException
{
    private String description = "";


    public PeerException( final Throwable cause )
    {
        super( cause );
    }


    public PeerException( final String message )
    {
        super( message );
    }


    public PeerException( final String message, String description )
    {
        super( message );
        this.description = description;
    }


    public String toString()
    {
        return super.toString() + " (" + this.description + ")";
    }
}
