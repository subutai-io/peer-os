package org.safehaus.subutai.plugin.spark.impl;


import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.spark.api.SetupType;
import org.safehaus.subutai.plugin.spark.api.Spark;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.plugin.spark.impl.handler.AddSlaveNodeOperationHandler;
import org.safehaus.subutai.plugin.spark.impl.handler.ChangeMasterNodeOperationHandler;
import org.safehaus.subutai.plugin.spark.impl.handler.CheckNodeOperationHandler;
import org.safehaus.subutai.plugin.spark.impl.handler.DestroySlaveNodeOperationHandler;
import org.safehaus.subutai.plugin.spark.impl.handler.InstallOperationHandler;
import org.safehaus.subutai.plugin.spark.impl.handler.StartClusterOperationHandler;
import org.safehaus.subutai.plugin.spark.impl.handler.StartNodeOperationHandler;
import org.safehaus.subutai.plugin.spark.impl.handler.StopClusterOperationHandler;
import org.safehaus.subutai.plugin.spark.impl.handler.StopNodeOperationHandler;
import org.safehaus.subutai.plugin.spark.impl.handler.UninstallOperationHandler;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;


public class SparkImpl extends SparkBase implements Spark
{

    public SparkImpl( final DataSource dataSource, final Tracker tracker, final EnvironmentManager environmentManager,
                      final Hadoop hadoopManager )
    {
        super( dataSource, tracker, environmentManager, hadoopManager );
    }


    @Override
    public UUID installCluster( final SparkClusterConfig config )
    {

        Preconditions.checkNotNull( config, "Configuration is null" );

        AbstractOperationHandler operationHandler = new InstallOperationHandler( this, config );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public UUID uninstallCluster( final String clusterName )
    {

        AbstractOperationHandler operationHandler = new UninstallOperationHandler( this, clusterName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public List<SparkClusterConfig> getClusters()
    {
        return pluginDAO.getInfo( SparkClusterConfig.PRODUCT_KEY, SparkClusterConfig.class );
    }


    @Override
    public SparkClusterConfig getCluster( String clusterName )
    {
        return pluginDAO.getInfo( SparkClusterConfig.PRODUCT_KEY, clusterName, SparkClusterConfig.class );
    }


    @Override
    public UUID addNode( final String clusterName, final String agentHostName )
    {
        return null;
    }


    @Override
    public UUID installCluster( SparkClusterConfig config, HadoopClusterConfig hadoopConfig )
    {
        InstallOperationHandler operationHandler = new InstallOperationHandler( this, config );
        operationHandler.setHadoopConfig( hadoopConfig );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public UUID addSlaveNode( final String clusterName, final String lxcHostname )
    {

        AbstractOperationHandler operationHandler = new AddSlaveNodeOperationHandler( this, clusterName, lxcHostname );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public UUID destroySlaveNode( final String clusterName, final String lxcHostname )
    {

        AbstractOperationHandler operationHandler =
                new DestroySlaveNodeOperationHandler( this, clusterName, lxcHostname );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public UUID changeMasterNode( final String clusterName, final String newMasterHostname, final boolean keepSlave )
    {

        AbstractOperationHandler operationHandler =
                new ChangeMasterNodeOperationHandler( this, clusterName, newMasterHostname, keepSlave );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public UUID startNode( final String clusterName, final String lxcHostname, final boolean master )
    {

        AbstractOperationHandler operationHandler =
                new StartNodeOperationHandler( this, clusterName, lxcHostname, master );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public UUID startCluster( final String clusterName, final String lxcHostname )
    {
        AbstractOperationHandler operationHandler = new StartClusterOperationHandler( this, clusterName );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID stopNode( final String clusterName, final String lxcHostname, final boolean master )
    {

        AbstractOperationHandler operationHandler =
                new StopNodeOperationHandler( this, clusterName, lxcHostname, master );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public UUID stopCluster( final String clusterName, final String lxcHostname )
    {
        AbstractOperationHandler operationHandler = new StopClusterOperationHandler( this, clusterName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public UUID checkNode( final String clusterName, final String lxcHostname, final boolean master )
    {
        AbstractOperationHandler operationHandler =
                new CheckNodeOperationHandler( this, clusterName, lxcHostname, master );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public EnvironmentBlueprint getDefaultEnvironmentBlueprint( SparkClusterConfig config )
    {

        EnvironmentBlueprint blueprint = new EnvironmentBlueprint();

        blueprint.setName( String.format( "%s-%s", config.getProductKey(), UUIDUtil.generateTimeBasedUUID() ) );
        blueprint.setExchangeSshKeys( true );
        blueprint.setLinkHosts( true );
        blueprint.setDomainName( Common.DEFAULT_DOMAIN_NAME );

        NodeGroup ng = new NodeGroup();
        ng.setName( "Default" );
        ng.setNumberOfNodes( 1 + config.getSlaveNodesCount() ); // master +slaves
        ng.setTemplateName( SparkClusterConfig.TEMPLATE_NAME );
        ng.setPlacementStrategy( PlacementStrategy.MORE_RAM );
        blueprint.setNodeGroups( Sets.newHashSet( ng ) );


        return blueprint;
    }


    @Override
    public ClusterSetupStrategy getClusterSetupStrategy( final TrackerOperation po,
                                                         final SparkClusterConfig clusterConfig,
                                                         final Environment environment )
    {

        Preconditions.checkNotNull( po, "Product operation is null" );
        Preconditions.checkNotNull( clusterConfig, "Spark cluster config is null" );

        if ( clusterConfig.getSetupType() == SetupType.OVER_HADOOP )
        {
            return new SetupStrategyOverHadoop( po, this, clusterConfig, environment );
        }
        else
        {
            return new SetupStrategyWithHadoop( po, this, clusterConfig, environment );
        }
    }
}
