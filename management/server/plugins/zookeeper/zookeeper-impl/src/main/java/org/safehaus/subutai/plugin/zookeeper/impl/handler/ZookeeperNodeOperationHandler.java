package org.safehaus.subutai.plugin.zookeeper.impl.handler;


import java.util.ArrayList;
import java.util.List;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.common.api.NodeOperationType;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.impl.Commands;
import org.safehaus.subutai.plugin.zookeeper.impl.ZookeeperImpl;

import com.google.common.base.Preconditions;


/**
 * This class handles operations that are related to just one node.
 *
 * TODO: add nodes and delete node operation should be implemented.
 */
public class ZookeeperNodeOperationHandler extends AbstractOperationHandler<ZookeeperImpl, ZookeeperClusterConfig>
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
                String.format( "Running %s operaion on %s...", operationType, hostname ) );
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
            List<CommandResult> commandResultList = new ArrayList<CommandResult>(  );
            switch ( operationType )
            {
                case START:
                        commandResultList.add( containerHost.execute( new RequestBuilder(
                                new Commands().getStartCommand()) ) );
                    break;
                case STOP:
                    commandResultList.add( containerHost.execute( new RequestBuilder(
                            new Commands().getStopCommand()) ) );
                    break;
                case STATUS:
                    commandResultList.add( containerHost.execute( new RequestBuilder(
                            new Commands().getStatusCommand()) ) );
                    break;
                case DESTROY:
                    commandResultList.add( containerHost.execute( new RequestBuilder(
                    Commands.getUninstallCommand() ) ) );
                    boolean isRemoved = config.getNodes().remove( containerHost.getId() );
                    if ( isRemoved ) {
                        manager.getPluginDAO().deleteInfo( config.getProductKey(), config.getClusterName() );
                        manager.getPluginDAO()
                               .saveInfo( config.getProductKey(), config.getClusterName(), config );
                    }
                    break;
            }
            logResults( trackerOperation, commandResultList );
        }
        catch ( CommandException e )
        {
            trackerOperation.addLogFailed( String.format( "Command failed, %s", e.getMessage() ) );
        }
    }


    public void logResults( TrackerOperation po, List<CommandResult> commandResultList )
    {
        Preconditions.checkNotNull( commandResultList );
        for ( CommandResult commandResult : commandResultList )
            po.addLog( commandResult.getStdOut() );
        po.addLogDone( "" );
    }
}
