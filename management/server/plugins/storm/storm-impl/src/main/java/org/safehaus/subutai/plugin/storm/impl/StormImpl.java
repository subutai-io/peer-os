package org.safehaus.subutai.plugin.storm.impl;


import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.EnvironmentBuildTask;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.plugin.storm.api.StormConfig;
import org.safehaus.subutai.plugin.storm.impl.handler.AddNodeHandler;
import org.safehaus.subutai.plugin.storm.impl.handler.DestroyNodeHandler;
import org.safehaus.subutai.plugin.storm.impl.handler.InstallHandler;
import org.safehaus.subutai.plugin.storm.impl.handler.RestartHandler;
import org.safehaus.subutai.plugin.storm.impl.handler.StartHandler;
import org.safehaus.subutai.plugin.storm.impl.handler.StatusHandler;
import org.safehaus.subutai.plugin.storm.impl.handler.StopHandler;
import org.safehaus.subutai.plugin.storm.impl.handler.UninstallHandler;


public class StormImpl extends StormBase
{

    @Override
    public UUID installCluster( StormConfig config )
    {
        AbstractOperationHandler h = new InstallHandler( this, config );
        executor.execute( h );
        return h.getTrackerId();
    }


    @Override
    public UUID uninstallCluster( String clusterName )
    {
        AbstractOperationHandler h = new UninstallHandler( this, clusterName );
        executor.execute( h );
        return h.getTrackerId();
    }


    @Override
    public List<StormConfig> getClusters()
    {
        return pluginDao.getInfo( StormConfig.PRODUCT_NAME, StormConfig.class );
    }


    @Override
    public StormConfig getCluster( String clusterName )
    {
        return pluginDao.getInfo( StormConfig.PRODUCT_NAME, clusterName, StormConfig.class );
    }


    @Override
    public UUID statusCheck( String clusterName, String hostname )
    {
        AbstractOperationHandler h = new StatusHandler( this, clusterName, hostname );
        executor.execute( h );
        return h.getTrackerId();
    }


    @Override
    public UUID startNode( String clusterName, String hostname )
    {
        AbstractOperationHandler h = new StartHandler( this, clusterName, hostname );
        executor.execute( h );
        return h.getTrackerId();
    }


    @Override
    public UUID stopNode( String clusterName, String hostname )
    {
        AbstractOperationHandler h = new StopHandler( this, clusterName, hostname );
        executor.execute( h );
        return h.getTrackerId();
    }


    @Override
    public UUID restartNode( String clusterName, String hostname )
    {
        AbstractOperationHandler h = new RestartHandler( this, clusterName, hostname );
        executor.execute( h );
        return h.getTrackerId();
    }


    @Override
    public UUID addNode( String clusterName )
    {
        AbstractOperationHandler h = new AddNodeHandler( this, clusterName );
        executor.execute( h );
        return h.getTrackerId();
    }


    @Override
    public UUID destroyNode( String clusterName, String hostname )
    {
        AbstractOperationHandler h = new DestroyNodeHandler( this, clusterName, hostname );
        executor.execute( h );
        return h.getTrackerId();
    }


    @Override
    public EnvironmentBuildTask getDefaultEnvironmentBlueprint( StormConfig config )
    {

        EnvironmentBuildTask environmentBuildTask = new EnvironmentBuildTask();

        EnvironmentBlueprint eb = new EnvironmentBlueprint();
        eb.setName( StormConfig.PRODUCT_NAME + UUID.randomUUID() );
        eb.setNodeGroups( new HashSet<NodeGroup>() );

        // no need to create new container for nimbus node if external Zookeeper
        // instance is used as nimbus node
        if ( !config.isExternalZookeeper() )
        {
            NodeGroup nimbus = new NodeGroup();
            nimbus.setName( StormService.NIMBUS.toString() );
            nimbus.setNumberOfNodes( 1 );
            nimbus.setTemplateName( StormConfig.TEMPLATE_NAME_NIMBUS );
            nimbus.setPlacementStrategy( PlacementStrategy.MORE_RAM );
            eb.getNodeGroups().add( nimbus );
        }

        NodeGroup workers = new NodeGroup();
        workers.setName( StormService.SUPERVISOR.toString() );
        workers.setNumberOfNodes( config.getSupervisorsCount() );
        workers.setTemplateName( StormConfig.TEMPLATE_NAME_WORKER );
        workers.setPlacementStrategy( PlacementStrategy.MORE_RAM );
        eb.getNodeGroups().add( workers );

        environmentBuildTask.setEnvironmentBlueprint( eb );

        return environmentBuildTask;
    }


    @Override
    public ClusterSetupStrategy getClusterSetupStrategy( Environment environment, StormConfig config,
                                                         ProductOperation po )
    {

        return new StormSetupStrategyDefault( this, config, environment, po );
    }
}
