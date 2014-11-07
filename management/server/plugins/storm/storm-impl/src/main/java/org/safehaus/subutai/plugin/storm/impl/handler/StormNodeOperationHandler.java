package org.safehaus.subutai.plugin.storm.impl.handler;


import java.util.ArrayList;
import java.util.List;

import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.CommandResult;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.common.api.NodeOperationType;
import org.safehaus.subutai.plugin.common.api.NodeType;
import org.safehaus.subutai.plugin.storm.api.StormClusterConfiguration;
import org.safehaus.subutai.plugin.storm.impl.CommandType;
import org.safehaus.subutai.plugin.storm.impl.Commands;
import org.safehaus.subutai.plugin.storm.impl.StormImpl;
import org.safehaus.subutai.plugin.storm.impl.StormService;

import com.google.common.base.Preconditions;


/**
 * This class handles operations that are related to just one node.
 *
 * TODO: add nodes and delete node operation should be implemented.
 */
public class StormNodeOperationHandler extends AbstractOperationHandler<StormImpl>
{

    private String clusterName;
    private String hostname;
    private NodeOperationType operationType;


    public StormNodeOperationHandler( final StormImpl manager, final String clusterName, final String hostname,
                                      NodeOperationType operationType, NodeType nodeType )
    {
        super( manager, clusterName );
        this.hostname = hostname;
        this.clusterName = clusterName;
        this.operationType = operationType;
        this.trackerOperation = manager.getTracker()
                                       .createTrackerOperation( StormClusterConfiguration.PRODUCT_NAME,
                                               String.format( "Creating %s tracker object...", clusterName ) );
    }


    @Override
    public void run()
    {
        StormClusterConfiguration config = manager.getCluster( clusterName );
        if ( config == null )
        {
            trackerOperation.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        Environment environment = manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() );
        ContainerHost host = environment.getContainerHostByHostname( hostname );

        if ( host == null )
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
                    for ( ContainerHost containerHost : environment.getContainers() )
                    {
                        if ( config.getNimbus().equals( containerHost.getId() ) ) {
                            commandResultList.add( containerHost.execute( new
                                    RequestBuilder( Commands.make( CommandType.START, StormService.NIMBUS ) ) ) );
                            commandResultList.add( containerHost.execute(
                                    new RequestBuilder( Commands.make( CommandType.START, StormService.UI ) ) ) );
                        }
                        else if ( config.getSupervisors().contains( containerHost.getId() ) )
                            commandResultList.add( containerHost.execute(
                                    new RequestBuilder( Commands.make( CommandType.START, StormService.SUPERVISOR ) ) ) );
                    }
                    break;
                case STOP:
                    for ( ContainerHost containerHost : environment.getContainers() )
                    {
                        if ( config.getNimbus().equals( containerHost.getId() ) ) {
                            commandResultList.add( containerHost.execute(
                                    new RequestBuilder( Commands.make( CommandType.STOP, StormService.NIMBUS ) ) ) );
                            commandResultList.add( containerHost.execute(
                                    new RequestBuilder( Commands.make( CommandType.STOP, StormService.UI ) ) ) );
                        }
                        else if ( config.getSupervisors().contains( containerHost.getId() ) )
                            commandResultList.add( containerHost.execute(
                                    new RequestBuilder( Commands.make( CommandType.STOP, StormService.SUPERVISOR ) ) ) );
                    }
                    break;
                case STATUS:
                    for ( ContainerHost containerHost : environment.getContainers() )
                    {
                        if ( config.getNimbus().equals( containerHost.getId() ) ) {
                            commandResultList.add( containerHost.execute(
                                    new RequestBuilder( Commands.make( CommandType.STATUS, StormService.NIMBUS ) ) ) );
                            commandResultList.add( containerHost.execute(
                                    new RequestBuilder( Commands.make( CommandType.STATUS, StormService.UI ) ) ) );
                        }
                        else if ( config.getSupervisors().contains( containerHost.getId() ) )
                            commandResultList.add( containerHost.execute(
                                    new RequestBuilder( Commands.make( CommandType.STATUS, StormService.SUPERVISOR ) ) ) );
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
        String finishMessage = String.format( "%s operation on %s finished", operationType );
        switch ( po.getState() )
        {
            case SUCCEEDED:
                po.addLogDone( finishMessage );
                break;
            case FAILED:
                po.addLogFailed( finishMessage );
                break;
            default:
                po.addLogDone( String.format( "Still running %s operations on %s", operationType ) );
                break;
        }
    }
}
