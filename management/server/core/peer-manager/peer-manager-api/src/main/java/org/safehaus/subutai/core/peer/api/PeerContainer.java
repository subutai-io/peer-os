package org.safehaus.subutai.core.peer.api;


import java.util.UUID;

import org.safehaus.subutai.common.exception.ContainerException;
import org.safehaus.subutai.common.protocol.Container;
import org.safehaus.subutai.common.protocol.DefaultCommandMessage;
import org.safehaus.subutai.common.protocol.PeerCommandType;


/**
 * Created by timur on 9/20/14.
 */
public class PeerContainer extends Container
{
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


    public void setPeerManager( PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    @Override
    public UUID getEnvironmentId()
    {
        return null;
    }


    @Override
    public DefaultCommandMessage start() throws ContainerException
    {
        peerManager.startContainer( this );
        return new DefaultCommandMessage( PeerCommandType.START, null, null, null );
    }


    @Override
    public DefaultCommandMessage stop() throws ContainerException
    {
        peerManager.stopContainer( this );
        return new DefaultCommandMessage( PeerCommandType.STOP, null, null, null );
    }


    @Override
    public DefaultCommandMessage isConnected() throws ContainerException
    {
        peerManager.isContainerConnected( this );
        return new DefaultCommandMessage( PeerCommandType.IS_CONNECTED, null, null, null );
    }
}
