package org.safehaus.subutai.plugin.zookeeper.impl.handler;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.exception.ClusterConfigurationException;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentDestroyException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.common.api.ClusterOperationHandlerInterface;
import org.safehaus.subutai.plugin.common.api.ClusterOperationType;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.api.SetupType;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.impl.ClusterConfiguration;
import org.safehaus.subutai.plugin.zookeeper.impl.Commands;
import org.safehaus.subutai.plugin.zookeeper.impl.ZookeeperImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * This class handles operations that are related to whole cluster.
 */
public class ZookeeperClusterOperationHandler extends AbstractPluginOperationHandler<ZookeeperImpl, ZookeeperClusterConfig>
        implements ClusterOperationHandlerInterface
{
    private static final Logger LOG = LoggerFactory.getLogger( ZookeeperClusterOperationHandler.class.getName() );
    private ClusterOperationType operationType;
    private ZookeeperClusterConfig zookeeperClusterConfig;
    private String hostName;
    private ExecutorService executor = Executors.newCachedThreadPool();


    public ZookeeperClusterOperationHandler( final ZookeeperImpl manager,
                                             final ZookeeperClusterConfig config,
                                             final ClusterOperationType operationType )
    {
        super( manager, config );
        this.operationType = operationType;
        this.zookeeperClusterConfig = config;
        trackerOperation = manager.getTracker().createTrackerOperation( config.getProductKey(),
                String.format( "Running %s operation on %s...", operationType , clusterName ) );
    }


    public ZookeeperClusterOperationHandler( final ZookeeperImpl manager,
                                             final ZookeeperClusterConfig zookeeperClusterConfig,
                                             final String hostName,
                                             final ClusterOperationType operationType )
    {
        super( manager, zookeeperClusterConfig );
        this.operationType = operationType;
        this.zookeeperClusterConfig = zookeeperClusterConfig;
        this.hostName = hostName;
        trackerOperation = manager.getTracker().createTrackerOperation( zookeeperClusterConfig.getProductKey(),
                String.format( "Running %s operation on %s...", operationType , clusterName ) );
    }


    public void run()
    {
        Preconditions.checkNotNull( zookeeperClusterConfig, "Configuration is null !!!" );
        runOperationOnContainers( operationType );
    }


    @Override
    public void runOperationOnContainers( ClusterOperationType clusterOperationType )
    {
        Environment environment;
        List<CommandResult> commandResultList = new ArrayList<>(  );
        switch ( clusterOperationType )
        {
            case INSTALL:
                setupCluster();
                break;
            case UNINSTALL:
                destroyCluster();
                break;
            case START_ALL:
                environment = manager.getEnvironmentManager().getEnvironmentByUUID( zookeeperClusterConfig.getEnvironmentId() );
                for ( ContainerHost containerHost : environment.getContainerHosts() )
                {
                    commandResultList.add( executeCommand( containerHost,
                            Commands.getStartCommand() ) );
                }
                break;
            case STOP_ALL:
                environment = manager.getEnvironmentManager().getEnvironmentByUUID( zookeeperClusterConfig.getEnvironmentId() );
                for ( ContainerHost containerHost : environment.getContainerHosts() )
                {
                    commandResultList.add( executeCommand( containerHost,
                            Commands.getStopCommand() ) );
                }
                break;
            case STATUS_ALL:
                environment = manager.getEnvironmentManager().getEnvironmentByUUID( zookeeperClusterConfig.getEnvironmentId() );
                for ( ContainerHost containerHost : environment.getContainerHosts() )
                {
                    commandResultList.add( executeCommand( containerHost,
                            Commands.getStatusCommand() ) );
                }
                break;
            case ADD:
                if ( zookeeperClusterConfig.getSetupType() == SetupType.OVER_HADOOP )
                    commandResultList.addAll( addNode( hostName ) );
                else if ( zookeeperClusterConfig.getSetupType() == SetupType.STANDALONE )
                    commandResultList.addAll( addNode() );
                else {
                    trackerOperation.addLogFailed( "Not supported SetupType" );
                    return;
                }
                break;
        }
        logResults( trackerOperation, commandResultList );
    }


    private List<CommandResult> addNode()
    {
        List<CommandResult> commandResultList = new ArrayList<>();
        trackerOperation.addLogFailed( "Adding node on standalone Zookeeper cluster is not supported yet!" );
        return commandResultList;
    }


    @Override
    public void setupCluster()
    {
        if ( Strings.isNullOrEmpty( zookeeperClusterConfig.getClusterName() ) )
        {
            trackerOperation.addLogFailed( "Malformed configuration" );
            return;
        }

        if ( manager.getCluster( clusterName ) != null )
        {
            trackerOperation.addLogFailed( String.format( "Cluster with name '%s' already exists", clusterName ) );
            return;
        }

        try
        {
            Environment env = null;
            if ( config.getSetupType() != SetupType.OVER_HADOOP ) {
                env = manager.getEnvironmentManager()
                             .buildEnvironment( manager.getDefaultEnvironmentBlueprint( zookeeperClusterConfig ) );
            }


            ClusterSetupStrategy clusterSetupStrategy =
                    manager.getClusterSetupStrategy( env, zookeeperClusterConfig, trackerOperation );
            clusterSetupStrategy.setup();

            trackerOperation.addLogDone( String.format( "Cluster %s set up successfully", clusterName ) );
        }
        catch ( EnvironmentBuildException | ClusterSetupException e )
        {
            trackerOperation.addLogFailed(
                    String.format( "Failed to setup %s cluster %s : %s", zookeeperClusterConfig.getProductKey(), clusterName,
                            e.getMessage() ) );
        }
    }


    @Override
    public void destroyCluster()
    {
        ZookeeperClusterConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            trackerOperation.addLogFailed(
                    String.format( "Cluster with name %s does not exist. Operation aborted", clusterName ) );
            return;
        }

        try
        {
            if ( config.getSetupType() == SetupType.OVER_HADOOP ) {
                List<CommandResult> commandResultList = new ArrayList<>(  );

                trackerOperation.addLog( "Uninstalling zookeeper on hadoop nodes" );
                Environment zookeeperEnvironment =
                        manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() );
                for ( ContainerHost containerHost : zookeeperEnvironment.getContainerHostsByIds( config.getNodes() ) ) {
                    commandResultList.add( containerHost.execute( new RequestBuilder (
                            Commands.getUninstallCommand() ) ) );
                }
                logResults( trackerOperation, commandResultList );

            }
            else {
                trackerOperation.addLog( "Destroying environment..." );
                manager.getEnvironmentManager().destroyEnvironment( config.getEnvironmentId() );
            }

            manager.getPluginDAO().deleteInfo( config.getProductKey(), config.getClusterName() );
            trackerOperation.addLogDone( "Cluster destroyed" );
        }
        catch ( EnvironmentDestroyException e )
        {
            trackerOperation.addLogFailed( String.format( "Error running command, %s", e.getMessage() ) );
            LOG.error( e.getMessage(), e );
        }
        catch ( CommandException e )
        {
            e.printStackTrace();
        }
    }


    public List<CommandResult> addNode ( String hostName ) {
        List<CommandResult> commandResultList = new ArrayList<>();
        Environment zookeeperEnvironment = manager.getEnvironmentManager().
                getEnvironmentByUUID( zookeeperClusterConfig.getEnvironmentId() );
        HadoopClusterConfig hadoopCluster = manager.getHadoopManager().
                getCluster( zookeeperClusterConfig.getHadoopClusterName() );
        Environment hadoopEnvironment = manager.getEnvironmentManager().
                getEnvironmentByUUID( hadoopCluster.getEnvironmentId() );
        try
        {
            ContainerHost newNode = hadoopEnvironment.getContainerHostByHostname( hostName );
            String command = Commands.getInstallCommand();
            if ( ! newNode.isConnected() ) {
                trackerOperation.addLogFailed( String.format( "Host %s is not connected. Aborting", hostName ) );
                return commandResultList;
            }
            CommandResult commandResult = executeCommand( newNode, command );
            commandResultList.add( commandResult );
            if ( ! commandResult.hasSucceeded() ) {
                trackerOperation.addLogFailed( String.format( "Command %s failed on %s", command, hostName ) );
                return commandResultList;
            }
            zookeeperClusterConfig.getNodes().add( newNode.getId() );
            new ClusterConfiguration( manager, trackerOperation ).configureCluster( zookeeperClusterConfig,
                    zookeeperEnvironment );
            trackerOperation.addLog( "Updating cluster information..." );
            manager.getPluginDAO()
                   .saveInfo( ZookeeperClusterConfig.PRODUCT_KEY, zookeeperClusterConfig.getClusterName(),
                           zookeeperClusterConfig );
        }
        catch ( ClusterConfigurationException e )
        {
            e.printStackTrace();
        }
        return commandResultList;
    }

}
