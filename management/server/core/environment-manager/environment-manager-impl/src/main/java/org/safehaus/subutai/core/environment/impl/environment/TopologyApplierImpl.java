package org.safehaus.subutai.core.environment.impl.environment;


import java.util.List;

import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.core.environment.impl.topologies.Topology;


/**
 * Created by bahadyr on 11/5/14.
 */
public class TopologyApplierImpl implements TopologyApplier
{
    @Override
    public List<ContainerDistributionMessage> applyTopology( final Topology topology,
                                                             final EnvironmentBlueprint blueprint )
    {
        List<ContainerDistributionMessage> messageList = topology.digestBlueprint( blueprint );
        return messageList;
    }
}
