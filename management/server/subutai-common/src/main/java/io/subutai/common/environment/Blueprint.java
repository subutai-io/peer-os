package io.subutai.common.environment;


import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import io.subutai.common.util.CollectionUtil;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;


/**
 * Blueprint for environment creation stores nodeGroups.
 *
 * @see NodeGroup
 */
public class Blueprint
{
    @JsonIgnore
    private UUID id;
    @JsonProperty( "environmentId" )
    private String environmentId;
    @JsonProperty( "name" )
    private String name;
    @JsonProperty( "cidr" )
    private String cidr;
    @JsonProperty( "sshKey" )
    private String sshKey;
    @JsonProperty( "nodegroups" )
    private Set<NodeGroup> nodeGroups;


    public Blueprint( @JsonProperty( "environmentId" ) final String environmentId,
                      @JsonProperty( "name" ) final String name, @JsonProperty( "cidr" ) final String cidr,
                      @JsonProperty( "sshKey" ) final String sshKey,
                      @JsonProperty( "nodegroups" ) final Set<NodeGroup> nodeGroups )
    {
        this.environmentId = environmentId;
        this.name = name;
        this.cidr = cidr;
        this.nodeGroups = nodeGroups;
        this.sshKey = sshKey;
    }


    public Blueprint( final String name, final Set<NodeGroup> nodeGroups )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( name ), "Invalid name" );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( nodeGroups ), "Invalid node group set" );

        this.id = UUID.randomUUID();
        this.name = name;
        this.nodeGroups = nodeGroups;
    }


    public UUID getId()
    {
        return id;
    }


    public void setId( final UUID id )
    {
        this.id = id;
    }


    public String getName()
    {
        return name;
    }


    public Set<NodeGroup> getNodeGroups()
    {
        return nodeGroups == null ? Sets.<NodeGroup>newHashSet() : Collections.unmodifiableSet( nodeGroups );
    }


    @JsonIgnore
    public Map<String, Set<NodeGroup>> getNodeGroupsMap()
    {
        Map<String, Set<NodeGroup>> result = new HashMap<>();

        for ( NodeGroup nodeGroup : nodeGroups )
        {
            String key = nodeGroup.getPeerId();
            Set<NodeGroup> nodes = result.get( key );
            if ( nodes == null )
            {
                nodes = new HashSet<>();
                result.put( key, nodes );
            }
            nodes.add( nodeGroup );
        }
        return result;
    }


    public String getCidr()
    {
        return cidr;
    }


    public String getSshKey()
    {
        return sshKey;
    }


    public String getEnvironmentId()
    {
        return environmentId;
    }
}
