package io.subutai.common.environment;


import io.subutai.common.protocol.PlacementStrategy;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * Node group
 */
public class NodeGroup
{
    private String name;
    private String templateName;
    private int numberOfContainers;
    private int sshGroupId;
    private int hostsGroupId;
    private PlacementStrategy containerPlacementStrategy;

    //TODO fix all clients and then remove this ctr
    @Deprecated
    public NodeGroup( final String name, final String templateName, final String domainName,
                      final int numberOfContainers, final int sshGroupId, final int hostsGroupId,
                      final PlacementStrategy containerPlacementStrategy )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( name ), "Invalid node group name" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateName ), "Invalid template name" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( domainName ), "Invalid domain name" );
        Preconditions.checkArgument( numberOfContainers > 0, "Number of containers must be greater than 0" );
        Preconditions.checkNotNull( containerPlacementStrategy, "Invalid container placement strategy" );


        this.name = name;
        this.templateName = templateName;
        this.numberOfContainers = numberOfContainers;
        this.sshGroupId = sshGroupId;
        this.hostsGroupId = hostsGroupId;
        this.containerPlacementStrategy = containerPlacementStrategy;
    }


    public NodeGroup( final String name, final String templateName, final int numberOfContainers, final int sshGroupId,
                      final int hostsGroupId, final PlacementStrategy containerPlacementStrategy )
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
        this.containerPlacementStrategy = containerPlacementStrategy;
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
}
