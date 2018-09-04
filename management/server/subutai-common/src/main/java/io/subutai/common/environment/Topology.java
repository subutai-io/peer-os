package io.subutai.common.environment;


import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.annotations.SerializedName;

import io.subutai.common.security.SshEncryptionType;


@JsonAutoDetect( fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE )
public class Topology
{
    @JsonProperty( "id" )
    private UUID id = UUID.randomUUID();

    @JsonProperty( "name" )
    @SerializedName( "name" )
    private String environmentName;

    @JsonProperty( "placement" )
    @SerializedName( "placement" )
    private Map<String, Set<Node>> nodeGroupPlacement = Maps.newHashMap();

    @JsonProperty( "sshKey" )
    private String sshKey;

    @JsonProperty( "exchangeSshKeys" )
    private boolean exchangeSshKeys = true;

    @JsonProperty( "registerHosts" )
    private boolean registerHosts = true;

    @JsonProperty( "sshKeyType" )
    private SshEncryptionType sshKeyType = SshEncryptionType.RSA;


    public Topology( @JsonProperty( "id" ) final UUID id, @JsonProperty( "name" ) final String environmentName,
                     @JsonProperty( "placement" ) final Map<String, Set<Node>> nodeGroupPlacement,
                     @JsonProperty( "sshKey" ) final String sshKey )
    {
        this.id = id;
        this.environmentName = environmentName;
        this.nodeGroupPlacement = nodeGroupPlacement;
        this.sshKey = sshKey;
        this.sshKeyType = SshEncryptionType.parseTypeFromKey( sshKey );
    }


    public Topology( final UUID id, final String environmentName, final String sshKey )
    {
        this.id = id;
        this.environmentName = environmentName;
        this.sshKey = sshKey;
        this.sshKeyType = SshEncryptionType.parseTypeFromKey( sshKey );
    }


    public Topology( final String environmentName )
    {
        this.id = UUID.randomUUID();
        this.environmentName = environmentName;
    }


    public UUID getId()
    {
        return id;
    }


    public String getEnvironmentName()
    {
        return environmentName;
    }


    public void setEnvironmentName( String environmentName )
    {
        this.environmentName = environmentName;
    }


    public Map<String, Set<Node>> getNodeGroupPlacement()
    {
        return Collections.unmodifiableMap( nodeGroupPlacement );
    }


    public Set<Node> getPeerNodes( String peerId )
    {
        for ( Map.Entry<String, Set<Node>> placementEntry : nodeGroupPlacement.entrySet() )
        {
            if ( placementEntry.getKey().equalsIgnoreCase( peerId ) )
            {
                return placementEntry.getValue();
            }
        }

        return Sets.newHashSet();
    }


    public Set<String> getAllPeers()
    {
        return Collections.unmodifiableSet( nodeGroupPlacement.keySet() );
    }


    public Map<String, Set<String>> getPeerRhIds()
    {
        Map<String, Set<String>> peersRhIds = Maps.newHashMap();

        for ( Map.Entry<String, Set<Node>> placementEntry : nodeGroupPlacement.entrySet() )
        {
            Set<String> peerRhIds = peersRhIds.get( placementEntry.getKey() );

            if ( peerRhIds == null )
            {
                peerRhIds = Sets.newHashSet();
                peersRhIds.put( placementEntry.getKey(), peerRhIds );
            }
            for ( Node node : placementEntry.getValue() )
            {
                peerRhIds.add( node.getHostId() );
            }
        }

        return peersRhIds;
    }


    public void addNodePlacement( String peerId, Node node )
    {
        Preconditions.checkNotNull( peerId, "Invalid peer" );
        Preconditions.checkNotNull( node, "Invalid node group" );

        if ( StringUtils.isEmpty( node.getHostname() ) )
        {
            node.setHostname( node.getName().replaceAll( "\\s+", "" ) );
        }

        Set<Node> peerNodes = nodeGroupPlacement.get( peerId );

        if ( peerNodes == null )
        {
            peerNodes = Sets.newHashSet();
            nodeGroupPlacement.put( peerId, peerNodes );
        }

        peerNodes.add( node );
    }


    public void addAllNodePlacement( final Collection<Node> nodes )
    {
        for ( Node node : nodes )
        {
            addNodePlacement( node.getPeerId(), node );
        }
    }


    public Set<Node> removePlacement( String peerId )
    {
        return nodeGroupPlacement.remove( peerId );
    }


    public String getSshKey()
    {
        return sshKey;
    }


    public void setSshKey( final String sshKey )
    {
        this.sshKey = sshKey;
    }


    public void setId( final UUID id )
    {
        this.id = id;
    }


    public boolean exchangeSshKeys()
    {
        return exchangeSshKeys;
    }


    public void setExchangeSshKeys( final boolean exchangeSshKeys )
    {
        this.exchangeSshKeys = exchangeSshKeys;
    }


    public boolean registerHosts()
    {
        return registerHosts;
    }


    public void setRegisterHosts( final boolean registerHosts )
    {
        this.registerHosts = registerHosts;
    }


    public SshEncryptionType getSshKeyType()
    {
        return sshKeyType == null ? SshEncryptionType.UNKNOWN : sshKeyType;
    }


    public void setSshKeyType( final SshEncryptionType sshKeyType )
    {
        this.sshKeyType = sshKeyType;
    }


    @Override
    public String toString()
    {
        return "Topology{" + "id=" + id + ", environmentName='" + environmentName + '\'' + ", nodeGroupPlacement="
                + nodeGroupPlacement + ", sshKey='" + sshKey + '\'' + '}';
    }
}
