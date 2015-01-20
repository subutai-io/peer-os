package org.safehaus.subutai.core.peer.api;


import org.safehaus.subutai.common.peer.PeerException;


/**
 *
 */
public class HostNotConnectedException extends PeerException
{
    public HostNotConnectedException( String msg )
    {
        super( msg );
    }
}
