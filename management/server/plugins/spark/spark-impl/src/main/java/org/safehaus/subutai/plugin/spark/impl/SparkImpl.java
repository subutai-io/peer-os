package org.safehaus.subutai.plugin.spark.impl;


import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.api.ClusterOperationType;
import org.safehaus.subutai.plugin.common.api.NodeType;
import org.safehaus.subutai.plugin.common.api.OperationType;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.spark.api.Spark;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.plugin.spark.impl.handler.ClusterOperationHandler;
import org.safehaus.subutai.plugin.spark.impl.handler.NodeOperationHandler;

import com.google.common.base.Preconditions;


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

        AbstractOperationHandler operationHandler =
                new ClusterOperationHandler( this, config, ClusterOperationType.INSTALL, null );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public UUID uninstallCluster( final String clusterName )
    {

        SparkClusterConfig config = getCluster( clusterName );
        AbstractOperationHandler operationHandler =
                new ClusterOperationHandler( this, config, ClusterOperationType.UNINSTALL, null );

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
    public UUID addSlaveNode( final String clusterName, final String lxcHostname )
    {
        SparkClusterConfig config = getCluster( clusterName );
        AbstractOperationHandler operationHandler =
                new NodeOperationHandler( this, config, lxcHostname, OperationType.INCLUDE, null );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public UUID destroySlaveNode( final String clusterName, final String lxcHostname )
    {
        SparkClusterConfig config = getCluster( clusterName );
        AbstractOperationHandler operationHandler =
                new NodeOperationHandler( this, config, lxcHostname, OperationType.EXCLUDE, null );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public UUID changeMasterNode( final String clusterName, final String newMasterHostname, final boolean keepSlave )
    {
        SparkClusterConfig config = getCluster( clusterName );
        AbstractOperationHandler operationHandler =
                new NodeOperationHandler( this, config, newMasterHostname, OperationType.CHANGE_MASTER,
                        keepSlave ? NodeType.SLAVE_NODE : NodeType.MASTER_NODE );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public UUID startNode( final String clusterName, final String lxcHostname, final boolean master )
    {
        SparkClusterConfig config = getCluster( clusterName );
        AbstractOperationHandler operationHandler =
                new NodeOperationHandler( this, config, lxcHostname, OperationType.START,
                        master ? NodeType.MASTER_NODE : NodeType.SLAVE_NODE );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public UUID startCluster( final String clusterName, final String lxcHostname )
    {
        SparkClusterConfig config = getCluster( clusterName );
        AbstractOperationHandler operationHandler =
                new ClusterOperationHandler( this, config, ClusterOperationType.START_ALL, null );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public UUID stopNode( final String clusterName, final String lxcHostname, final boolean master )
    {
        SparkClusterConfig config = getCluster( clusterName );
        AbstractOperationHandler operationHandler =
                new NodeOperationHandler( this, config, lxcHostname, OperationType.STOP,
                        master ? NodeType.MASTER_NODE : NodeType.SLAVE_NODE );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public UUID stopCluster( final String clusterName, final String lxcHostname )
    {
        SparkClusterConfig config = getCluster( clusterName );
        AbstractOperationHandler operationHandler =
                new ClusterOperationHandler( this, config, ClusterOperationType.STOP_ALL, null );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public UUID checkNode( final String clusterName, final String lxcHostname, final boolean master )
    {
        SparkClusterConfig config = getCluster( clusterName );
        AbstractOperationHandler operationHandler =
                new NodeOperationHandler( this, config, lxcHostname, OperationType.STATUS,
                        master ? NodeType.MASTER_NODE : NodeType.SLAVE_NODE );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public ClusterSetupStrategy getClusterSetupStrategy( final TrackerOperation po,
                                                         final SparkClusterConfig clusterConfig,
                                                         final Environment environment )
    {

        Preconditions.checkNotNull( po, "Product operation is null" );
        Preconditions.checkNotNull( clusterConfig, "Spark cluster config is null" );

        return new SetupStrategyOverHadoop( po, this, clusterConfig, environment );
    }
}
