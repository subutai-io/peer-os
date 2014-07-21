package org.safehaus.subutai.impl.mongodb;


import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandCallback;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.container.ContainerManager;
import org.safehaus.subutai.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.api.manager.helper.PlacementStrategyENUM;
import org.safehaus.subutai.impl.mongodb.common.CommandType;
import org.safehaus.subutai.impl.mongodb.common.Commands;
import org.safehaus.subutai.plugin.mongodb.api.Mongo;
import org.safehaus.subutai.plugin.mongodb.api.MongoClusterConfig;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.ClusterSetupException;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.shared.protocol.Response;


/**
 * This is a mongodb cluster setup strategy.
 */
public class MongoDbSetupStrategy implements ClusterSetupStrategy {

    private CommandRunner commandRunner;
    private Mongo mongoManager;
    private ContainerManager containerManager;
    private AgentManager agentManager;
    private ProductOperation po;
    private MongoClusterConfig config;
    private static final String templateName = "mongodb";


    /*@todo add parameter validation logic*/
    public MongoDbSetupStrategy( ProductOperation po, AgentManager agentManager, Mongo mongoManager,
                                 CommandRunner commandRunner, ContainerManager containerManager,
                                 MongoClusterConfig config ) {

        this.mongoManager = mongoManager;
        this.containerManager = containerManager;
        this.agentManager = agentManager;
        this.commandRunner = commandRunner;
        this.po = po;
        this.config = config;

        //        config.setCfgSrvPort( cfgSrvPort );
        //        config.setRouterPort( routerPort );
        //        config.setDataNodePort( dataNodePort );
        //        config.setClusterName( clusterName );
        //        config.setReplicaSetName( replicaSetName );
        //        config.setDomainName( domainName );
        //        config.setNumberOfDataNodes( numberOfDataNodes );
        //        config.setNumberOfConfigServers( numberOfConfigServers );
        //        config.setNumberOfRouters( numberOfRouters );
    }


    @Override
    public MongoClusterConfig setup() throws ClusterSetupException {


        //check if mongo cluster with the same name already exists
        if ( mongoManager.getCluster( config.getClusterName() ) != null ) {
            throw new ClusterSetupException(
                    String.format( "Cluster with name '%s' already exists\nInstallation aborted",
                            config.getClusterName() ) );
        }

        try {

            po.addLog( String.format( "Creating %d config servers...", config.getNumberOfConfigServers() ) );
            Set<Agent> cfgServers = containerManager
                    .clone( templateName, config.getNumberOfConfigServers(), agentManager.getPhysicalAgents(),
                            PlacementStrategyENUM.MORE_CPU );

            po.addLog( String.format( "Creating %d routers...", config.getNumberOfRouters() ) );
            Set<Agent> routers = containerManager
                    .clone( templateName, config.getNumberOfRouters(), agentManager.getPhysicalAgents(),
                            PlacementStrategyENUM.MORE_RAM );

            po.addLog( String.format( "Creating %d data nodes...", config.getNumberOfDataNodes() ) );
            Set<Agent> dataNodes = containerManager
                    .clone( templateName, config.getNumberOfDataNodes(), agentManager.getPhysicalAgents(),
                            PlacementStrategyENUM.MORE_HDD );


            config.setConfigServers( cfgServers );
            config.setRouterServers( routers );
            config.setDataNodes( dataNodes );

            po.addLog( "Lxc containers created successfully" );

            //continue installation here

            installMongoCluster();
        }
        catch ( LxcCreateException ex ) {
            throw new ClusterSetupException( ex.getMessage() );
        }


        return config;
    }


    private void installMongoCluster() {

        List<Command> installationCommands = Commands.getInstallationCommands( config );

        boolean installationOK = true;

        for ( Command command : installationCommands ) {
            po.addLog( String.format( "Running command: %s", command.getDescription() ) );
            final AtomicBoolean commandOK = new AtomicBoolean();

            if ( command.getData() == CommandType.START_CONFIG_SERVERS || command.getData() == CommandType.START_ROUTERS
                    || command.getData() == CommandType.START_DATA_NODES ) {
                commandRunner.runCommand( command, new CommandCallback() {

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
                commandRunner.runCommand( command );
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
