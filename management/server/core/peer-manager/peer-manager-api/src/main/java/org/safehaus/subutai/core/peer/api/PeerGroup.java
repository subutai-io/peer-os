package org.safehaus.subutai.core.peer.api;


import java.util.HashSet;
import java.util.Objects;
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


    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode( this.id );
        return hash;
    }


    @Override
    public boolean equals( Object obj )
    {
        if ( obj instanceof PeerGroup )
        {
            PeerGroup other = ( PeerGroup ) obj;
            return Objects.equals( this.id, other.id );
        }
        return false;
    }


}

