package org.safehaus.subutai.plugin.presto.impl;


import java.util.List;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.presto.api.Presto;
import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;
import org.safehaus.subutai.plugin.presto.api.SetupType;
import org.safehaus.subutai.plugin.presto.impl.handler.AddWorkerNodeOperationHandler;
import org.safehaus.subutai.plugin.presto.impl.handler.ChangeCoordinatorNodeOperationHandler;
import org.safehaus.subutai.plugin.presto.impl.handler.CheckNodeOperationHandler;
import org.safehaus.subutai.plugin.presto.impl.handler.DestroyWorkerNodeOperationHandler;
import org.safehaus.subutai.plugin.presto.impl.handler.InstallOperationHandler;
import org.safehaus.subutai.plugin.presto.impl.handler.StartNodeOperationHandler;
import org.safehaus.subutai.plugin.presto.impl.handler.StopNodeOperationHandler;
import org.safehaus.subutai.plugin.presto.impl.handler.UninstallOperationHandler;

import com.google.common.base.Preconditions;


public class PrestoImpl extends PrestoBase implements Presto
{

    public PrestoImpl()
    {
    }


    @Override
    public UUID installCluster( final PrestoClusterConfig config )
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
    public List<PrestoClusterConfig> getClusters()
    {
        return pluginDAO.getInfo( PrestoClusterConfig.PRODUCT_KEY, PrestoClusterConfig.class );
    }


    @Override
    public PrestoClusterConfig getCluster( String clusterName )
    {
        return pluginDAO.getInfo( PrestoClusterConfig.PRODUCT_KEY, clusterName, PrestoClusterConfig.class );
    }


    @Override
    public UUID addNode( final String clusterName, final String agentHostName )
    {
        return null;
    }


    @Override
    public UUID installCluster( PrestoClusterConfig config, HadoopClusterConfig hadoopConfig )
    {
        InstallOperationHandler h = new InstallOperationHandler( this, config );
        h.setHadoopConfig( hadoopConfig );
        executor.execute( h );
        return h.getTrackerId();
    }


    @Override
    public UUID addWorkerNode( final String clusterName, final String lxcHostname )
    {

        AbstractOperationHandler operationHandler = new AddWorkerNodeOperationHandler( this, clusterName, lxcHostname );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public UUID destroyWorkerNode( final String clusterName, final String lxcHostname )
    {

        AbstractOperationHandler operationHandler =
                new DestroyWorkerNodeOperationHandler( this, clusterName, lxcHostname );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public UUID changeCoordinatorNode( final String clusterName, final String newCoordinatorHostname )
    {

        AbstractOperationHandler operationHandler =
                new ChangeCoordinatorNodeOperationHandler( this, clusterName, newCoordinatorHostname );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public UUID startNode( final String clusterName, final String lxcHostname )
    {

        AbstractOperationHandler operationHandler = new StartNodeOperationHandler( this, clusterName, lxcHostname );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public UUID stopNode( final String clusterName, final String lxcHostname )
    {

        AbstractOperationHandler operationHandler = new StopNodeOperationHandler( this, clusterName, lxcHostname );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public UUID checkNode( final String clusterName, final String lxcHostname )
    {

        AbstractOperationHandler operationHandler = new CheckNodeOperationHandler( this, clusterName, lxcHostname );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public ClusterSetupStrategy getClusterSetupStrategy( final ProductOperation po, final PrestoClusterConfig config,
                                                         final Environment environment )
    {

        if ( config.getSetupType() == SetupType.OVER_HADOOP )
        {
            return new SetupStrategyOverHadoop( po, this, config );
        }

        if ( config.getSetupType() == SetupType.WITH_HADOOP )
        {
            SetupStrategyWithHadoop s = new SetupStrategyWithHadoop( po, this, config );
            s.setEnvironment( environment );
            return s;
        }

        return null;
    }
}
