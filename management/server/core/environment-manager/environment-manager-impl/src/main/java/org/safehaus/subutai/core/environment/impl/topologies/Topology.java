package org.safehaus.subutai.core.environment.impl.topologies;


import java.util.List;

import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.core.environment.impl.environment.ContainerDistributionMessage;


/**
 * Created by bahadyr on 11/5/14.
 */
public abstract class Topology
{


    public abstract List<ContainerDistributionMessage> digestBlueprint( final EnvironmentBlueprint blueprint );
}
