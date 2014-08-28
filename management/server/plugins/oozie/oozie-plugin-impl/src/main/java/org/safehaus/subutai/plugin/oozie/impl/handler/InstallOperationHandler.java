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
import org.safehaus.subutai.shared.protocol.Util;
import org.safehaus.subutai.shared.protocol.settings.Common;


/**
 * Created by bahadyr on 8/25/14.
 */
public class InstallOperationHandler extends AbstractOperationHandler<OozieImpl> {

    private ProductOperation po;
    private OozieConfig config;


    public InstallOperationHandler( final OozieImpl manager, final OozieConfig config ) {
        super( manager, config.getClusterName() );
        this.config = config;
        po = manager.getTracker().createProductOperation( OozieConfig.PRODUCT_KEY,
                String.format( "Setting up %s cluster...", config.getClusterName() ) );
    }


    @Override
    public void run() {
        final ProductOperation po =
                manager.getTracker().createProductOperation( OozieConfig.PRODUCT_KEY, "Installing Oozie" );

        manager.getExecutor().execute( new Runnable() {

            public void run() {
                if ( manager.getDbManager().getInfo( config.PRODUCT_KEY, config.getClusterName(), OozieConfig.class )
                        != null ) {
                    po.addLogFailed( String.format( "Cluster with name '%s' already exists\nInstallation aborted",
                                    config.getClusterName() ) );
                    return;
                }


                for ( String node : config.getHadoopNodes() ) {
                    if ( manager.getAgentManager().getAgentByHostname( node ) == null ) {
                        po.addLogFailed( String.format( "Node %s not connected\nAborted", node ) );
                        return;
                    }
                }

                if ( manager.getDbManager().saveInfo( config.PRODUCT_KEY, config.getClusterName(), config ) ) {
                    po.addLog( "Cluster info saved to DB" );

                    //                    Set<Agent> allNodes = new HashSet<Agent>();
                    //                    allNodes.add(config.getServer());
                    //                    allNodes.addAll(config.getClients());

                    // Installing Oozie server
                    po.addLog( "Installing Oozie server..." );
                    Set<Agent> servers = new HashSet<Agent>();
                    Agent serverAgent = manager.getAgentManager().getAgentByHostname( config.getServer() );
                    servers.add( serverAgent );
                    Command installServerCommand = Commands.getInstallServerCommand( servers );
                    manager.getCommandRunner().runCommand( installServerCommand );

                    if ( installServerCommand.hasSucceeded() ) {
                        po.addLog( "Install server successful." );
                    }
                    else {
                        po.addLogFailed(
                                String.format( "Installation failed, %s", installServerCommand.getAllErrors() ) );
                        return;
                    }

                    // Installing Oozie client
                    po.addLog( "Installing Oozie clients..." );
                    Set<Agent> clientAgents = new HashSet<Agent>();
                    for ( String clientAgent : config.getClients() ) {
                        Agent client = manager.getAgentManager().getAgentByHostname( clientAgent );
                        clientAgents.add( client );
                    }
                    Command installClientsCommand = Commands.getInstallClientCommand( clientAgents );
                    manager.getCommandRunner().runCommand( installClientsCommand );

                    if ( installClientsCommand.hasSucceeded() ) {
                        po.addLog( "Install clients successful." );
                    }
                    else {
                        po.addLogFailed(
                                String.format( "Installation failed, %s", installClientsCommand.getAllErrors() ) );
                        return;
                    }

                    po.addLog( "Configuring root hosts..." );
                    Agent server = manager.getAgentManager().getAgentByHostname( config.getServer() );
                    Set<Agent> hadoopNodes = new HashSet<Agent>();
                    for ( String hadoopNode : config.getHadoopNodes() ) {
                        Agent hadoopNodeAgent = manager.getAgentManager().getAgentByHostname( hadoopNode );
                        hadoopNodes.add( hadoopNodeAgent );
                    }
                    Command configureRootHostsCommand = Commands.getConfigureRootHostsCommand( hadoopNodes,
                            Util.getAgentIpByMask( server, Common.IP_MASK ) );
                    manager.getCommandRunner().runCommand( configureRootHostsCommand );

                    if ( configureRootHostsCommand.hasSucceeded() ) {
                        po.addLog( "Configuring root hosts successful." );
                    }
                    else {
                        po.addLogFailed(
                                String.format( "Configuration failed, %s", configureRootHostsCommand.getAllErrors() ) );
                        return;
                    }

                    po.addLog( "Configuring root groups..." );
                    Command configureRootGroupsCommand = Commands.getConfigureRootGroupsCommand( hadoopNodes );
                    manager.getCommandRunner().runCommand( configureRootGroupsCommand );

                    if ( configureRootGroupsCommand.hasSucceeded() ) {
                        po.addLog( "Configuring root groups successful." );
                    }
                    else {
                        po.addLogFailed(
                                String.format( "Configuring failed, %s", configureRootGroupsCommand.getAllErrors() ) );
                        return;
                    }
                    po.addLogDone( "Oozie installation succeeded" );
                }
                else {
                    po.addLogFailed( "Could not save cluster info to DB! Please see logs\nInstallation aborted" );
                }
            }
        } );
    }
}
