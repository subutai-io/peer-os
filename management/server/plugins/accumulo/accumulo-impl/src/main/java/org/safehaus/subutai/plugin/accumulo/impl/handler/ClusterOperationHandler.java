package org.safehaus.subutai.plugin.accumulo.impl.handler;


import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.CommandResult;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.common.tracker.TrackerOperation;
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
                                    final HadoopClusterConfig hadoopConfig, final ZookeeperClusterConfig zookeeperClusterConfig,
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
                executor.execute( new Runnable()
                {
                    public void run()
                    {
                        setupCluster();
                    }
                } );
                break;
            case UNINSTALL:
                executor.execute( new Runnable()
                {
                    public void run()
                    {
                        destroyCluster();
                    }
                } );
                break;
            case START_ALL:
            case STOP_ALL:
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
        switch ( clusterOperationType )
        {
            case START_ALL:
                for ( ContainerHost containerHost : environment.getContainers() )
                {
                    result = executeCommand( containerHost, Commands.startCommand );
                }
                break;
            case STOP_ALL:
                for ( ContainerHost containerHost : environment.getContainers() )
                {
                    result = executeCommand( containerHost, Commands.stopCommand );
                }
                break;
            case STATUS_ALL:
                for ( ContainerHost containerHost : environment.getContainers() )
                {
                    result = executeCommand( containerHost, Commands.statusCommand );
                }
                break;
        }
        NodeOperationHandler.logResults( trackerOperation, result );
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
                                        .getContainerHostByUUID( uuid );
            CommandResult result = null;
//            try
//            {
//                if ( uuid.equals( config.getServer() ) )
//                {
//                    host.execute( new RequestBuilder(
//                            Commands.uninstallCommand + Common.PACKAGE_PREFIX + AccumuloClusterConfig.PRODUCT_KEY
//                                    .toLowerCase() ) );
//                    host.execute( new RequestBuilder( Commands.uninstallCommand + Common.PACKAGE_PREFIX + "derby" ) );
//                }
//                else
//                {
//                    host.execute( new RequestBuilder(
//                            Commands.uninstallCommand + Common.PACKAGE_PREFIX + AccumuloClusterConfig.PRODUCT_KEY
//                                    .toLowerCase() ) );
//                }
//                result = host.execute( new RequestBuilder( Commands.uninstallCommand ) );
//                if ( result.hasSucceeded() )
//                {
//                    config.getClients().remove( host.getId() );
//                    trackerOperation.addLog( AccumuloClusterConfig.PRODUCT_KEY + " is uninstalled from node " + host.getHostname()
//                            + " successfully." );
//                }
//                else
//                {
//                    trackerOperation.addLogFailed(
//                            "Could not uninstall " + AccumuloClusterConfig.PRODUCT_KEY + " from node " + host.getHostname() );
//                }
//            }
//            catch ( CommandException e )
//            {
//                e.printStackTrace();
//            }
        }
        manager.getPluginDAO().deleteInfo( AccumuloClusterConfig.PRODUCT_KEY, config.getClusterName() );
        trackerOperation.addLogDone( "Hive is uninstalled from all nodes" );
    }
}
