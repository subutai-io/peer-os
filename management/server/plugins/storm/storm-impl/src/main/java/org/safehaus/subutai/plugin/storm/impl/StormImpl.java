package org.safehaus.subutai.plugin.storm.impl;


import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.plugin.common.api.ClusterOperationType;
import org.safehaus.subutai.plugin.common.api.NodeOperationType;
import org.safehaus.subutai.plugin.common.api.NodeType;
import org.safehaus.subutai.plugin.storm.api.StormClusterConfiguration;
import org.safehaus.subutai.plugin.storm.impl.handler.StormClusterOperationHandler;
import org.safehaus.subutai.plugin.storm.impl.handler.StormNodeOperationHandler;


public class StormImpl extends StormBase
{

    public StormImpl( DataSource dataSource )
    {
        this.dataSource = dataSource;
    }


    @Override
    public UUID installCluster( StormClusterConfiguration config )
    {
        AbstractOperationHandler h = new StormClusterOperationHandler( this, config, ClusterOperationType.INSTALL );
        executor.execute( h );
        return h.getTrackerId();
    }


    @Override
    public UUID uninstallCluster( String clusterName )
    {
        AbstractOperationHandler h = new StormClusterOperationHandler( this, getCluster( clusterName ), ClusterOperationType.UNINSTALL );
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
        AbstractOperationHandler h = new StormNodeOperationHandler( this, clusterName, hostname,
                NodeOperationType.STATUS );
        executor.execute( h );
        return h.getTrackerId();
    }


    @Override
    public UUID startNode( String clusterName, String hostname )
    {
        AbstractOperationHandler h = new StormNodeOperationHandler( this, clusterName, hostname,
                NodeOperationType.START );
        executor.execute( h );
        return h.getTrackerId();
    }


    @Override
    public UUID stopNode( String clusterName, String hostname )
    {
        AbstractOperationHandler h = new StormNodeOperationHandler( this, clusterName, hostname,
                NodeOperationType.STOP );
        executor.execute( h );
        return h.getTrackerId();
    }


    @Override
    public UUID restartNode( String clusterName, String hostname )
    {
        AbstractOperationHandler h = new StormNodeOperationHandler( this, clusterName, hostname,
                NodeOperationType.RESTART );
        executor.execute( h );
        return h.getTrackerId();
    }


    @Override
    public UUID addNode( String clusterName )
    {
        StormClusterConfiguration zookeeperClusterConfig = getCluster( clusterName );

        AbstractOperationHandler h = new StormClusterOperationHandler( this, zookeeperClusterConfig,
                ClusterOperationType.ADD );
        executor.execute( h );
        return h.getTrackerId();
    }


    @Override
    public UUID destroyNode( String clusterName, String hostname )
    {
        StormClusterConfiguration zookeeperClusterConfig = getCluster( clusterName );

        AbstractOperationHandler h = new StormClusterOperationHandler( this, zookeeperClusterConfig, hostname,
                ClusterOperationType.ADD );
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
            nimbus.setLinkHosts( false );
            nimbus.setExchangeSshKeys( false );
            nimbus.setNumberOfNodes( 1 );
            nimbus.setTemplateName( StormClusterConfiguration.TEMPLATE_NAME );
            nimbus.setPlacementStrategy( StormSetupStrategyDefault.getNodePlacementStrategyByNodeType(
                    NodeType.STORM_NIMBUS ) );
            environmentBlueprint.getNodeGroups().add( nimbus );
        }

        NodeGroup workers = new NodeGroup();
        workers.setName( StormService.SUPERVISOR.toString() );
        workers.setLinkHosts( false );
        workers.setExchangeSshKeys( false );
        workers.setNumberOfNodes( config.getSupervisorsCount() );
        workers.setTemplateName( StormClusterConfiguration.TEMPLATE_NAME );
        workers.setPlacementStrategy( StormSetupStrategyDefault.getNodePlacementStrategyByNodeType(
                NodeType.STORM_SUPERVISOR ) );
        environmentBlueprint.getNodeGroups().add( workers );


        return environmentBlueprint;
    }


    @Override
    public ClusterSetupStrategy getClusterSetupStrategy( Environment environment, StormClusterConfiguration config,
                                                         TrackerOperation po )
    {

        return new StormSetupStrategyDefault( this, config, environment, po, environmentManager );
    }
}
