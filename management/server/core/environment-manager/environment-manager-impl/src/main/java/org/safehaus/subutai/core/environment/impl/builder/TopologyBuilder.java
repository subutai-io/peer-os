package org.safehaus.subutai.core.environment.impl.builder;


import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;
import org.safehaus.subutai.core.environment.api.topology.TopologyData;
import org.safehaus.subutai.core.environment.impl.EnvironmentManagerImpl;


/**
 * Created by bahadyr on 10/21/14.
 */
public abstract class TopologyBuilder
{

    EnvironmentManagerImpl environmentManager;


    public TopologyBuilder( final EnvironmentManagerImpl environmentManager )
    {
        this.environmentManager = environmentManager;
    }


    public abstract EnvironmentBuildProcess prepareBuildProcess( TopologyData topologyData ) throws TopologyBuilderException;
}
