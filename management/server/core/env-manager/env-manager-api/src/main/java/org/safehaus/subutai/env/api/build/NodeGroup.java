package org.safehaus.subutai.env.api.build;


import org.safehaus.subutai.common.protocol.PlacementStrategy;


/**
 * Node group
 */
public interface NodeGroup
{

    public String getName();

    public int getNumberOfNodes();

    public String getTemplateName();

    public PlacementStrategy getNodePlacementStrategy();

    public String getDomainName();

    public int getSshGroupId();

    public int getHostsGroupId();
}
