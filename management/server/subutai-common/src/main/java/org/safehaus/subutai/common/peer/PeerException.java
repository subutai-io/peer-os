package org.safehaus.subutai.common.peer;


/**
 * Exception thrown by peer methods
 */
public class PeerException extends Exception
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


    public PeerException( final String message, final Throwable cause )
    {
        super( message, cause );
    }


    public String toString()
    {
        return super.toString() + " (" + this.description + ")";
    }
}
