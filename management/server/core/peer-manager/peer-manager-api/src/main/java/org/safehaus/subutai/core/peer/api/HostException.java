package org.safehaus.subutai.core.peer.api;


import org.safehaus.subutai.common.peer.PeerException;


public class HostException extends PeerException
{
    public HostException( final String message, final String description )
    {
        super( message, description );
    }
}
