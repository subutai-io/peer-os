package org.safehaus.subutai.core.env.api.build;


import org.safehaus.subutai.common.protocol.PlacementStrategy;

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
    private int numberOfNodes;
    private int sshGroupId;
    private int hostsGroupId;
    private PlacementStrategy nodePlacementStrategy;


    public NodeGroup( final String name, final String templateName, final String domainName, final int numberOfNodes,
                      final int sshGroupId, final int hostsGroupId, final PlacementStrategy nodePlacementStrategy )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( name ), "Invalid node group name" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateName ), "Invalid template name" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( domainName ), "Invalid domain name" );
        Preconditions.checkArgument( numberOfNodes > 0, "Number of nodes must be greater than 0" );
        Preconditions.checkNotNull( nodePlacementStrategy, "Invalid node placement strategy" );


        this.name = name;
        this.templateName = templateName;
        this.domainName = domainName;
        this.numberOfNodes = numberOfNodes;
        this.sshGroupId = sshGroupId;
        this.hostsGroupId = hostsGroupId;
        this.nodePlacementStrategy = nodePlacementStrategy;
    }


    public String getName()
    {
        return name;
    }


    public int getNumberOfNodes()
    {
        return numberOfNodes;
    }


    public String getTemplateName()
    {
        return templateName;
    }


    public PlacementStrategy getNodePlacementStrategy()
    {
        return nodePlacementStrategy;
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
