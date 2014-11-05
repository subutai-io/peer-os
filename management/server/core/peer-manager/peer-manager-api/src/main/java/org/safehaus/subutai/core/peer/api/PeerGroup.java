package org.safehaus.subutai.core.peer.api;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.util.UUIDUtil;


/**
 * Peer group
 */
public class PeerGroup
{
    Set<UUID> peerIds;
    private String name;
    private UUID id;


    public PeerGroup()
    {
        this.peerIds = new HashSet<>();
        this.id = UUIDUtil.generateTimeBasedUUID();
    }


    public UUID getId()
    {
        return id;
    }


    public void setId( final UUID id )
    {
        this.id = id;
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
        return id;
    }
}
