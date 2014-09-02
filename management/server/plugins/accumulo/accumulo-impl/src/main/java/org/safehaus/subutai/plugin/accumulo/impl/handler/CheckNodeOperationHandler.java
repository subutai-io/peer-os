package org.safehaus.subutai.plugin.accumulo.impl.handler;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.accumulo.impl.AccumuloImpl;
import org.safehaus.subutai.plugin.accumulo.impl.Commands;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;


import java.util.UUID;


/**
 * Check node status operation handler
 */
public class CheckNodeOperationHandler extends AbstractOperationHandler<AccumuloImpl> {
    private final String lxcHostname;

    public CheckNodeOperationHandler( AccumuloImpl manager, String clusterName, String lxcHostname ) {
        super( manager, clusterName );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcHostname ), "Lxc hostname is null or empty" );
        this.lxcHostname = lxcHostname;
        productOperation = manager.getTracker().createProductOperation( AccumuloClusterConfig.PRODUCT_KEY,
                String.format( "Checking node %s in %s", lxcHostname, clusterName ) );
    }


    @Override
    public UUID getTrackerId() {
        return productOperation.getId();
    }


    @Override
    public void run() {
        AccumuloClusterConfig accumuloClusterConfig = manager.getCluster( clusterName );
        if ( accumuloClusterConfig == null ) {
            productOperation.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        final Agent node = manager.getAgentManager().getAgentByHostname( lxcHostname );
        if ( node == null ) {
            productOperation.addLogFailed( String.format( "Agent with hostname %s is not connected", lxcHostname ) );
            return;
        }
        if ( !accumuloClusterConfig.getAllNodes().contains( node ) ) {
            productOperation.addLogFailed(
                    String.format( "Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName ) );
            return;
        }

        Command checkNodeCommand = Commands.getStatusCommand( node );
        manager.getCommandRunner().runCommand( checkNodeCommand );

        if ( checkNodeCommand.hasSucceeded() ) {
            productOperation.addLogDone( String.format( "Status on %s is %s", lxcHostname,
                    checkNodeCommand.getResults().get( node.getUuid() ).getStdOut() ) );
        }
        else {
            productOperation.addLogFailed(
                    String.format( "Failed to check status of %s, %s", lxcHostname, checkNodeCommand.getAllErrors() ) );
        }
    }
}
