package org.safehaus.subutai.core.peer.api;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


/**
 * Created by bahadyr on 10/8/14.
 */
public class PeerGroup
{
    Set<UUID> peerIds;
    private String name;
    private UUID uuid;


    public PeerGroup()
    {
        this.peerIds = new HashSet<>();
        this.uuid = UUID.randomUUID();
    }


    public UUID getUuid()
    {
        return uuid;
    }


    public void setUuid( final UUID uuid )
    {
        this.uuid = uuid;
    }


    public Set<UUID> getPeerIds()
    {
        return peerIds;
    }


    public void setPeerIds( final Set<UUID> peerIds )
    {
        this.peerIds = peerIds;
    }


    public void addPeerUUID( final UUID uuid )
    {
        this.peerIds.add( uuid );
    }


    public String getName()
    {
        return name;
    }


    public void setName( final String name )
    {
        this.name = name;
    }


    public UUID getUUID()
    {
        return uuid;
    }
}
