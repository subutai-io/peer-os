package org.safehaus.subutai.plugin.accumulo.impl.handler;


import java.util.UUID;

import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.accumulo.impl.AccumuloImpl;
import org.safehaus.subutai.plugin.accumulo.impl.Commands;


/**
 * Handles stop cluster operation
 */
public class StopClusterOperationHandler extends AbstractOperationHandler<AccumuloImpl>
{

    public StopClusterOperationHandler( AccumuloImpl manager, String clusterName )
    {
        super( manager, clusterName );

        productOperation = manager.getTracker().createProductOperation( AccumuloClusterConfig.PRODUCT_KEY,
                String.format( "Stopping cluster %s", clusterName ) );
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

        productOperation.addLog( "Stopping cluster..." );

        Command stopCommand = Commands.getStopCommand( accumuloClusterConfig.getMasterNode() );
        manager.getCommandRunner().runCommand( stopCommand );

        if ( stopCommand.hasSucceeded() )
        {
            productOperation.addLogDone( "Cluster stopped successfully" );
        }
        else
        {
            productOperation.addLogFailed(
                    String.format( "Failed to stop cluster %s, %s", clusterName, stopCommand.getAllErrors() ) );
        }
    }
}
