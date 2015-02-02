package org.safehaus.subutai.common.environment;


import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.common.settings.Common;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * Node group
 */
public class NodeGroup
{
    private String name;
    private String templateName;
    private String domainName;
    private int numberOfContainers;
    private int sshGroupId;
    private int hostsGroupId;
    private PlacementStrategy containerPlacementStrategy;


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
        this.domainName = domainName;
        this.numberOfContainers = numberOfContainers;
        this.sshGroupId = sshGroupId;
        this.hostsGroupId = hostsGroupId;
        this.containerPlacementStrategy = containerPlacementStrategy;
    }


    public NodeGroup( final String name, final String templateName, final int numberOfContainers, final int sshGroupId,
                      final int hostsGroupId, final PlacementStrategy containerPlacementStrategy )
    {
        this( name, templateName, Common.DEFAULT_DOMAIN_NAME, numberOfContainers, sshGroupId, hostsGroupId,
                containerPlacementStrategy );
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


    public String getDomainName()
    {
        return domainName;
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
