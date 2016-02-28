package io.subutai.common.environment;


import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;


public class Topology
{
    private UUID id = UUID.randomUUID();
    private final Map<String, Set<Node>> nodeGroupPlacement = Maps.newHashMap();
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


    public Map<String, Set<Node>> getNodeGroupPlacement()
    {
        return Collections.unmodifiableMap( nodeGroupPlacement );
    }


    public Set<String> getAllPeers()
    {
        return Collections.unmodifiableSet( nodeGroupPlacement.keySet() );
    }


    public void addNodePlacement( String peerId, Node node )
    {
        Preconditions.checkNotNull( peerId, "Invalid peer" );
        Preconditions.checkNotNull( node, "Invalid node group" );

        if ( StringUtils.isEmpty( node.getHostname() ) )
        {
            node.setHostname( UUID.randomUUID().toString() );
        }

        Set<Node> peerNodes = nodeGroupPlacement.get( peerId );

        if ( peerNodes == null )
        {
            peerNodes = Sets.newHashSet();
            nodeGroupPlacement.put( peerId, peerNodes );
        }

        peerNodes.add( node );
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
