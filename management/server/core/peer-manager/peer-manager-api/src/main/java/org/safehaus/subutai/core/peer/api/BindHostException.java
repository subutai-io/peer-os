package org.safehaus.subutai.core.peer.api;


import java.util.UUID;


/**
 * Created by timur on 11/6/14.
 */
public class BindHostException extends PeerException
{
    public BindHostException( UUID id )
    {
        super( "Could not bind host.", id.toString() );
    }
}
