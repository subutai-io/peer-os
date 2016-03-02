package io.subutai.common.environment;


import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.subutai.common.peer.Peer;


public class Topology
{
    private UUID id = UUID.randomUUID();
    private final Map<String, Set<NodeGroup>> nodeGroupPlacement = Maps.newHashMap();
    private String environmentName;
    private String subnet;
    private String sshKey;
    private int sshGroupId;
    private int hostGroupId;


    public Topology( final String environmentName, final int sshGroupId, final int hostGroupId )
    {
        this.environmentName = environmentName;
        this.sshGroupId = sshGroupId;
        this.hostGroupId = hostGroupId;
    }


    public UUID getId()
    {
        return id;
    }


    public String getEnvironmentName()
    {
        return environmentName;
    }


    public Map<String, Set<NodeGroup>> getNodeGroupPlacement()
    {
        return Collections.unmodifiableMap( nodeGroupPlacement );
    }


    public Set<String> getAllPeers()
    {
        return Collections.unmodifiableSet( nodeGroupPlacement.keySet() );
    }


    public void addNodeGroupPlacement( String peerId, NodeGroup nodeGroup )
    {
        Preconditions.checkNotNull( peerId, "Invalid peer" );
        Preconditions.checkNotNull( nodeGroup, "Invalid node group" );

        Set<NodeGroup> peerNodeGroups = nodeGroupPlacement.get( peerId );

        if ( peerNodeGroups == null )
        {
            peerNodeGroups = Sets.newHashSet();
            nodeGroupPlacement.put( peerId, peerNodeGroups );
        }

        peerNodeGroups.add( nodeGroup );
    }


    public String getSshKey()
    {
        return sshKey;
    }


    public void setSshKey( final String sshKey )
    {
        this.sshKey = sshKey;
    }


    public String getSubnet()
    {
        return subnet;
    }


    public void setSubnet( final String subnet )
    {
        this.subnet = subnet;
    }


    public int getSshGroupId()
    {
        return sshGroupId;
    }


    public int getHostGroupId()
    {
        return hostGroupId;
    }


    public void setId( final UUID id )
    {
        this.id = id;
    }
}
