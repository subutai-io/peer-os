package org.safehaus.subutai.core.peer.api.helpers;


import java.util.UUID;


/**
 * Created by timur on 9/20/14.
 */
public class PeerCommandMessage {
    private UUID agentId;
    private UUID peerId;


    public UUID getAgentId()
    {
        return agentId;
    }


    public void setAgentId( final UUID agentId )
    {
        this.agentId = agentId;
    }


    public UUID getPeerId()
    {
        return peerId;
    }


    public void setPeerId( final UUID peerId )
    {
        this.peerId = peerId;
    }
}
