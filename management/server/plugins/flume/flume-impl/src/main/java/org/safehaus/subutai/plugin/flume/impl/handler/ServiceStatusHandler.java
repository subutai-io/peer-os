package org.safehaus.subutai.plugin.flume.impl.handler;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.plugin.flume.api.FlumeConfig;
import org.safehaus.subutai.plugin.flume.impl.CommandType;
import org.safehaus.subutai.plugin.flume.impl.Commands;
import org.safehaus.subutai.plugin.flume.impl.FlumeImpl;


public class ServiceStatusHandler extends AbstractOperationHandler<FlumeImpl>
{

    private final String hostname;


    public ServiceStatusHandler( FlumeImpl manager, String clusterName, String hostname )
    {
        super( manager, clusterName );
        this.hostname = hostname;
        this.trackerOperation = manager.getTracker().createTrackerOperation( FlumeConfig.PRODUCT_KEY,
                "Check service on node " + hostname );
    }


    @Override
    public void run()
    {
        TrackerOperation po = trackerOperation;
        if ( manager.getCluster( clusterName ) == null )
        {
            po.addLogFailed( "Cluster does not exist: " + clusterName );
            return;
        }

        Agent node = manager.getAgentManager().getAgentByHostname( hostname );
        if ( node == null )
        {
            po.addLogFailed( "Node is not connected: " + hostname );
            return;
        }

        Command cmd = manager.getCommandRunner()
                             .createCommand( new RequestBuilder( Commands.make( CommandType.SERVICE_STATUS ) ),
                                     new HashSet<>( Arrays.asList( node ) ) );
        manager.getCommandRunner().runCommand( cmd );

        if ( cmd.hasSucceeded() )
        {
            AgentResult result = cmd.getResults().get( node.getUuid() );
            if ( result.getStdOut().contains( FlumeConfig.PRODUCT_KEY.toLowerCase() ) )
            {
                po.addLogDone( "Flume is running" );
            }
            else
            {
                po.addLogDone( "Flume is not running" );
            }
        }
        else
        {
            logStatusResults( trackerOperation, cmd );
        }
    }


    private void logStatusResults( TrackerOperation po, Command checkStatusCommand )
    {

        StringBuilder log = new StringBuilder();

        for ( Map.Entry<UUID, AgentResult> e : checkStatusCommand.getResults().entrySet() )
        {

            String status = "UNKNOWN";
            if ( e.getValue().getExitCode() == 0 )
            {
                status = "Flume is running";
            }
            else if ( e.getValue().getExitCode() == 256 )
            {
                status = "Flume is not running";
            }

            log.append( String.format( "%s", status ) );
        }
        po.addLogDone( log.toString() );
    }
}