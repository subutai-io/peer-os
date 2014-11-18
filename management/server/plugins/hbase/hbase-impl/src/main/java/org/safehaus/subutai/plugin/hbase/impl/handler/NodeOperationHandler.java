package org.safehaus.subutai.plugin.hbase.impl.handler;


import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.exception.ClusterException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.common.api.ClusterOperationType;
import org.safehaus.subutai.plugin.common.api.OperationType;
import org.safehaus.subutai.plugin.hbase.api.HBaseConfig;
import org.safehaus.subutai.plugin.hbase.impl.Commands;
import org.safehaus.subutai.plugin.hbase.impl.HBaseImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * Created by bahadyr on 11/17/14.
 */
public class NodeOperationHandler extends AbstractOperationHandler<HBaseImpl, HBaseConfig>
{
    private static final Logger LOG = LoggerFactory.getLogger( NodeOperationHandler.class.getName() );
    private String hostname;
    private OperationType operationType;
    private ContainerHost node;
    private Environment environment;


    public NodeOperationHandler( final HBaseImpl manager, final HBaseConfig config )
    {
        super( manager, config );
    }


    public NodeOperationHandler( final HBaseImpl manager, final HBaseConfig config, final String hostname,
                                 OperationType operationType )
    {
        super( manager, config );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ), "Invalid hostname" );
        Preconditions.checkNotNull( operationType );
        this.hostname = hostname;
        this.operationType = operationType;
        this.trackerOperation = manager.getTracker().createTrackerOperation( HBaseConfig.PRODUCT_KEY,
                String.format( "Executing %s operation on node %s", operationType.name(), hostname ) );
    }


    public NodeOperationHandler( final HBaseImpl hBase, final HBaseConfig config, final ClusterOperationType startAll )
    {
        super( hBase, config );
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

            environment = manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() );

            if ( environment == null )
            {
                throw new ClusterException(
                        String.format( "Environment not found by id %s", config.getEnvironmentId() ) );
            }

            node = environment.getContainerHostByHostname( hostname );

            if ( node == null )
            {
                throw new ClusterException( String.format( "Node not found in environment by name %s", hostname ) );
            }


            if ( !node.isConnected() )
            {
                throw new ClusterException( String.format( "Node %s is not connected", hostname ) );
            }


            switch ( operationType )
            {

                case INCLUDE:
                    addNode();
                    break;
                case EXCLUDE:
                    removeNode();
                    break;
            }
        }
        catch ( ClusterException e )
        {
            LOG.error( "Error in NodeOperationHandler", e );
            trackerOperation
                    .addLogFailed( String.format( "Operation %s failed: %s", operationType.name(), e.getMessage() ) );
        }
    }


    private void removeNode() throws ClusterException
    {

        //check if node is in the cluster
        if ( !config.getAllNodes().contains( node.getId() ) )
        {
            throw new ClusterException( String.format( "Node %s does not belong to this cluster", hostname ) );
        }

        if ( config.getAllNodes().size() == 1 )
        {
            throw new ClusterException( "This is the last node in the cluster. Please, destroy cluster instead" );
        }

        trackerOperation.addLog( "Uninstalling HBase..." );

        executeCommand( node, manager.getCommands().getUninstallCommand(), true );

        config.getAllNodes().remove( node.getId() );

        trackerOperation.addLog( "Updating db..." );

        if ( !manager.getPluginDAO().saveInfo( HBaseConfig.PRODUCT_KEY, config.getClusterName(), config ) )
        {
            throw new ClusterException( "Could not update cluster info" );
        }
    }


    private void addNode() throws ClusterException
    {
        //check if node is in the cluster
        if ( config.getAllNodes().contains( node.getId() ) )
        {
            throw new ClusterException( String.format( "Node %s already belongs to this cluster", hostname ) );
        }

        CommandResult result = executeCommand( node, manager.getCommands().getCheckInstalledCommand() );

        if ( result.getStdOut().contains( Commands.PACKAGE_NAME ) )
        {
            throw new ClusterException( "Node already has HBase installed" );
        }

        config.getAllNodes().add( node.getId() );


        trackerOperation.addLog( "Installing HBase..." );

        executeCommand( node, manager.getCommands().getInstallCommand() );

        trackerOperation.addLog( "Setting Master IP..." );

        //        executeCommand( node, manager.getCommands().getSetMasterIPCommand( sparkMaster ) );

        trackerOperation.addLog( "Updating db..." );

        if ( !manager.getPluginDAO().saveInfo( HBaseConfig.PRODUCT_KEY, config.getClusterName(), config ) )
        {
            throw new ClusterException( "Could not update cluster info" );
        }
    }


    public CommandResult executeCommand( ContainerHost host, RequestBuilder command ) throws ClusterException
    {

        return executeCommand( host, command, false );
    }


    public CommandResult executeCommand( ContainerHost host, RequestBuilder command, boolean skipError )
            throws ClusterException
    {

        CommandResult result = null;
        try
        {
            result = host.execute( command );
        }
        catch ( CommandException e )
        {
            if ( skipError )
            {
                trackerOperation
                        .addLog( String.format( "Error on container %s: %s", host.getHostname(), e.getMessage() ) );
            }
            else
            {
                throw new ClusterException( e );
            }
        }
        if ( skipError )
        {
            if ( result != null && !result.hasSucceeded() )
            {
                trackerOperation.addLog( String.format( "Error on container %s: %s", host.getHostname(),
                        result.hasCompleted() ? result.getStdErr() : "Command timed out" ) );
            }
        }
        else
        {
            if ( !result.hasSucceeded() )
            {
                throw new ClusterException( String.format( "Error on container %s: %s", host.getHostname(),
                        result.hasCompleted() ? result.getStdErr() : "Command timed out" ) );
            }
        }
        return result;
    }
}
