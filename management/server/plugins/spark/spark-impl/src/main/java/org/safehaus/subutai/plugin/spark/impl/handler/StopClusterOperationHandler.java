package org.safehaus.subutai.plugin.spark.impl.handler;


import java.util.concurrent.atomic.AtomicBoolean;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandCallback;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.plugin.spark.impl.SparkImpl;


public class StopClusterOperationHandler extends AbstractOperationHandler<SparkImpl>
{

    public StopClusterOperationHandler( SparkImpl manager, String clusterName )
    {
        super( manager, clusterName );

        trackerOperation = manager.getTracker().createTrackerOperation( SparkClusterConfig.PRODUCT_KEY,
                String.format( "Stopping %s cluster", clusterName ) );
    }


    @Override
    public void run()
    {

        SparkClusterConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            trackerOperation.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        Command stopCommand = manager.getCommands().getStopAllCommand( config.getMasterNode() );
        final AtomicBoolean ok = new AtomicBoolean();
        manager.getCommandRunner().runCommand( stopCommand, new CommandCallback()
        {
            @Override
            public void onResponse( Response response, AgentResult agentResult, Command command )
            {
                if ( agentResult.getStdOut().contains( "stopping" ) )
                {
                    ok.set( true );
                    stop();
                }
            }
        } );

        if ( ok.get() )
        {
            trackerOperation.addLogDone( "All nodes are stopped successfully." );
        }
        else
        {
            trackerOperation.addLogFailed( "Could not stop all nodes !" );
        }
    }
}
