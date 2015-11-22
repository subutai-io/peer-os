package io.subutai.common.environment;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.gson.required.GsonRequired;
import io.subutai.common.gson.required.GsonValidation;
import io.subutai.common.peer.ContainerType;
import io.subutai.common.protocol.PlacementStrategy;


/**
 * Node group
 */
public class NodeGroup
{
    @GsonRequired
    @JsonProperty( "name" )
    private String name;
    @GsonRequired
    @JsonProperty( "templateName" )
    private String templateName;
    @GsonRequired
    @JsonProperty( "type" )
    private ContainerType type = ContainerType.SMALL;
    @GsonRequired( validation = GsonValidation.GREATER_THAN_ZERO )
    @JsonProperty( "numberOfContainers" )
    private int numberOfContainers;

    @GsonRequired
    @JsonProperty( "sshGroupId" )
    private int sshGroupId;
    @GsonRequired
    @JsonProperty( "hostsGroupId" )
    private int hostsGroupId;
    @JsonProperty( "containerPlacementStrategy" )
    private PlacementStrategy containerPlacementStrategy;
    @GsonRequired
    @JsonProperty( "peerId" )
    private String peerId;
    @JsonProperty( "hostId" )
    private String hostId;
    @JsonProperty( "containerDistributionType" )
    private ContainerDistributionType containerDistributionType = ContainerDistributionType.AUTO;


    private NodeGroup()
    {
    }


    public NodeGroup( @JsonProperty( "name" ) final String name,
                      @JsonProperty( "templateName" ) final String templateName,
                      @JsonProperty( "numberOfContainers" ) final int numberOfContainers,
                      @JsonProperty( "type" ) ContainerType type, @JsonProperty( "sshGroupId" ) final int sshGroupId,
                      @JsonProperty( "hostsGroupId" ) final int hostsGroupId,
                      @JsonProperty( "containerDistributionType" ) ContainerDistributionType containerDistributionType,
                      @JsonProperty( "containerPlacementStrategy" ) final PlacementStrategy containerPlacementStrategy,
                      @JsonProperty( "peerId" ) final String peerId, @JsonProperty( "hostId" ) final String hostId )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( name ), "Invalid node group name" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateName ), "Invalid template name" );
        Preconditions.checkArgument( numberOfContainers > 0, "Number of containers must be greater than 0" );
        Preconditions.checkNotNull( type );

        this.name = name;
        this.templateName = templateName;
        this.numberOfContainers = numberOfContainers;
        this.type = type;
        this.sshGroupId = sshGroupId;
        this.hostsGroupId = hostsGroupId;
        this.peerId = peerId;
        this.hostId = hostId;
        this.containerPlacementStrategy = containerPlacementStrategy;
        this.containerDistributionType = containerDistributionType;
    }


    public NodeGroup( final String name, final String templateName, final int numberOfContainers, final int sshGroupId,
                      final int hostsGroupId, final PlacementStrategy containerPlacementStrategy, final String peerId )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( name ), "Invalid node group name" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateName ), "Invalid template name" );
        Preconditions.checkArgument( numberOfContainers > 0, "Number of containers must be greater than 0" );
        Preconditions.checkNotNull( containerPlacementStrategy, "Invalid container placement strategy" );
        Preconditions.checkNotNull( type, "Container type could not be null" );
        Preconditions.checkNotNull( peerId, "Peer could not be null" );

        this.name = name;
        this.templateName = templateName;
        this.numberOfContainers = numberOfContainers;
        this.sshGroupId = sshGroupId;
        this.hostsGroupId = hostsGroupId;
        this.peerId = peerId;
        this.containerPlacementStrategy = containerPlacementStrategy;
        this.containerDistributionType = ContainerDistributionType.AUTO;
    }


    public NodeGroup( final String name, final String templateName, ContainerType type, final int numberOfContainers,
                      final int sshGroupId, final int hostsGroupId, final String peerId, final String hostId )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( name ), "Invalid node group name" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateName ), "Invalid template name" );
        Preconditions.checkArgument( numberOfContainers > 0, "Number of containers must be greater than 0" );
        Preconditions.checkNotNull( type, "Container type could not be null" );
        Preconditions.checkNotNull( peerId, "Peer could not be null" );
        Preconditions.checkNotNull( hostId, "Host could not be null" );

        this.name = name;
        this.templateName = templateName;
        this.type = type;
        this.numberOfContainers = numberOfContainers;
        this.sshGroupId = sshGroupId;
        this.hostsGroupId = hostsGroupId;
        this.peerId = peerId;
        this.hostId = hostId;
        this.containerDistributionType = ContainerDistributionType.CUSTOM;
    }


    public NodeGroup( final String name, final String templateName, final ContainerType type,
                      final int numberOfContainers, final int sshGroupId, final int hostsGroupId )
    {
        this.name = name;
        this.templateName = templateName;
        this.type = type;
        this.numberOfContainers = numberOfContainers;
        this.sshGroupId = sshGroupId;
        this.hostsGroupId = hostsGroupId;
    }


    public String getName()
    {
        return name;
    }


    public int getNumberOfContainers()
    {
        return numberOfContainers;
    }


    public String getTemplateName()
    {
        return templateName;
    }


    public PlacementStrategy getContainerPlacementStrategy()
    {
        return containerPlacementStrategy;
    }


    public int getSshGroupId()
    {
        return sshGroupId;
    }


    public int getHostsGroupId()
    {
        return hostsGroupId;
    }


    public ContainerType getType()
    {
        return type;
    }


    public String getPeerId()
    {
        return peerId;
    }


    public String getHostId()
    {
        return hostId;
    }


    public ContainerDistributionType getContainerDistributionType()
    {
        return containerDistributionType;
    }


    public void setContainerDistributionType( final ContainerDistributionType containerDistributionType )
    {
        this.containerDistributionType = containerDistributionType;
    }
}
