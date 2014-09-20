package org.safehaus.subutai.plugin.accumulo.impl.handler;


import java.util.UUID;

import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.accumulo.impl.AccumuloImpl;
import org.safehaus.subutai.plugin.accumulo.impl.Commands;


/**
 * Handles start cluster operation
 */
public class StartClusterOperationHandler extends AbstractOperationHandler<AccumuloImpl>
{

    public StartClusterOperationHandler( AccumuloImpl manager, String clusterName )
    {
        super( manager, clusterName );
        productOperation = manager.getTracker().createProductOperation( AccumuloClusterConfig.PRODUCT_KEY,
                String.format( "Starting cluster %s", clusterName ) );
    }


    @Override
    public UUID getTrackerId()
    {
        return productOperation.getId();
    }


    @Override
    public void run()
    {
        AccumuloClusterConfig accumuloClusterConfig = manager.getCluster( clusterName );
        if ( accumuloClusterConfig == null )
        {
            productOperation.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        if ( manager.getAgentManager().getAgentByHostname( accumuloClusterConfig.getMasterNode().getHostname() )
                == null )
        {
            productOperation.addLogFailed( String.format( "Master node '%s' is not connected",
                    accumuloClusterConfig.getMasterNode().getHostname() ) );
            return;
        }

        productOperation.addLog( "Starting cluster..." );

        Command startCommand = Commands.getStartCommand( accumuloClusterConfig.getMasterNode() );
        manager.getCommandRunner().runCommand( startCommand );

        if ( startCommand.hasSucceeded() )
        {
            productOperation.addLogDone( "Cluster started successfully" );
        }
        else
        {
            productOperation.addLogFailed(
                    String.format( "Failed to start cluster %s, %s", clusterName, startCommand.getAllErrors() ) );
        }
    }
}
