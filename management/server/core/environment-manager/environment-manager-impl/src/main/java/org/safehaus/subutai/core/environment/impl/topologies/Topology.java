package org.safehaus.subutai.core.environment.impl.topologies;


import java.util.List;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.core.environment.impl.environment.ContainerDistributionMessage;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;


/**
 * Created by bahadyr on 11/5/14.
 */
public abstract class Topology
{
    public final TemplateRegistry templateRegistry;


    protected Topology( final TemplateRegistry templateRegistry )
    {
        this.templateRegistry = templateRegistry;
    }


    public abstract List<ContainerDistributionMessage> digestBlueprint( final EnvironmentBlueprint blueprint,
                                                                        UUID environmentId );
}
