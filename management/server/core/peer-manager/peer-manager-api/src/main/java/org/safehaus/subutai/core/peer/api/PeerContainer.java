package org.safehaus.subutai.core.peer.api;


import java.util.UUID;

import org.safehaus.subutai.common.exception.ContainerException;
import org.safehaus.subutai.common.protocol.Container;


/**
 * Created by timur on 9/20/14.
 */
public class PeerContainer extends Container {
    // UUID of physical agent
    private UUID parentHostId;
    private PeerManager peerManager;


    public UUID getParentHostId()
    {
        return parentHostId;
    }


    public void setParentHostId( final UUID parentHostId )
    {
        this.parentHostId = parentHostId;
    }

    public void setPeerManager(PeerManager peerManager) {
        this.peerManager = peerManager;
    }

    @Override
    public boolean start() throws ContainerException
    {
        return peerManager.startContainer( this );
    }


    @Override
    public boolean stop() throws ContainerException
    {
        return peerManager.stopContainer( this );
    }


    @Override
    public boolean isConnected() throws ContainerException
    {
        return peerManager.isContainerConnected( this );
    }
}
