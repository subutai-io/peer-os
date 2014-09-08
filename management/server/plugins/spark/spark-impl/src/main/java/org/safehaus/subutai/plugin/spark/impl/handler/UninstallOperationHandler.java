package org.safehaus.subutai.plugin.spark.impl.handler;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.command.api.AgentResult;
import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.plugin.spark.impl.Commands;
import org.safehaus.subutai.plugin.spark.impl.SparkImpl;


/**
 * Created by dilshat on 5/7/14.
 */
public class UninstallOperationHandler extends AbstractOperationHandler<SparkImpl> {
    private final ProductOperation po;


    public UninstallOperationHandler( SparkImpl manager, String clusterName ) {
        super( manager, clusterName );
        po = SparkImpl.getTracker().createProductOperation( SparkClusterConfig.PRODUCT_KEY,
                String.format( "Destroying cluster %s", clusterName ) );
    }


    @Override
    public UUID getTrackerId() {
        return po.getId();
    }


    @Override
    public void run() {
        productOperation = po;
        SparkClusterConfig config = manager.getCluster( clusterName );
        if ( config == null ) {
            po.addLogFailed( String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
            return;
        }

        for ( Agent node : config.getAllNodes() ) {
            if ( SparkImpl.getAgentManager().getAgentByHostname( node.getHostname() ) == null ) {
                po.addLogFailed( String.format( "Node %s is not connected\nOperation aborted", node.getHostname() ) );
                return;
            }
        }

        po.addLog( "Uninstalling Spark..." );

        Command uninstallCommand = Commands.getUninstallCommand( config.getAllNodes() );
        SparkImpl.getCommandRunner().runCommand( uninstallCommand );

        if ( uninstallCommand.hasCompleted() ) {
            for ( AgentResult result : uninstallCommand.getResults().values() ) {
                Agent agent = SparkImpl.getAgentManager().getAgentByUUID( result.getAgentUUID() );
                if ( result.getExitCode() != null && result.getExitCode() == 0 ) {
                    if ( result.getStdOut().contains( "Package ksks-spark is not installed, so not removed" ) ) {
                        po.addLog( String.format( "Spark is not installed, so not removed on node %s",
                                agent == null ? result.getAgentUUID() : agent.getHostname() ) );
                    }
                    else {
                        po.addLog( String.format( "Spark is removed from node %s",
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
                SparkImpl.getPluginDAO().deleteInfo( SparkClusterConfig.PRODUCT_KEY, config.getClusterName() );
                po.addLogFailed( "Error while deleting cluster info from DB. Check logs.\nFailed" );
            }
            catch ( DBException ex ) {
                po.addLogDone( "Cluster info deleted from DB\nDone" );
            }
        }
        else {
            po.addLogFailed( String.format( "Uninstallation failed, %s", uninstallCommand.getAllErrors() ) );
        }
    }
}
