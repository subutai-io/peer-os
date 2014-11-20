package org.safehaus.subutai.core.peer.api;


/**
 * Created by timur on 11/6/14.
 */
public class BindHostException extends PeerException
{
    public BindHostException( String id )
    {
        super( "Could not bind host.", id );
    }
}
