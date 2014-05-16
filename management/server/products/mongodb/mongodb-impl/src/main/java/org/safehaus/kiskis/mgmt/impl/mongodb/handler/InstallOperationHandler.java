package org.safehaus.kiskis.mgmt.impl.mongodb.handler;


import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.safehaus.kiskis.mgmt.api.commandrunner.AgentResult;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandCallback;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcCreateException;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcDestroyException;
import org.safehaus.kiskis.mgmt.api.mongodb.Config;
import org.safehaus.kiskis.mgmt.api.mongodb.NodeType;
import org.safehaus.kiskis.mgmt.impl.mongodb.CustomPlacementStrategy;
import org.safehaus.kiskis.mgmt.impl.mongodb.MongoImpl;
import org.safehaus.kiskis.mgmt.impl.mongodb.common.CommandType;
import org.safehaus.kiskis.mgmt.impl.mongodb.common.Commands;
import org.safehaus.kiskis.mgmt.shared.operation.AbstractOperationHandler;
import org.safehaus.kiskis.mgmt.shared.operation.ProductOperation;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

import com.google.common.base.Strings;


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

        if ( Strings.isNullOrEmpty( config.getClusterName() ) || Strings.isNullOrEmpty( config.getReplicaSetName() )
                || Strings.isNullOrEmpty( config.getDomainName() ) || config.getNumberOfConfigServers() <= 0
                || config.getNumberOfRouters() <= 0 || config.getNumberOfDataNodes() <= 0 || config.getCfgSrvPort() <= 0
                || config.getDataNodePort() <= 0 || config.getRouterPort() <= 0 ) {
            po.addLogFailed( "Malformed configuration\nInstallation aborted" );
            return;
        }

        //check if mongo cluster with the same name already exists
        if ( manager.getCluster( config.getClusterName() ) != null ) {
            po.addLogFailed( String.format( "Cluster with name '%s' already exists\nInstallation aborted",
                    config.getClusterName() ) );
            return;
        }

        try {
            int numberOfLxcsNeeded =
                    config.getNumberOfConfigServers() + config.getNumberOfRouters() + config.getNumberOfDataNodes();
            //clone lxc containers
            po.addLog( String.format( "Creating %d lxc containers...", numberOfLxcsNeeded ) );
            Map<NodeType, Set<Agent>> nodes = CustomPlacementStrategy
                    .getNodes( manager.getLxcManager(), config.getNumberOfConfigServers(), config.getNumberOfRouters(),
                            config.getNumberOfDataNodes() );

            config.setConfigServers( nodes.get( NodeType.CONFIG_NODE ) );
            config.setDataNodes( nodes.get( NodeType.DATA_NODE ) );
            config.setRouterServers( nodes.get( NodeType.ROUTER_NODE ) );
            po.addLog( "Lxc containers created successfully\nUpdating db..." );

            if ( manager.getDbManager().saveInfo( Config.PRODUCT_KEY, config.getClusterName(), config ) ) {
                po.addLog( "Cluster info saved to DB\nInstalling Mongo..." );
                installMongoCluster( config, po );
            }
            else {
                //destroy all lxcs also
                try {
                    manager.getLxcManager().destroyLxcs( config.getAllNodes() );
                }
                catch ( LxcDestroyException ex ) {
                    po.addLogFailed( "Could not save cluster info to DB! Please see logs. Use LXC module to "
                                    + "cleanup\nInstallation aborted"
                                   );
                }
                po.addLogFailed( "Could not save cluster info to DB! Please see logs\nInstallation aborted" );
            }
        }
        catch ( LxcCreateException ex ) {
            po.addLogFailed( ex.getMessage() );
        }
    }


    private void installMongoCluster( final Config config, final ProductOperation po ) {

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

        if ( installationOK ) {
            po.addLogDone( "Installation succeeded" );
        }
        else {
            po.addLogFailed( "Installation failed" );
        }
    }
}
