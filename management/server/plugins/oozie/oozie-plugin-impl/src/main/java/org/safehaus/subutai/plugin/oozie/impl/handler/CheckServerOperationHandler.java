package org.safehaus.subutai.plugin.oozie.impl.handler;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.plugin.oozie.api.OozieConfig;
import org.safehaus.subutai.plugin.oozie.impl.Commands;
import org.safehaus.subutai.plugin.oozie.impl.OozieImpl;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;


/**
 * Created by bahadyr on 8/25/14.
 */
public class CheckServerOperationHandler extends AbstractOperationHandler<OozieImpl> {

    private ProductOperation po;
    private String clusterName;


    public CheckServerOperationHandler( final OozieImpl manager, final String clusterName ) {
        super( manager, clusterName );
        this.clusterName = clusterName;
        po = manager.getTracker().createProductOperation( OozieConfig.PRODUCT_KEY,
                String.format( "Starting server on %s cluster...", clusterName ) );
    }


    @Override
    public void run() {
        final ProductOperation po = manager.getTracker().createProductOperation( OozieConfig.PRODUCT_KEY,
                String.format( "Checking status of cluster %s", clusterName ) );
        manager.getExecutor().execute( new Runnable() {

            public void run() {
                OozieConfig config =
                        manager.getDbManager().getInfo( OozieConfig.PRODUCT_KEY, clusterName, OozieConfig.class );
                if ( config == null ) {
                    po.addLogFailed( String.format( "Cluster with name %s does not exist\nOperation aborted",
                            config.getClusterName() ) );
                    return;
                }
                Agent serverAgent = manager.getAgentManager().getAgentByHostname( config.getServer() );
                if ( serverAgent == null ) {
                    po.addLogFailed( String.format( "Server agent %s not connected", config.getServer() ) );
                    return;
                }
                Set<Agent> servers = new HashSet<Agent>();
                servers.add( serverAgent );
                Command statusServiceCommand = Commands.getStatusServerCommand( servers );
                manager.getCommandRunner().runCommand( statusServiceCommand );

                if ( statusServiceCommand.hasCompleted() ) {

                    po.addLogDone( statusServiceCommand.getResults().get( serverAgent.getUuid() ).getStdOut() );
                }
                else {
                    po.addLogFailed(
                            String.format( "Failed to check status, %s", statusServiceCommand.getAllErrors() ) );
                }
            }
        } );
    }
}
