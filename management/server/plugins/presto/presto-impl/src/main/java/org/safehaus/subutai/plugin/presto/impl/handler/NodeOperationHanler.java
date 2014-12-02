package org.safehaus.subutai.plugin.presto.impl.handler;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.common.api.NodeOperationType;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;
import org.safehaus.subutai.plugin.presto.impl.PrestoImpl;
import org.safehaus.subutai.plugin.presto.impl.SetupHelper;

import com.google.common.base.Preconditions;


/**
 * Created by ebru on 13.11.2014.
 */
public class NodeOperationHanler extends AbstractOperationHandler<PrestoImpl, PrestoClusterConfig>
{
    private String clusterName;
    private String hostName;
    private NodeOperationType operationType;


    public NodeOperationHanler( final PrestoImpl manager, final String clusterName, final String hostName,
                                NodeOperationType operationType )
    {
        super( manager, manager.getCluster( clusterName ) );
        this.hostName = hostName;
        this.clusterName = clusterName;
        this.operationType = operationType;
        this.trackerOperation = manager.getTracker().createTrackerOperation( PrestoClusterConfig.PRODUCT_KEY,
                String.format( "Checking %s cluster...", clusterName ) );
    }


    @Override
    public void run()
    {
        PrestoClusterConfig config = manager.getCluster( clusterName );
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
            if ( host.getHostname().equals( hostName ) )
            {
                break;
            }
        }

        if ( host == null )
        {
            trackerOperation.addLogFailed( String.format( "No Container with ID %s", hostName ) );
            return;
        }
        ContainerHost coordinator = environment.getContainerHostById( config.getCoordinatorNode() );
        if( !coordinator.isConnected())
        {
            trackerOperation.addLogFailed( String.format( "Coordinator node %s is not connected",
                    coordinator.getHostname() ) );
            return;
        }

        try
        {
            CommandResult result = null;
            switch ( operationType )
            {
                case START:
                    result = host.execute( manager.getCommands().getStartCommand() );
                    logStatusResults( trackerOperation, result );
                    break;
                case STOP:
                    result = host.execute( manager.getCommands().getStopCommand() );
                    logStatusResults( trackerOperation, result );
                    break;
                case STATUS:
                    result = host.execute( manager.getCommands().getStatusCommand() );
                    logStatusResults( trackerOperation, result );
                    break;
                case INSTALL:
                    installProductOnNode( host );
                    break;
                case UNINSTALL:
                    uninstallProductOnNode( host );
                    break;
            }
            //logStatusResults( trackerOperation, result );
        }
        catch ( CommandException e )
        {
            trackerOperation.addLogFailed( String.format( "Command failed, %s", e.getMessage() ) );
        }


    }

    private CommandResult installProductOnNode( ContainerHost host )
    {
        CommandResult result = null;
        try
        {
            if( !host.isConnected())
            {
                throw new ClusterSetupException( "New node is not connected" );
            }
            if( config.getWorkers().contains( host.getId() ))
            {
                throw new ClusterSetupException( "Node already belongs to cluster" + clusterName );
            }
            result = host.execute( manager.getCommands().getCheckInstalledCommand() );
            String hadoopPackage = Common.PACKAGE_PREFIX + HadoopClusterConfig.PRODUCT_NAME;
            boolean skipInstall = false;
            if( result.getStdOut().contains( manager.getCommands().PACKAGE_NAME ))
            {
                skipInstall = true;
                trackerOperation.addLog( "Node already has Presto installed" );
            }
            else if ( !result.getStdOut().contains( hadoopPackage ) )
            {
                throw new ClusterSetupException( "Node has no Hadoop installation" );
            }
            if( !skipInstall)
            {
                trackerOperation.addLog( "Installing Presto..." );
                result = host.execute( manager.getCommands().getInstallCommand() );
                if( result.hasSucceeded() )
                {
                    config.getWorkers().add( host.getId() );
                    manager.getPluginDAO().saveInfo( PrestoClusterConfig.PRODUCT_KEY, config.getClusterName(), config );
                    trackerOperation.addLogDone(
                            PrestoClusterConfig.PRODUCT_KEY + " is installed on node " + host.getHostname() + " successfully." );
                }
                else
                {
                    trackerOperation.addLogFailed(
                            "Could not install " + PrestoClusterConfig.PRODUCT_KEY + " to node " + host.getHostname() );
                }
            }
            Set<ContainerHost> set = new HashSet<>( Arrays.asList( host ) );
            SetupHelper sh = new SetupHelper( trackerOperation, manager, config );
            sh.configureAsWorker( set );
            sh.startNodes( set );
        }
        catch ( CommandException | ClusterSetupException e  )
        {
            trackerOperation.addLogFailed( String.format( "Command failed, %s", e.getMessage() ) );
        }
        return result;
    }

    private CommandResult uninstallProductOnNode( ContainerHost host )
    {
        CommandResult result = null;
        try
        {
            result = host.execute( manager.getCommands().getUninstallCommand() );
            if ( result.hasSucceeded() )
            {
                config.getWorkers().remove( host.getId() );
                manager.getPluginDAO().saveInfo( PrestoClusterConfig.PRODUCT_KEY, config.getClusterName(), config );
                trackerOperation.addLogDone( PrestoClusterConfig.PRODUCT_KEY + " is uninstalled from node " + host.getHostname()
                        + " successfully." );
            }
            else
            {
                trackerOperation.addLogFailed(
                        "Could not uninstall " + PrestoClusterConfig.PRODUCT_KEY + " from node " + host.getHostname() );
            }
        }
        catch ( CommandException e )
        {
            e.printStackTrace();
        }
        return result;
    }
    public static void logStatusResults( TrackerOperation po, CommandResult result )
    {
        Preconditions.checkNotNull( result );
        StringBuilder log = new StringBuilder();
        String status = "UNKNOWN";

        if( result.getExitCode() == 0 )
        {
            status = result.getStdOut();
        }
        if ( result.getExitCode() == 768 )
        {
            status = "Not running";
        }

        log.append( String.format( "%s", status ) );
        po.addLogDone( log.toString() );
    }

}
