package org.safehaus.subutai.plugin.storm.impl;


import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.plugin.storm.api.StormClusterConfiguration;
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

    public StormImpl( DataSource dataSource )
    {
        this.dataSource = dataSource;
    }


    @Override
    public UUID installCluster( StormClusterConfiguration config )
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
    public List<StormClusterConfiguration> getClusters()
    {
        return pluginDAO.getInfo( StormClusterConfiguration.PRODUCT_NAME, StormClusterConfiguration.class );
    }


    @Override
    public StormClusterConfiguration getCluster( String clusterName )
    {
        return pluginDAO.getInfo( StormClusterConfiguration.PRODUCT_NAME, clusterName, StormClusterConfiguration.class );
    }


    @Override
    public UUID addNode( final String clusterName, final String agentHostName )
    {
        return null;
    }


    @Override
    public UUID checkNode( String clusterName, String hostname )
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
    public EnvironmentBlueprint getDefaultEnvironmentBlueprint( StormClusterConfiguration config )
    {

        EnvironmentBlueprint environmentBlueprint = new EnvironmentBlueprint();
        environmentBlueprint.setName( StormClusterConfiguration.PRODUCT_NAME + UUIDUtil.generateTimeBasedUUID() );
        environmentBlueprint.setNodeGroups( new HashSet<NodeGroup>() );

        // no need to create new container for nimbus node if external Zookeeper
        // instance is used as nimbus node
        if ( !config.isExternalZookeeper() )
        {
            NodeGroup nimbus = new NodeGroup();
            nimbus.setName( StormService.NIMBUS.toString() );
            nimbus.setNumberOfNodes( 1 );
            nimbus.setTemplateName( StormClusterConfiguration.TEMPLATE_NAME );
            nimbus.setPlacementStrategy( PlacementStrategy.MORE_RAM );
            environmentBlueprint.getNodeGroups().add( nimbus );
        }

        NodeGroup workers = new NodeGroup();
        workers.setName( StormService.SUPERVISOR.toString() );
        workers.setNumberOfNodes( config.getSupervisorsCount() );
        workers.setTemplateName( StormClusterConfiguration.TEMPLATE_NAME );
        workers.setPlacementStrategy( PlacementStrategy.MORE_RAM );
        environmentBlueprint.getNodeGroups().add( workers );


        return environmentBlueprint;
    }


    @Override
    public ClusterSetupStrategy getClusterSetupStrategy( Environment environment, StormClusterConfiguration config,
                                                         TrackerOperation po )
    {

        return new StormSetupStrategyDefault( this, config, environment, po );
    }
}
