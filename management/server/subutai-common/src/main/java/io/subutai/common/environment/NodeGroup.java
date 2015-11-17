package io.subutai.common.environment;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.gson.required.GsonRequired;
import io.subutai.common.gson.required.GsonValidation;
import io.subutai.common.host.HostId;
import io.subutai.common.peer.PeerId;
import io.subutai.common.protocol.PlacementStrategy;


/**
 * Node group
 */
public class NodeGroup
{
    @GsonRequired
    private String name;
    @GsonRequired
    private String templateName;
    @GsonRequired
    private ContainerType type;
    @GsonRequired( validation = GsonValidation.GREATER_THAN_ZERO )
    private int numberOfContainers;

    @GsonRequired
    private int sshGroupId;
    @GsonRequired
    private int hostsGroupId;
    private PlacementStrategy containerPlacementStrategy;
    @GsonRequired
    private String peerId;
    private String hostId;
    private ContainerDistributionType containerDistributionType = ContainerDistributionType.AUTO;


    public NodeGroup( final String name, final String templateName, final int numberOfContainers, final int sshGroupId,
                      final int hostsGroupId, final PlacementStrategy containerPlacementStrategy, final String peerId )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( name ), "Invalid node group name" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateName ), "Invalid template name" );
        Preconditions.checkArgument( numberOfContainers > 0, "Number of containers must be greater than 0" );
        Preconditions.checkNotNull( containerPlacementStrategy, "Invalid container placement strategy" );


        this.name = name;
        this.templateName = templateName;
        this.numberOfContainers = numberOfContainers;
        this.sshGroupId = sshGroupId;
        this.hostsGroupId = hostsGroupId;
        this.peerId = peerId;
        this.containerPlacementStrategy = containerPlacementStrategy;
        this.containerDistributionType = ContainerDistributionType.AUTO;
    }


    public NodeGroup( final String name, final String templateName, final ContainerType type,
                      final int numberOfContainers, final int sshGroupId, final int hostsGroupId, final String peerId,
                      final String hostId )
    {
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
        Preconditions.checkArgument( !Strings.isNullOrEmpty( name ), "Invalid node group name" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateName ), "Invalid template name" );
        Preconditions.checkArgument( numberOfContainers > 0, "Number of containers must be greater than 0" );

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
