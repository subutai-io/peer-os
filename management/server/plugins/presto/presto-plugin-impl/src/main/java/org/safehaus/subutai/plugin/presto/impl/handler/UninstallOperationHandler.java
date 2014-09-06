package org.safehaus.subutai.plugin.presto.impl.handler;


import java.util.UUID;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.command.api.AgentResult;
import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;
import org.safehaus.subutai.plugin.presto.impl.Commands;
import org.safehaus.subutai.plugin.presto.impl.PrestoImpl;


public class UninstallOperationHandler extends AbstractOperationHandler<PrestoImpl> {
    private final ProductOperation po;


    public UninstallOperationHandler( PrestoImpl manager, String clusterName ) {
        super( manager, clusterName );
        po = PrestoImpl.getTracker().createProductOperation( PrestoClusterConfig.PRODUCT_KEY,
                String.format( "Destroying cluster %s", clusterName ) );
    }


    @Override
    public UUID getTrackerId() {
        return po.getId();
    }


    @Override
    public void run() {
        productOperation = po;
        PrestoClusterConfig config = manager.getCluster( clusterName );
        if ( config == null ) {
            po.addLogFailed( String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
            return;
        }

        for ( Agent node : config.getAllNodes() ) {
            if ( PrestoImpl.getAgentManager().getAgentByHostname( node.getHostname() ) == null ) {
                po.addLogFailed( String.format( "Node %s is not connected\nOperation aborted", node.getHostname() ) );
                return;
            }
        }

        po.addLog( "Uninstalling Presto..." );

        Command uninstallCommand = Commands.getUninstallCommand( config.getAllNodes() );
        PrestoImpl.getCommandRunner().runCommand( uninstallCommand );

        if ( uninstallCommand.hasCompleted() ) {
            for ( AgentResult result : uninstallCommand.getResults().values() ) {
                Agent agent = PrestoImpl.getAgentManager().getAgentByUUID( result.getAgentUUID() );
                if ( result.getExitCode() != null && result.getExitCode() == 0 ) {
                    if(result.getStdOut().contains("Presto is not installed, so not removed"))
                        po.addLog( String.format( "Presto is not installed, so not removed on node %s",
                                agent == null ? result.getAgentUUID() : agent.getHostname() ) );
                    }
                    else {
                        po.addLog( String.format( "Presto is removed from node %s",
                                agent == null ? result.getAgentUUID() : agent.getHostname() ) );
                    }
                }
                else {
                    po.addLog( String.format( "Error %s on node %s", result.getStdErr(),
                            agent == null ? result.getAgentUUID() : agent.getHostname() ) );
                }
            }
            po.addLog( "Updating db..." );
            try {
                PrestoImpl.getPluginDAO().deleteInfo( PrestoClusterConfig.PRODUCT_KEY, config.getClusterName() );
                po.addLogDone( "Cluster info deleted from DB\nDone" );
            }
            catch ( DBException e ) {
                po.addLogFailed( "Error while deleting cluster info from DB. Check logs.\nFailed" );
            }
        }
        else {
            po.addLogFailed( String.format( "Uninstallation failed, %s", uninstallCommand.getAllErrors() ) );
        }
    }
}
