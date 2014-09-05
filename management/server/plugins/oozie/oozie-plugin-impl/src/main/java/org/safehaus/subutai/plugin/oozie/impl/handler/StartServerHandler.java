package org.safehaus.subutai.plugin.oozie.impl.handler;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.plugin.oozie.api.OozieClusterConfig;
import org.safehaus.subutai.plugin.oozie.impl.Commands;
import org.safehaus.subutai.plugin.oozie.impl.OozieImpl;


/**
 * Created by bahadyr on 8/25/14.
 */
public class StartServerHandler extends AbstractOperationHandler<OozieImpl> {

    private ProductOperation po;
    private String clusterName;


    public StartServerHandler( final OozieImpl manager, final String clusterName ) {
        super( manager, clusterName );
        this.clusterName = clusterName;
        po = manager.getTracker().createProductOperation( OozieClusterConfig.PRODUCT_KEY,
                String.format( "Starting server on %s cluster...", clusterName ) );
    }


    @Override
    public void run() {
        final ProductOperation po = manager.getTracker().createProductOperation( OozieClusterConfig.PRODUCT_KEY,
                String.format( "Starting cluster %s", clusterName ) );
        manager.getExecutor().execute( new Runnable() {

            public void run() {
                OozieClusterConfig config = manager.getDbManager().getInfo( OozieClusterConfig.PRODUCT_KEY, clusterName,
                        OozieClusterConfig.class );
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
                Command startServiceCommand = Commands.getStartServerCommand( servers );
                manager.getCommandRunner().runCommand( startServiceCommand );

                if ( startServiceCommand.hasCompleted() ) {
                    po.addLog( "Checking status..." );

                    Command checkCommand = Commands.getStatusServerCommand( servers );
                    manager.getCommandRunner().runCommand( checkCommand );

                    if ( checkCommand.hasCompleted() ) {

                        po.addLogDone( checkCommand.getResults().get( serverAgent.getUuid() ).getStdOut() );
                    }
                    else {
                        po.addLogFailed( String.format( "Failed to check status, %s", checkCommand.getAllErrors() ) );
                    }
                }
                else {
                    po.addLogFailed( String.format( "Start failed, %s", startServiceCommand.getAllErrors() ) );
                }
            }
        } );
    }
}
