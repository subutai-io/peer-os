package org.safehaus.subutai.core.env.impl;


import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.core.env.api.build.NodeGroup;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * NodeGroup implementation
 */
public class NodeGroupImpl implements NodeGroup
{
    private String name;
    private String templateName;
    private String domainName;
    private int numberOfNodes;
    private int sshGroupId;
    private int hostsGroupId;
    private PlacementStrategy nodePlacementStrategy;


    public NodeGroupImpl( final String name, final String templateName, final String domainName,
                          final int numberOfNodes, final int sshGroupId, final int hostsGroupId,
                          final PlacementStrategy nodePlacementStrategy )
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


    @Override
    public String getName()
    {
        return name;
    }


    @Override
    public int getNumberOfNodes()
    {
        return numberOfNodes;
    }


    @Override
    public String getTemplateName()
    {
        return templateName;
    }


    @Override
    public PlacementStrategy getNodePlacementStrategy()
    {
        return nodePlacementStrategy;
    }


    @Override
    public String getDomainName()
    {
        return domainName;
    }


    @Override
    public int getSshGroupId()
    {
        return sshGroupId;
    }


    @Override
    public int getHostsGroupId()
    {
        return hostsGroupId;
    }
}
