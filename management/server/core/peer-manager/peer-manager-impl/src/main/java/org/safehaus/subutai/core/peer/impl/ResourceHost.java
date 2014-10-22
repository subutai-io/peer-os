package org.safehaus.subutai.core.peer.impl;


import org.safehaus.subutai.common.protocol.PeerCommandMessage;
import org.safehaus.subutai.core.peer.api.Host;
import org.safehaus.subutai.core.peer.api.PeerException;


/**
 * Resource host implementation.
 */
public class ResourceHost extends Host
{
    @Override
    public void invoke( final PeerCommandMessage commandMessage ) throws PeerException
    {
        getParentHost().invoke( commandMessage );
    }


    @Override
    public String getHostname()
    {
        return getAgent() == null ? "Unknown resource host" : getAgent().getHostname();
    }
}
