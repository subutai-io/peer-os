package org.safehaus.subutai.plugin.hive.impl.handler;


import java.util.Iterator;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.common.api.NodeOperationType;
import org.safehaus.subutai.plugin.hive.api.HiveConfig;
import org.safehaus.subutai.plugin.hive.impl.Commands;
import org.safehaus.subutai.plugin.hive.impl.HiveImpl;

import com.google.common.base.Preconditions;


/**
 * This class handles operations that are related to just one node.
 *
 * TODO: add nodes and delete node operation should be implemented.
 */
public class NodeOperationHandler extends AbstractOperationHandler<HiveImpl, HiveConfig>
{

    private String clusterName;
    private String hostname;
    private NodeOperationType operationType;


    public NodeOperationHandler( final HiveImpl manager, final String clusterName, final String hostname,
                                 NodeOperationType operationType )
    {
        super( manager, manager.getCluster( clusterName ) );
        this.hostname = hostname;
        this.clusterName = clusterName;
        this.operationType = operationType;
        this.trackerOperation = manager.getTracker().createTrackerOperation( HiveConfig.PRODUCT_KEY,
                String.format( "Creating %s tracker object...", clusterName ) );
    }


    @Override
    public void run()
    {
        HiveConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            trackerOperation.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        Environment environment = manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() );
        Iterator iterator = environment.getContainerHosts().iterator();
        ContainerHost host = null;
        while ( iterator.hasNext() )
        {
            host = ( ContainerHost ) iterator.next();
            if ( host.getHostname().equals( hostname ) )
            {
                break;
            }
        }

        if ( host == null )
        {
            trackerOperation.addLogFailed( String.format( "No Container with ID %s", hostname ) );
            return;
        }

        try
        {
            CommandResult result = null;
            switch ( operationType )
            {
                case START:
                    result = host.execute( new RequestBuilder( Commands.startCommand ) );
                    break;
                case STOP:
                    result = host.execute( new RequestBuilder( Commands.stopCommand ) );
                    break;
                case STATUS:
                    result = host.execute( new RequestBuilder( Commands.statusCommand ) );
                    break;
                case RESTART:
                    result = host.execute( new RequestBuilder( Commands.restartCommand ) );
                    break;
                case UNINSTALL:
                    result = uninstallProductOnNode( host );
                    break;
                case INSTALL:
                    result = installProductOnNode( host );
                    break;
            }
            logResults( trackerOperation, result );
        }
        catch ( CommandException e )
        {
            trackerOperation.addLogFailed( String.format( "Command failed, %s", e.getMessage() ) );
        }
    }


    public static void logResults( TrackerOperation po, CommandResult result )
    {
        Preconditions.checkNotNull( result );
        String status = "UNKNOWN";
        if ( result.getExitCode() == 0 )
        {
            status = result.getStdOut();
        }
        else if ( result.getExitCode() == 768 )
        {
            status = "Hive Thrift Server is not running";
        }
        po.addLogDone( status );
    }


    private CommandResult installProductOnNode( ContainerHost host )
    {
        CommandResult result = null;
        try
        {
            result = host.execute( new RequestBuilder(
                    Commands.installCommand + Common.PACKAGE_PREFIX + HiveConfig.PRODUCT_KEY.toLowerCase() ) );
            if ( result.hasSucceeded() )
            {
                config.getClients().add( host.getId() );
                manager.getPluginDAO().saveInfo( HiveConfig.PRODUCT_KEY, config.getClusterName(), config );
                trackerOperation.addLog(
                        HiveConfig.PRODUCT_KEY + " is installed on node " + host.getHostname() + " successfully." );
            }
            else
            {
                trackerOperation.addLogFailed( "Could not install " + HiveConfig.PRODUCT_KEY + " to node " + hostname );
            }
        }
        catch ( CommandException e )
        {
            e.printStackTrace();
        }
        return result;
    }


    private CommandResult uninstallProductOnNode( ContainerHost host )
    {
        CommandResult result = null;
        try
        {
            result = host.execute( new RequestBuilder(
                    Commands.uninstallCommand + Common.PACKAGE_PREFIX + HiveConfig.PRODUCT_KEY.toLowerCase() ) );
            if ( result.hasSucceeded() )
            {
                config.getClients().remove( host.getId() );
                manager.getPluginDAO().saveInfo( HiveConfig.PRODUCT_KEY, config.getClusterName(), config );
                trackerOperation.addLog(
                        HiveConfig.PRODUCT_KEY + " is uninstalled from node " + host.getHostname() + " successfully." );
            }
            else
            {
                trackerOperation
                        .addLogFailed( "Could not uninstall " + HiveConfig.PRODUCT_KEY + " from node " + hostname );
            }
        }
        catch ( CommandException e )
        {
            e.printStackTrace();
        }
        return result;
    }
}
