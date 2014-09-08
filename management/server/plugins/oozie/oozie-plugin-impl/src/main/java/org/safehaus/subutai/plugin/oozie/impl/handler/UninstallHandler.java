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
public class UninstallHandler extends AbstractOperationHandler<OozieImpl> {

    private ProductOperation po;
    //    private OozieConfig config;
    private String clusterName;


    public UninstallHandler( final OozieImpl manager, final String clusterName ) {
        super( manager, clusterName );
        this.clusterName = clusterName;
        po = manager.getTracker().createProductOperation( OozieClusterConfig.PRODUCT_KEY,
                String.format( "Unistalling %s cluster...", clusterName ) );
    }


    @Override
    public void run() {
        final ProductOperation po = manager.getTracker().createProductOperation( OozieClusterConfig.PRODUCT_KEY,
                String.format( "Destroying cluster %s", clusterName ) );
        manager.getExecutor().execute( new Runnable() {

            public void run() {
                OozieClusterConfig config = manager.getDbManager().getInfo( OozieClusterConfig.PRODUCT_KEY, clusterName,
                        OozieClusterConfig.class );
                if ( config == null ) {
                    po.addLogFailed(
                            String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
                    return;
                }

                Set<String> nodes = new HashSet<String>();
                nodes.addAll( config.getClients() );
                nodes.add( config.getServer() );
                for ( String node : nodes ) {
                    if ( manager.getAgentManager().getAgentByHostname( node ) == null ) {
                        po.addLogFailed( String.format( "Node %s not connected\nAborted", node ) );
                        return;
                    }
                }

                Set<Agent> servers = new HashSet<Agent>();
                Agent serverAgent = manager.getAgentManager().getAgentByHostname( config.getServer() );
                servers.add( serverAgent );

                Command uninstallServerCommand = Commands.getUninstallServerCommand( servers );
                manager.getCommandRunner().runCommand( uninstallServerCommand );

                if ( uninstallServerCommand.hasSucceeded() ) {
                    po.addLog( "Uninstall server succeeded" );
                }
                else {
                    po.addLogFailed(
                            String.format( "Uninstall server failed, %s", uninstallServerCommand.getAllErrors() ) );
                    return;
                }

                Set<Agent> clientAgents = new HashSet<Agent>();
                for ( String clientHostname : config.getClients() ) {
                    Agent clientAgent = manager.getAgentManager().getAgentByHostname( clientHostname );
                    clientAgents.add( clientAgent );
                }
                Command uninstallClientsCommand = Commands.getUninstallClientsCommand( clientAgents );
                manager.getCommandRunner().runCommand( uninstallClientsCommand );

                if ( uninstallClientsCommand.hasSucceeded() ) {
                    po.addLog( "Uninstall clients succeeded" );
                }
                else {
                    po.addLogFailed(
                            String.format( "Uninstall clients failed, %s", uninstallClientsCommand.getAllErrors() ) );
                    return;
                }

                po.addLog( "Updating db..." );
                if ( manager.getDbManager().deleteInfo( OozieClusterConfig.PRODUCT_KEY, config.getClusterName() ) ) {
                    po.addLogDone( "Cluster info deleted from DB\nDone" );
                }
                else {
                    po.addLogFailed( "Error while deleting cluster info from DB. Check logs.\nFailed" );
                }
            }
        } );
    }
}
