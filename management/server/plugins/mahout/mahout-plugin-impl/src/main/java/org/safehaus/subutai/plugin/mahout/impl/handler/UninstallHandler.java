package org.safehaus.subutai.plugin.mahout.impl.handler;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.command.AgentResult;
import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.plugin.mahout.api.MahoutClusterConfig;
import org.safehaus.subutai.plugin.mahout.impl.Commands;
import org.safehaus.subutai.plugin.mahout.impl.MahoutImpl;


/**
 * Created by dilshat on 5/6/14.
 */
public class UninstallHandler extends AbstractOperationHandler<MahoutImpl> {
    private final ProductOperation po;


    public UninstallHandler( MahoutImpl manager, String clusterName ) {
        super( manager, clusterName );
        po = manager.getTracker().createProductOperation( MahoutClusterConfig.PRODUCT_KEY,
                String.format( "Destroying cluster %s", clusterName ) );
    }


    @Override
    public UUID getTrackerId() {
        return po.getId();
    }


    @Override
    public void run() {
        MahoutClusterConfig config = manager.getCluster( clusterName );
        if ( config == null ) {
            po.addLogFailed( String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
            return;
        }

        for ( Agent node : config.getNodes() ) {
            if ( manager.getAgentManager().getAgentByHostname( node.getHostname() ) == null ) {
                po.addLogFailed( String.format( "Node %s is not connected\nOperation aborted", node.getHostname() ) );
                return;
            }
        }

        po.addLog( "Uninstalling Mahout..." );

        Command uninstallCommand = Commands.getUninstallCommand( config.getNodes() );
        manager.getCommandRunner().runCommand( uninstallCommand );

        if ( uninstallCommand.hasCompleted() ) {
            for ( AgentResult result : uninstallCommand.getResults().values() ) {
                Agent agent = manager.getAgentManager().getAgentByUUID( result.getAgentUUID() );
                if ( result.getExitCode() != null && result.getExitCode() == 0 ) {
                    if ( result.getStdOut().contains( "Package ksks-mahout is not installed, so not removed" ) ) {
                        po.addLog( String.format( "Mahout is not installed, so not removed on node %s",
                                agent == null ? result.getAgentUUID() : agent.getHostname() ) );
                    }
                    else {
                        po.addLog( String.format( "Mahout is removed from node %s",
                                agent == null ? result.getAgentUUID() : agent.getHostname() ) );
                    }
                }
                else {
                    po.addLog( String.format( "Error %s on node %s", result.getStdErr(),
                            agent == null ? result.getAgentUUID() : agent.getHostname() ) );
                }
            }
            po.addLog( "Updating db..." );
            if ( manager.getDbManager().deleteInfo( MahoutClusterConfig.PRODUCT_KEY, config.getClusterName() ) ) {
                po.addLogDone( "Cluster info deleted from DB\nDone" );
            }
            else {
                po.addLogFailed( "Error while deleting cluster info from DB. Check logs.\nFailed" );
            }
        }
        else {
            po.addLogFailed( "Uninstallation failed, command timed out" );
        }
    }
}
