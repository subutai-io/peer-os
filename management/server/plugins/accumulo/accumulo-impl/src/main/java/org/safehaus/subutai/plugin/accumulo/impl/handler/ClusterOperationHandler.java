package org.safehaus.subutai.plugin.accumulo.impl.handler;


import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.accumulo.impl.AccumuloImpl;
import org.safehaus.subutai.plugin.accumulo.impl.AccumuloOverZkNHadoopSetupStrategy;
import org.safehaus.subutai.plugin.accumulo.impl.Commands;
import org.safehaus.subutai.plugin.common.api.ClusterOperationHandlerInterface;
import org.safehaus.subutai.plugin.common.api.ClusterOperationType;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;


/**
 * This class handles operations that are related to whole cluster.
 */
public class ClusterOperationHandler extends AbstractOperationHandler<AccumuloImpl, AccumuloClusterConfig>
        implements ClusterOperationHandlerInterface
{
    private static final Logger LOG = LoggerFactory.getLogger( ClusterOperationHandler.class.getName() );
    private ClusterOperationType operationType;
    private AccumuloClusterConfig config;
    private HadoopClusterConfig hadoopConfig;

    private ExecutorService executor = Executors.newCachedThreadPool();


    public ClusterOperationHandler( final AccumuloImpl manager, final AccumuloClusterConfig config,
                                    final HadoopClusterConfig hadoopConfig,
                                    final ZookeeperClusterConfig zookeeperClusterConfig,
                                    final ClusterOperationType operationType )
    {
        super( manager, config );
        this.operationType = operationType;
        this.config = config;
        this.hadoopConfig = hadoopConfig;
        trackerOperation = manager.getTracker().createTrackerOperation( AccumuloClusterConfig.PRODUCT_KEY,
                String.format( "Creating %s tracker object...", clusterName ) );
    }


    public void run()
    {
        Preconditions.checkNotNull( config, "Configuration is null !!!" );
        switch ( operationType )
        {
            case INSTALL:
                setupCluster();
                break;
            case UNINSTALL:
                destroyCluster();
                break;
            case START_ALL:
                runOperationOnContainers( operationType );
            case STOP_ALL:
                runOperationOnContainers( operationType );
            case STATUS_ALL:
                runOperationOnContainers( operationType );
                break;
        }
    }


    @Override
    public void runOperationOnContainers( ClusterOperationType clusterOperationType )
    {
        Environment environment = manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() );
        CommandResult result = null;
        ContainerHost containerHost = environment.getContainerHostById( config.getMasterNode() );
        switch ( clusterOperationType )
        {
            case START_ALL:
                result = executeCommand( containerHost, Commands.startCommand );
                break;
            case STOP_ALL:
                result = executeCommand( containerHost, Commands.stopCommand );
                break;
            case STATUS_ALL:
                for ( ContainerHost host : environment.getContainerHosts() )
                {
                    result = executeCommand( host, Commands.statusCommand );
                }
                break;
        }
        assert result != null;
        if ( result.hasSucceeded() )
        {
            trackerOperation.addLogDone( "" );
        }
    }


    @Override
    public void setupCluster()
    {
        Environment environment =
                manager.getEnvironmentManager().getEnvironmentByUUID( hadoopConfig.getEnvironmentId() );
        AccumuloOverZkNHadoopSetupStrategy setupStrategyOverHadoop =
                new AccumuloOverZkNHadoopSetupStrategy( environment, config, hadoopConfig, trackerOperation, manager );
        try
        {
            setupStrategyOverHadoop.setup();
        }
        catch ( ClusterSetupException e )
        {
            e.printStackTrace();
        }
    }


    @Override
    public void destroyCluster()
    {
        AccumuloClusterConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            trackerOperation.addLogFailed(
                    String.format( "Cluster with name %s does not exist. Operation aborted", clusterName ) );
            return;
        }

        for ( UUID uuid : config.getAllNodes() )
        {
            ContainerHost host = manager.getEnvironmentManager().getEnvironmentByUUID( hadoopConfig.getEnvironmentId() )
                                        .getContainerHostById( uuid );
            CommandResult result;
            try
            {
                result = host.execute( new RequestBuilder(
                        Commands.uninstallCommand + Common.PACKAGE_PREFIX + AccumuloClusterConfig.PRODUCT_KEY
                                .toLowerCase() ) );
                if ( result.hasSucceeded() )
                {
                    trackerOperation.addLog(
                            AccumuloClusterConfig.PRODUCT_KEY + " is uninstalled from node " + host.getHostname()
                                    + " successfully." );
                }
                else
                {
                    trackerOperation.addLogFailed(
                            "Could not uninstall " + AccumuloClusterConfig.PRODUCT_KEY + " from node " + host
                                    .getHostname() );
                }
            }
            catch ( CommandException e )
            {
                e.printStackTrace();
            }
        }
        ContainerHost namenode = manager.getEnvironmentManager().getEnvironmentByUUID( hadoopConfig.getEnvironmentId() )
                                        .getContainerHostById( hadoopConfig.getNameNode() );
        CommandResult result = executeCommand( namenode, Commands.getRemoveAccumuloFromHFDSCommand() );
        if ( result.hasSucceeded() )
        {
            trackerOperation.addLog( AccumuloClusterConfig.PRODUCT_KEY + " cluster info removed from HDFS." );
        }
        else
        {
            trackerOperation.addLogFailed( "Failed to remove " + config.getClusterName() + " cluster info from DB." );
        }
        manager.getPluginDAO().deleteInfo( AccumuloClusterConfig.PRODUCT_KEY, config.getClusterName() );
        trackerOperation.addLogDone( AccumuloClusterConfig.PRODUCT_KEY + " is uninstalled from all nodes" );
    }


    private CommandResult executeCommand( ContainerHost containerHost, String command )
    {
        CommandResult result = null;
        try
        {
            result = containerHost.execute( new RequestBuilder( command ) );
        }
        catch ( CommandException e )
        {
            LOG.error( "Could not execute command correctly. ", command );
            e.printStackTrace();
        }
        return result;
    }
}
