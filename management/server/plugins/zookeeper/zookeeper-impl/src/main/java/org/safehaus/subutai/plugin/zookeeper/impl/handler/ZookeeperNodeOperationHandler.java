package org.safehaus.subutai.plugin.zookeeper.impl.handler;


import java.util.ArrayList;
import java.util.List;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.exception.ClusterConfigurationException;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentManagerException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.common.api.NodeOperationType;
import org.safehaus.subutai.plugin.zookeeper.api.SetupType;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.impl.ClusterConfiguration;
import org.safehaus.subutai.plugin.zookeeper.impl.Commands;
import org.safehaus.subutai.plugin.zookeeper.impl.ZookeeperImpl;


/**
 * This class handles operations that are related to just one node.
 *
 * TODO: add nodes and delete node operation should be implemented.
 */
public class ZookeeperNodeOperationHandler extends AbstractPluginOperationHandler<ZookeeperImpl, ZookeeperClusterConfig>
{

    private String clusterName;
    private String hostname;
    private NodeOperationType operationType;


    public ZookeeperNodeOperationHandler( final ZookeeperImpl manager, final String clusterName, final String hostname,
                                          NodeOperationType operationType )
    {

        super( manager, clusterName );
        this.hostname = hostname;
        this.clusterName = clusterName;
        this.operationType = operationType;
        this.trackerOperation = manager.getTracker().createTrackerOperation( ZookeeperClusterConfig.PRODUCT_NAME,
                String.format( "Running %s operation on %s...", operationType, hostname ) );
    }


    @Override
    public void run()
    {
        ZookeeperClusterConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            trackerOperation.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        Environment environment = manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() );
        ContainerHost containerHost = environment.getContainerHostByHostname( hostname );

        if ( containerHost == null )
        {
            trackerOperation.addLogFailed( String.format( "No Container with ID %s", hostname ) );
            return;
        }

        try
        {
            List<CommandResult> commandResultList = new ArrayList<>(  );
            switch ( operationType )
            {
                case START:
                    commandResultList.add( containerHost.execute( new RequestBuilder(
                            Commands.getStartCommand()) ) );
                    break;
                case STOP:
                    commandResultList.add( containerHost.execute( new RequestBuilder(
                            Commands.getStopCommand()) ) );
                    break;
                case STATUS:
                    commandResultList.add( containerHost.execute( new RequestBuilder(
                            Commands.getStatusCommand()) ) );
                    break;
                case DESTROY:
                    if ( config.getSetupType() == SetupType.OVER_HADOOP ) {
                        commandResultList.add( containerHost.execute( new RequestBuilder(
                                Commands.getUninstallCommand() ) ) );
                        boolean isRemoved = config.getNodes().remove( containerHost.getId() );
                        if ( isRemoved ) {
                            manager.getPluginDAO().deleteInfo( config.getProductKey(), config.getClusterName() );
                            manager.getPluginDAO()
                                   .saveInfo( config.getProductKey(), config.getClusterName(), config );
                        }
                    }
                    else {
                        destroyNode( containerHost );
                    }

                    break;
            }
            logResults( trackerOperation, commandResultList );
        }
        catch ( CommandException e )
        {
            trackerOperation.addLogFailed( String.format( "Command failed, %s", e.getMessage() ) );
            e.printStackTrace();

        }
    }


    public void destroyNode( ContainerHost host ){
        EnvironmentManager environmentManager  = manager.getEnvironmentManager();
        try
        {
            ZookeeperClusterConfig config = manager.getCluster( clusterName );
            environmentManager.removeContainer( config.getEnvironmentId(), host.getId() );
            config.getNodes().remove( host.getId() );
            manager.getPluginDAO().saveInfo( ZookeeperClusterConfig.PRODUCT_KEY, config.getClusterName(), config );
            ClusterConfiguration configurator = new ClusterConfiguration( manager, trackerOperation );
            try
            {
                configurator.configureCluster( config, environmentManager.getEnvironmentByUUID( config
                        .getEnvironmentId() ) );
            }
            catch ( ClusterConfigurationException e )
            {
                e.printStackTrace();
            }
            trackerOperation.addLog( String.format( "Cluster information is updated" ) );
            trackerOperation.addLogDone( String.format( "Container %s is removed from cluster", host.getHostname() ) );
        }
        catch ( EnvironmentManagerException e )
        {
            e.printStackTrace();
        }
    }
}
