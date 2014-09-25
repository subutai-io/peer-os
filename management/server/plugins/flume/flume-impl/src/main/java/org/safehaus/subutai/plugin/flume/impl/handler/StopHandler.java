package org.safehaus.subutai.plugin.flume.impl.handler;


import java.util.Arrays;
import java.util.HashSet;

import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.RequestBuilder;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.flume.api.FlumeConfig;
import org.safehaus.subutai.plugin.flume.impl.CommandType;
import org.safehaus.subutai.plugin.flume.impl.Commands;
import org.safehaus.subutai.plugin.flume.impl.FlumeImpl;


public class StopHandler extends AbstractOperationHandler<FlumeImpl>
{

    private final String hostname;


    public StopHandler( FlumeImpl manager, String clusterName, String hostname )
    {
        super( manager, clusterName );
        this.hostname = hostname;
        this.productOperation =
                manager.getTracker().createProductOperation( FlumeConfig.PRODUCT_KEY, "Stop node " + hostname );
    }


    @Override
    public void run()
    {
        ProductOperation po = productOperation;
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

        po.addLog( "Stopping node..." );
        Command cmd = manager.getCommandRunner().createCommand( new RequestBuilder( Commands.make( CommandType.STOP ) ),
                new HashSet<>( Arrays.asList( node ) ) );
        manager.getCommandRunner().runCommand( cmd );

        if ( cmd.hasSucceeded() )
        {
            po.addLogDone( "Flume stopped on " + hostname );
        }
        else
        {
            po.addLog( cmd.getAllErrors() );
            po.addLogFailed( "Failed to stop node " + hostname );
        }
    }
}
