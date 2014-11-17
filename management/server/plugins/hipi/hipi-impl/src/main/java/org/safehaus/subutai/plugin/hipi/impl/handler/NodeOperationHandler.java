package org.safehaus.subutai.plugin.hipi.impl.handler;


import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.exception.ClusterException;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.common.api.NodeOperationType;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hipi.api.HipiConfig;
import org.safehaus.subutai.plugin.hipi.api.SetupType;
import org.safehaus.subutai.plugin.hipi.impl.CommandFactory;
import org.safehaus.subutai.plugin.hipi.impl.HipiImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class NodeOperationHandler extends AbstractOperationHandler<HipiImpl, HipiConfig>
{
    private static Log LOG = LogFactory.getLog( NodeOperationHandler.class );
    private String hostName = null;
    private NodeOperationType operationType;


    public NodeOperationHandler( final HipiImpl manager, final HipiConfig config, String hostname,
                                 NodeOperationType operationType )
    {
        super( manager, config );
        this.hostName = hostname;
        this.operationType = operationType;

        String desc = String.format( "Executing %s operation on node %s", operationType.name(), hostname );
        this.trackerOperation = manager.getTracker().createTrackerOperation( HipiConfig.PRODUCT_KEY, desc );
    }


    @Override
    public void run()
    {
        try
        {
            if ( manager.getCluster( clusterName ) == null )
            {
                throw new ClusterException( String.format( "Cluster with name %s does not exist", clusterName ) );
            }

            Environment environment = manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() );
            if ( environment == null )
            {
                throw new ClusterException( "Environment not found: " + config.getEnvironmentId() );
            }

            ContainerHost node = environment.getContainerHostByHostname( hostName );
            if ( node == null )
            {
                throw new ClusterException( "Node not found in environment: " + hostName );
            }
            if ( !node.isConnected() )
            {
                throw new ClusterException( "Node is not connected: " + hostName );
            }

            switch ( operationType )
            {
                case EXCLUDE:
                    removeNode( node );
                    break;
                case INCLUDE:
                    addNode( node );
                    break;
                default:
                    LOG.warn( "Unsupported operation type : " + operationType );
            }

            trackerOperation.addLogDone( String.format( "Operation %s succeeded", operationType.name() ) );
        }
        catch ( ClusterException ex )
        {
            LOG.error( "Exception " + ex.getMessage() + " thrown when handling operation type " + operationType.name(),
                    ex );
            trackerOperation
                    .addLogFailed( String.format( "Operation %s failed: %s", operationType.name(), ex.getMessage() ) );
        }
    }


    private void addNode( ContainerHost node )
    {
        HipiConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            trackerOperation.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }
        try
        {
            if ( config.getSetupType() == SetupType.OVER_HADOOP )
            {
                setupHost( config, node );
            }

            config.getNodes().add( node.getId() );

            trackerOperation.addLog( "Saving cluster info..." );
            manager.getPluginDao().saveInfo( HipiConfig.PRODUCT_KEY, clusterName, config );
            trackerOperation.addLogDone( "Saved cluster info" );
        }
        catch ( ClusterSetupException ex )
        {
            trackerOperation.addLogFailed( "Add worker node failed with message " + ex.getMessage() );
        }
    }


    public void setupHost( HipiConfig config, ContainerHost node ) throws ClusterSetupException
    {
        //check if node is in the cluster
        if ( config.getNodes().contains( node.getId() ) )
        {
            throw new ClusterSetupException( "Node already belongs to cluster" + clusterName );
        }

        trackerOperation.addLog( "Checking prerequisites..." );

        try
        {
            String checkCommand = CommandFactory.build( NodeOperationType.CHECK_INSTALLATION );
            CommandResult checkCommandResult = node.execute( new RequestBuilder( checkCommand ) );
            String hadoopPack = Common.PACKAGE_PREFIX + HadoopClusterConfig.PRODUCT_NAME;
            if ( !checkCommandResult.hasCompleted() )
            {
                throw new ClusterSetupException( "Failed to check installed packages" );
            }

            boolean skipInstall = false;
            if ( checkCommandResult.hasSucceeded() && checkCommandResult.getStdOut()
                                                                        .contains( CommandFactory.PACKAGE_NAME ) )
            {
                skipInstall = true;
                trackerOperation.addLog( "Node already has " + HipiConfig.PRODUCT_KEY + " installed" );
            }
            if ( checkCommandResult.hasSucceeded() && !checkCommandResult.getStdOut().contains( hadoopPack ) )
            {
                throw new ClusterSetupException( "Node has no Hadoop installation" );
            }
            if ( !skipInstall )
            {
                trackerOperation.addLog( "Installing " + HipiConfig.PRODUCT_KEY );
                String installCommand = CommandFactory.build( NodeOperationType.INSTALL );
                CommandResult installCommandResult = node.execute( new RequestBuilder( installCommand ) );

                if ( installCommandResult.hasSucceeded() )
                {
                    trackerOperation.addLog( "Installation succeeded" );
                }
                else
                {
                    throw new ClusterSetupException( "Installation failed: " + installCommandResult.getStdErr() );
                }
            }
        }
        catch ( CommandException ex )
        {
            throw new ClusterSetupException( ex );
        }
    }


    private void removeNode( ContainerHost node )
    {
        HipiConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            trackerOperation.addLogFailed(
                    String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
            return;
        }

        if ( config.getNodes().size() == 1 )
        {
            trackerOperation.addLogFailed(
                    "This is the last slave node in the cluster. Please, destroy cluster instead\nOperation aborted" );
            return;
        }

        //check if node is in the cluster
        if ( !config.getNodes().contains( node.getId() ) )
        {
            trackerOperation.addLogFailed(
                    String.format( "Node %s does not belong to this cluster\nOperation aborted", node.getHostname() ) );
            return;
        }

        boolean ok = false;
        try
        {
            if ( config.getSetupType() == SetupType.OVER_HADOOP )
            {
                trackerOperation.addLog( "Uninstalling " + HipiConfig.PRODUCT_KEY );

                String uninstallCommand = CommandFactory.build( NodeOperationType.UNINSTALL );
                CommandResult uninstallCommandResult = node.execute( new RequestBuilder( uninstallCommand ) );

                if ( uninstallCommandResult.hasSucceeded() )
                {
                    trackerOperation.addLog( HipiConfig.PRODUCT_KEY + " removed from " + node.getHostname() );
                    ok = true;
                }
                else
                {
                    trackerOperation.addLog( "Uninstallation failed: " + uninstallCommandResult.getStdErr() );
                    ok = false;
                }
            }
        }
        catch (CommandException ex)
        {
            trackerOperation.addLogFailed( "Failed to remove with message " + ex.getMessage() );
        }

        if ( ok )
        {
            config.getNodes().remove( node.getId() );
            trackerOperation.addLog( "Updating db..." );

            manager.getPluginDao().saveInfo( HipiConfig.PRODUCT_KEY, config.getClusterName(), config );
            trackerOperation.addLogDone( "Cluster info updated in DB\nDone" );
        }
        else
        {
            trackerOperation.addLogFailed( "Failed to destroy node" );
        }
    }
}
