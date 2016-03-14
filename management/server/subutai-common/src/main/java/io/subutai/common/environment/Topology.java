package io.subutai.common.environment;


import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;


@JsonAutoDetect( fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE )
public class Topology
{
    @JsonProperty( "id" )
    private UUID id = UUID.randomUUID();
    @JsonProperty( "name" )
    private String environmentName;
    @JsonProperty( "placement" )
    private Map<String, Set<Node>> nodeGroupPlacement = Maps.newHashMap();
    @JsonProperty( "subnet" )
    private String subnet;
    @JsonProperty( "sshKey" )
    private String sshKey;


    public Topology( @JsonProperty( "id" ) final UUID id, @JsonProperty( "name" ) final String environmentName,
                     @JsonProperty( "placement" ) final Map<String, Set<Node>> nodeGroupPlacement,
                     @JsonProperty( "subnet" ) final String subnet, @JsonProperty( "sshKey" ) final String sshKey )
    {
        this.id = id;
        this.environmentName = environmentName;
        this.nodeGroupPlacement = nodeGroupPlacement;
        this.subnet = subnet;
        this.sshKey = sshKey;
    }


    public Topology( final UUID id, final String environmentName, final String subnet, final String sshKey )
    {
        this.id = id;
        this.environmentName = environmentName;
        this.subnet = subnet;
        this.sshKey = sshKey;
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


    public void setId( final UUID id )
    {
        this.id = id;
    }
}
