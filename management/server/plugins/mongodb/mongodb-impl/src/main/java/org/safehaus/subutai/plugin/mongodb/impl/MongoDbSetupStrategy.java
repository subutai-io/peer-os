package org.safehaus.subutai.plugin.mongodb.impl;


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandCallback;
import org.safehaus.subutai.api.manager.exception.EnvironmentBuildException;
import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.api.manager.helper.EnvironmentBlueprint;
import org.safehaus.subutai.api.manager.helper.Node;
import org.safehaus.subutai.api.manager.helper.NodeGroup;
import org.safehaus.subutai.api.manager.helper.PlacementStrategy;
import org.safehaus.subutai.plugin.mongodb.api.MongoClusterConfig;
import org.safehaus.subutai.plugin.mongodb.api.NodeType;
import org.safehaus.subutai.plugin.mongodb.impl.common.CommandType;
import org.safehaus.subutai.plugin.mongodb.impl.common.Commands;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.ClusterSetupException;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.shared.protocol.Response;

import com.google.common.base.Preconditions;


/**
 * This is a mongodb cluster setup strategy.
 */
public class MongoDbSetupStrategy implements ClusterSetupStrategy {

    private MongoImpl mongoManager;
    private ProductOperation po;
    private MongoClusterConfig config;
    public static final String TEMPLATE_NAME = "mongodb";


    public MongoDbSetupStrategy( MongoClusterConfig config, ProductOperation po, MongoImpl mongoManager ) {

        Preconditions.checkNotNull( config, "Cluster config is null" );
        Preconditions.checkNotNull( po, "Product operation tracker is null" );
        Preconditions.checkNotNull( mongoManager, "Mongo manager is null" );

        this.mongoManager = mongoManager;
        this.po = po;
        this.config = config;
    }


    public static PlacementStrategy getNodePlacementStrategyByNodeType( NodeType nodeType ) {
        switch ( nodeType ) {
            case CONFIG_NODE:
                return PlacementStrategy.MORE_RAM;
            case ROUTER_NODE:
                return PlacementStrategy.MORE_CPU;
            case DATA_NODE:
                return PlacementStrategy.MORE_HDD;
            default:
                return PlacementStrategy.ROUND_ROBIN;
        }
    }


    private EnvironmentBlueprint getDefaultEnvironmentBlueprint() {
        EnvironmentBlueprint environmentBlueprint = new EnvironmentBlueprint();
        environmentBlueprint.setName( String.format( "%s-%s", MongoClusterConfig.PRODUCT_KEY, UUID.randomUUID() ) );
        environmentBlueprint.setLinkHosts( true );
        //config servers
        NodeGroup cfgServersGroup = new NodeGroup();
        cfgServersGroup.setName( NodeType.CONFIG_NODE.name() );
        cfgServersGroup.setNumberOfNodes( config.getNumberOfConfigServers() );
        cfgServersGroup.setTemplateName( TEMPLATE_NAME );
        cfgServersGroup.setPlacementStrategy( getNodePlacementStrategyByNodeType( NodeType.CONFIG_NODE ) );

        //routers
        NodeGroup routersGroup = new NodeGroup();
        routersGroup.setName( NodeType.ROUTER_NODE.name() );
        routersGroup.setNumberOfNodes( config.getNumberOfRouters() );
        routersGroup.setTemplateName( TEMPLATE_NAME );
        routersGroup.setPlacementStrategy( getNodePlacementStrategyByNodeType( NodeType.ROUTER_NODE ) );

        //data nodes
        NodeGroup dataNodesGroup = new NodeGroup();
        dataNodesGroup.setName( NodeType.DATA_NODE.name() );
        dataNodesGroup.setNumberOfNodes( config.getNumberOfDataNodes() );
        dataNodesGroup.setTemplateName( TEMPLATE_NAME );
        dataNodesGroup.setPlacementStrategy( getNodePlacementStrategyByNodeType( NodeType.DATA_NODE ) );

        return environmentBlueprint;
    }


    @Override
    public MongoClusterConfig setup() throws ClusterSetupException {


        //check if mongo cluster with the same name already exists
        if ( mongoManager.getCluster( config.getClusterName() ) != null ) {
            throw new ClusterSetupException(
                    String.format( "Cluster with name '%s' already exists\nInstallation aborted",
                            config.getClusterName() ) );
        }
        //if no nodes are set, setup default environment
        if ( config.getAllNodes() == null || config.getAllNodes().isEmpty() ) {
            try {
                Environment env = mongoManager.getEnvironmentManager()
                                              .buildEnvironmentAndReturn( getDefaultEnvironmentBlueprint() );

                Set<Agent> configServers = new HashSet<>();
                Set<Agent> routers = new HashSet<>();
                Set<Agent> dataNodes = new HashSet<>();
                for ( Node node : env.getNodes() ) {
                    if ( NodeType.CONFIG_NODE.name().equalsIgnoreCase( node.getNodeGroupName() ) ) {
                        configServers.add( node.getAgent() );
                    }
                    else if ( NodeType.ROUTER_NODE.name().equalsIgnoreCase( node.getNodeGroupName() ) ) {
                        routers.add( node.getAgent() );
                    }
                    else if ( NodeType.DATA_NODE.name().equalsIgnoreCase( node.getNodeGroupName() ) ) {
                        dataNodes.add( node.getAgent() );
                    }
                }

                if ( configServers.isEmpty() ) {
                    throw new ClusterSetupException( "Config servers are not created" );
                }
                if ( routers.isEmpty() ) {
                    throw new ClusterSetupException( "Routers are not created" );
                }
                if ( dataNodes.isEmpty() ) {
                    throw new ClusterSetupException( "Data nodes are not created" );
                }
                config.setConfigServers( configServers );
                config.setRouterServers( routers );
                config.setDataNodes( dataNodes );
            }
            catch ( EnvironmentBuildException e ) {
                throw new ClusterSetupException( String.format( "Error building environment: %s", e.getMessage() ) );
            }
        }
        else {

            //check if nodes are set
            if ( config.getConfigServers() == null || config.getConfigServers().isEmpty() ) {
                throw new ClusterSetupException( "No config servers are set" );
            }
            if ( config.getDataNodes() == null || config.getDataNodes().isEmpty() ) {
                throw new ClusterSetupException( "No data nodes are set" );
            }
            if ( config.getRouterServers() == null || config.getRouterServers().isEmpty() ) {
                throw new ClusterSetupException( "No routers are set" );
            }
        }

        installMongoCluster();

        return config;
    }


    private void installMongoCluster() throws ClusterSetupException {

        List<Command> installationCommands = Commands.getInstallationCommands( config );

        for ( Command command : installationCommands ) {
            po.addLog( String.format( "Running command: %s", command.getDescription() ) );
            final AtomicBoolean commandOK = new AtomicBoolean();

            if ( command.getData() == CommandType.START_CONFIG_SERVERS || command.getData() == CommandType.START_ROUTERS
                    || command.getData() == CommandType.START_DATA_NODES ) {
                mongoManager.getCommandRunner().runCommand( command, new CommandCallback() {

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
                mongoManager.getCommandRunner().runCommand( command );
            }

            if ( command.hasSucceeded() || commandOK.get() ) {
                po.addLog( String.format( "Command %s succeeded", command.getDescription() ) );
            }
            else {
                throw new ClusterSetupException(
                        String.format( "Command %s failed: %s", command.getDescription(), command.getAllErrors() ) );
            }
        }
    }
}
