package org.safehaus.subutai.core.environment.impl.topologies;


import java.util.List;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.core.environment.impl.environment.ContainerDistributionMessage;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;


/**
 * Created by bahadyr on 11/5/14.
 */
public class ContainerToPeerTopology extends Topology
{


    public ContainerToPeerTopology( final TemplateRegistry templateRegistry )
    {
        super( templateRegistry );
    }


    @Override
    public List<ContainerDistributionMessage> digestBlueprint( final EnvironmentBlueprint blueprint,
                                                               final UUID environmentId )
    {
        return null;
    }
}
