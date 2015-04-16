package org.safehaus.subutai.core.peer.api;


import org.safehaus.subutai.common.peer.PeerException;


/**
 *
 */
public class HostNotFoundException extends PeerException
{
    public HostNotFoundException( String msg )
    {
        super( msg );
    }
}
