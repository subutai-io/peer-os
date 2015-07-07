package io.subutai.core.peer.api;


import io.subutai.common.peer.PeerException;


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
