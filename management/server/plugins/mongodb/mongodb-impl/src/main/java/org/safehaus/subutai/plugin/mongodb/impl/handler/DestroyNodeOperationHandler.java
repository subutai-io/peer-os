package org.safehaus.subutai.plugin.mongodb.impl.handler;


import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandCallback;
import org.safehaus.subutai.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.plugin.mongodb.api.MongoClusterConfig;
import org.safehaus.subutai.plugin.mongodb.api.NodeType;
import org.safehaus.subutai.plugin.mongodb.impl.MongoImpl;
import org.safehaus.subutai.plugin.mongodb.impl.common.Commands;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Response;

import com.google.common.base.Strings;


/**
 * Handles destroy mongo node operation
 */
public class DestroyNodeOperationHandler extends AbstractOperationHandler<MongoImpl> {
    private final ProductOperation po;
    private final String lxcHostname;


    public DestroyNodeOperationHandler( MongoImpl manager, String clusterName, String lxcHostname ) {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        po = manager.getTracker().createProductOperation( MongoClusterConfig.PRODUCT_KEY,
                String.format( "Destroying %s in %s", lxcHostname, clusterName ) );
    }


    @Override
    public UUID getTrackerId() {
        return po.getId();
    }


    @Override
    public void run() {
        final MongoClusterConfig config = manager.getCluster( clusterName );
        if ( config == null ) {
            po.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        Agent agent = manager.getAgentManager().getAgentByHostname( lxcHostname );
        if ( agent == null ) {
            po.addLogFailed( String.format( "Agent with hostname %s is not connected", lxcHostname ) );
            return;
        }
        if ( !config.getAllNodes().contains( agent ) ) {
            po.addLogFailed(
                    String.format( "Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName ) );
            return;
        }

        final NodeType nodeType = config.getNodeType( agent );
        if ( nodeType == NodeType.CONFIG_NODE && config.getConfigServers().size() == 1 ) {
            po.addLogFailed( "This is the last configuration server in the cluster. Please, destroy cluster instead" );
            return;
        }
        else if ( nodeType == NodeType.DATA_NODE && config.getDataNodes().size() == 1 ) {
            po.addLogFailed( "This is the last data node in the cluster. Please, destroy cluster instead" );
            return;
        }
        else if ( nodeType == NodeType.ROUTER_NODE && config.getRouterServers().size() == 1 ) {
            po.addLogFailed( "This is the last router in the cluster. Please, destroy cluster instead" );
            return;
        }

        if ( nodeType == NodeType.CONFIG_NODE ) {
            config.getConfigServers().remove( agent );
            //restart routers
            po.addLog( "Restarting routers..." );
            Command stopRoutersCommand = Commands.getStopNodeCommand( config.getRouterServers() );
            manager.getCommandRunner().runCommand( stopRoutersCommand );
            //don't check status of this command since it always ends with execute_timeouted
            if ( stopRoutersCommand.hasCompleted() ) {
                final AtomicInteger okCount = new AtomicInteger();
                manager.getCommandRunner().runCommand(
                        Commands.getStartRouterCommand( config.getRouterPort(), config.getCfgSrvPort(),
                                config.getDomainName(), config.getConfigServers(), config.getRouterServers() ),
                        new CommandCallback() {

                            @Override
                            public void onResponse( Response response, AgentResult agentResult, Command command ) {
                                for ( AgentResult result : command.getResults().values() ) {
                                    if ( result.getStdOut()
                                               .contains( "child process started successfully, parent exiting" )
                                            && okCount.incrementAndGet() == config.getRouterServers().size() ) {
                                        stop();
                                    }
                                }
                            }
                        } );

                if ( okCount.get() != config.getRouterServers().size() ) {
                    po.addLog( "Not all routers restarted. Use Terminal module to restart them, skipping..." );
                }
            }
            else {
                po.addLog( "Could not restart routers. Use Terminal module to restart them, skipping..." );
            }
        }
        else if ( nodeType == NodeType.DATA_NODE ) {
            config.getDataNodes().remove( agent );
            //unregister from primary
            po.addLog( "Unregistering this node from replica set..." );
            Command findPrimaryNodeCommand = Commands.getFindPrimaryNodeCommand( agent, config.getDataNodePort() );
            manager.getCommandRunner().runCommand( findPrimaryNodeCommand );

            if ( findPrimaryNodeCommand.hasCompleted() && !findPrimaryNodeCommand.getResults().isEmpty() ) {
                Pattern p = Pattern.compile( "primary\" : \"(.*)\"" );
                Matcher m = p.matcher( findPrimaryNodeCommand.getResults().get( agent.getUuid() ).getStdOut() );
                Agent primaryNodeAgent = null;
                if ( m.find() ) {
                    String primaryNodeHost = m.group( 1 );
                    if ( !Strings.isNullOrEmpty( primaryNodeHost ) ) {
                        String hostname = primaryNodeHost.split( ":" )[0].replace( "." + config.getDomainName(), "" );
                        primaryNodeAgent = manager.getAgentManager().getAgentByHostname( hostname );
                    }
                }
                if ( primaryNodeAgent != null ) {
                    if ( primaryNodeAgent != agent ) {
                        Command unregisterSecondaryNodeFromPrimaryCommand =
                                Commands.getUnregisterSecondaryNodeFromPrimaryCommand( primaryNodeAgent,
                                        config.getDataNodePort(), agent, config.getDomainName() );

                        manager.getCommandRunner().runCommand( unregisterSecondaryNodeFromPrimaryCommand );
                        if ( !unregisterSecondaryNodeFromPrimaryCommand.hasCompleted() ) {
                            po.addLog( "Could not unregister this node from replica set, skipping..." );
                        }
                    }
                }
                else {
                    po.addLog( "Could not determine primary node for unregistering from replica set, skipping..." );
                }
            }
            else {
                po.addLog( "Could not determine primary node for unregistering from replica set, skipping..." );
            }
        }
        else if ( nodeType == NodeType.ROUTER_NODE ) {
            config.getRouterServers().remove( agent );
        }
        //destroy lxc
        po.addLog( "Destroying lxc container..." );
        Agent physicalAgent = manager.getAgentManager().getAgentByHostname( agent.getParentHostName() );
        if ( physicalAgent == null ) {
            po.addLog(
                    String.format( "Could not determine physical parent of %s. Use LXC module to cleanup, skipping...",
                            agent.getHostname() ) );
        }
        else {
            try {
                manager.getContainerManager().cloneDestroy( physicalAgent.getHostname(), agent.getHostname() );
                po.addLog( "Lxc container destroyed successfully" );
            }
            catch ( LxcDestroyException e ) {
                po.addLog( String.format( "Could not destroy lxc container %s. Use LXC module to cleanup, skipping...",
                        e.getMessage() ) );
            }
        }
        //update db
        po.addLog( "Updating db..." );
        if ( !manager.getDbManager().saveInfo( MongoClusterConfig.PRODUCT_KEY, config.getClusterName(), config ) ) {
            po.addLogFailed( String.format( "Error while updating cluster information [%s] in database. Check logs",
                    config.getClusterName() ) );
        }
        else {
            po.addLogDone( "Done" );
        }
    }
}
