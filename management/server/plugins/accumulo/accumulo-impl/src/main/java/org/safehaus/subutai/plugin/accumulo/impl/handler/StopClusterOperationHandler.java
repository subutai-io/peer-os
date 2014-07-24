package org.safehaus.subutai.plugin.accumulo.impl.handler;


import java.util.UUID;

import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.plugin.accumulo.impl.AccumuloImpl;
import org.safehaus.subutai.plugin.accumulo.impl.Commands;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;


/**
 * Created by dilshat on 5/6/14.
 */
public class StopClusterOperationHandler extends AbstractOperationHandler<AccumuloImpl> {
    private final ProductOperation po;


    public StopClusterOperationHandler( AccumuloImpl manager, String clusterName ) {
        super( manager, clusterName );

        po = manager.getTracker()
                    .createProductOperation( AccumuloClusterConfig.PRODUCT_KEY, String.format( "Stopping cluster %s", clusterName ) );
    }


    @Override
    public UUID getTrackerId() {
        return po.getId();
    }


    @Override
    public void run() {
        AccumuloClusterConfig accumuloClusterConfig = manager.getCluster( clusterName );
        if ( accumuloClusterConfig == null ) {
            po.addLogFailed( String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
            return;
        }

        if ( manager.getAgentManager().getAgentByHostname( accumuloClusterConfig.getMasterNode().getHostname() ) == null ) {
            po.addLogFailed( String.format( "Master node '%s' is not connected\nOperation aborted",
                    accumuloClusterConfig.getMasterNode().getHostname() ) );
            return;
        }

        po.addLog( "Stopping cluster..." );

        Command stopCommand = Commands.getStopCommand( accumuloClusterConfig.getMasterNode() );
        manager.getCommandRunner().runCommand( stopCommand );

        if ( stopCommand.hasSucceeded() ) {
            po.addLogDone( "Cluster stopped successfully" );
        }
        else {
            po.addLogFailed(
                    String.format( "Failed to stop cluster %s, %s", clusterName, stopCommand.getAllErrors() ) );
        }
    }
}
