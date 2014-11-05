package org.safehaus.subutai.core.environment.impl.environment;


import java.util.List;

import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.core.environment.impl.topologies.Topology;


/**
 * Created by bahadyr on 11/5/14.
 */
public interface TopologyApplicator
{

    List<ContainerDistributionMessage> applyTopology( Topology topology, EnvironmentBlueprint blueprint );
}
