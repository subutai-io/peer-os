package org.safehaus.subutai.impl.mongodb.handler;


import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandCallback;
import org.safehaus.subutai.api.dbmanager.DBException;
import org.safehaus.subutai.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.api.mongodb.Config;
import org.safehaus.subutai.impl.mongodb.MongoImpl;
import org.safehaus.subutai.impl.mongodb.common.CommandType;
import org.safehaus.subutai.impl.mongodb.common.Commands;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.operation.ProductOperationState;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Response;

import com.google.common.base.Strings;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;


/**
 * Installs Mongo cluster
 */
public class InstallOperationHandler extends AbstractOperationHandler<MongoImpl> {
    private final ProductOperation po;
    private final Config config;


    public InstallOperationHandler( MongoImpl manager, Config config ) {
        super( manager, config.getClusterName() );
        this.config = config;
        po = manager.getTracker()
                    .createProductOperation( Config.PRODUCT_KEY, String.format( "Installing %s", Config.PRODUCT_KEY ) );
    }


    @Override
    public UUID getTrackerId() {
        return po.getId();
    }


    @Override
    public void run() {

        if ( Strings.isNullOrEmpty( config.getClusterName() ) ||
                Strings.isNullOrEmpty( config.getDomainName() ) ||
                Strings.isNullOrEmpty( config.getReplicaSetName() ) ||
                !Sets.newHashSet( 1, 3 ).contains( config.getNumberOfConfigServers() ) ||
                !Range.closed( 1, 3 ).contains( config.getNumberOfRouters() ) ||
                !Sets.newHashSet( 3, 5, 7 ).contains( config.getNumberOfDataNodes() ) ||
                !Range.closed( 1024, 65535 ).contains( config.getCfgSrvPort() ) ||
                !Range.closed( 1024, 65535 ).contains( config.getRouterPort() ) ||
                !Range.closed( 1024, 65535 ).contains( config.getDataNodePort() ) ) {
            po.addLogFailed( "Malformed configuration\nInstallation aborted" );
            return;
        }

        //check if mongo cluster with the same name already exists
        if ( manager.getCluster( config.getClusterName() ) != null ) {
            po.addLogFailed( String.format( "Cluster with name '%s' already exists", config.getClusterName() ) );
            return;
        }

        try {
            int numberOfLxcsNeeded =
                    config.getNumberOfConfigServers() + config.getNumberOfRouters() + config.getNumberOfDataNodes();
            //clone lxc containers
            po.addLog( String.format( "Creating %d lxc containers...", numberOfLxcsNeeded ) );

            Map<Agent, Set<Agent>> agents = manager.getLxcManager().createLxcs( numberOfLxcsNeeded );

            Set<Agent> allAgents = new HashSet<>();
            for ( Set<Agent> ag : agents.values() ) {
                allAgents.addAll( ag );
            }

            if ( allAgents.size() < numberOfLxcsNeeded ) {
                po.addLogFailed( String.format( "Needed %d containers but %d were created", numberOfLxcsNeeded,
                        allAgents.size() ) );
            }
            else {

                Iterator<Agent> it = allAgents.iterator();
                for ( int i = 0; i < config.getNumberOfConfigServers(); i++ ) {
                    config.getConfigServers().add( it.next() );
                }
                for ( int i = 0; i < config.getNumberOfRouters(); i++ ) {
                    config.getRouterServers().add( it.next() );
                }
                for ( int i = 0; i < config.getNumberOfDataNodes(); i++ ) {
                    config.getDataNodes().add( it.next() );
                }

                //            Map<NodeType, Set<Agent>> nodes = CustomPlacementStrategy
                //                    .getNodes( manager.getLxcManager(), config.getNumberOfConfigServers(),
                // config.getNumberOfRouters(),
                //                            config.getNumberOfDataNodes() );
                //            config.setConfigServers( nodes.get( NodeType.CONFIG_NODE ) );
                //            config.setDataNodes( nodes.get( NodeType.DATA_NODE ) );
                //            config.setRouterServers( nodes.get( NodeType.ROUTER_NODE ) );


                po.addLog( "Lxc containers created successfully" );

                if ( installMongoCluster( config, po ) ) {
                    po.addLog( "Updating db..." );

                    try {
                        manager.getDbManager().saveInfo2( Config.PRODUCT_KEY, config.getClusterName(), config );

                        po.addLogDone( "Database information updated" );
                    }
                    catch ( DBException e ) {
                        po.addLogFailed( String.format( "Failed to update database information, %s", e.getMessage() ) );
                    }
                }
                else {
                    po.addLogFailed( "Installation failed" );
                }
            }

            if ( po.getState() != ProductOperationState.SUCCEEDED ) {
                try {
                    manager.getLxcManager().destroyLxcs( config.getAllNodes() );
                }
                catch ( LxcDestroyException ignore ) {
                }
            }
        }
        catch ( LxcCreateException ex ) {
            po.addLogFailed( ex.getMessage() );
        }
    }


    private boolean installMongoCluster( final Config config, final ProductOperation po ) {

        List<Command> installationCommands = Commands.getInstallationCommands( config );

        boolean installationOK = true;

        for ( Command command : installationCommands ) {
            po.addLog( String.format( "Running command: %s", command.getDescription() ) );
            final AtomicBoolean commandOK = new AtomicBoolean();

            if ( command.getData() == CommandType.START_CONFIG_SERVERS || command.getData() == CommandType.START_ROUTERS
                    || command.getData() == CommandType.START_DATA_NODES ) {
                manager.getCommandRunner().runCommand( command, new CommandCallback() {

                    @Override
                    public void onResponse( Response response, AgentResult agentResult, Command command ) {

                        int count = 0;
                        for ( AgentResult result : command.getResults().values() ) {
                            if ( result.getStdOut().contains( "child process started successfully, parent exiting" ) ) {
                                count++;
                            }
                        }
                        if ( command.getData() == CommandType.START_CONFIG_SERVERS ) {
                            if ( count == config.getConfigServers().size() ) {
                                commandOK.set( true );
                            }
                        }
                        else if ( command.getData() == CommandType.START_ROUTERS ) {
                            if ( count == config.getRouterServers().size() ) {
                                commandOK.set( true );
                            }
                        }
                        else if ( command.getData() == CommandType.START_DATA_NODES ) {
                            if ( count == config.getDataNodes().size() ) {
                                commandOK.set( true );
                            }
                        }
                        if ( commandOK.get() ) {
                            stop();
                        }
                    }
                } );
            }
            else {
                manager.getCommandRunner().runCommand( command );
            }

            if ( command.hasSucceeded() || commandOK.get() ) {
                po.addLog( String.format( "Command %s succeeded", command.getDescription() ) );
            }
            else {
                po.addLog( String.format( "Command %s failed: %s", command.getDescription(), command.getAllErrors() ) );
                installationOK = false;
                break;
            }
        }

        return installationOK;
    }
}
