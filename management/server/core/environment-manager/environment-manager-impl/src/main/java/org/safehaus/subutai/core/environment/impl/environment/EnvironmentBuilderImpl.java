package org.safehaus.subutai.core.environment.impl.environment;


import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.core.environment.impl.topologies.Topology;


/**
 * Created by bahadyr on 11/5/14.
 */
public class EnvironmentBuilderImpl implements EnvironmentBuilder, Observer
{
    ContainerDisributor containerDisributor;

    ExecutorService executorService;


    public EnvironmentBuilderImpl()
    {
        executorService = Executors.newSingleThreadExecutor();
    }


    /**
     * Builds environment with a given topology
     */
    public void build( final EnvironmentBlueprint blueprint, Topology topology ) throws BuildException
    {

        TopologyApplier topologyApplier = new TopologyApplierImpl();

        List<ContainerDistributionMessage> messageList = topologyApplier.applyTopology( topology, blueprint );

        EnvironmentBuilderThread builderThread = new EnvironmentBuilderThread( this, messageList );


    }


    @Override
    public void update( final Observable o, final Object arg )
    {

    }
}
