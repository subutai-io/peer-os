package org.safehaus.subutai.plugin.mongodb.impl;


import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandCallback;
import org.safehaus.subutai.api.dbmanager.DBException;
import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.api.manager.helper.Node;
import org.safehaus.subutai.plugin.mongodb.api.MongoClusterConfig;
import org.safehaus.subutai.plugin.mongodb.api.NodeType;
import org.safehaus.subutai.plugin.mongodb.impl.common.CommandType;
import org.safehaus.subutai.plugin.mongodb.impl.common.Commands;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.ClusterConfigurationException;
import org.safehaus.subutai.shared.protocol.ClusterSetupException;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.shared.protocol.PlacementStrategy;
import org.safehaus.subutai.shared.protocol.Response;
import org.safehaus.subutai.shared.protocol.settings.Common;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;


/**
 * This is a mongodb cluster setup strategy.
 */
public class MongoDbSetupStrategy implements ClusterSetupStrategy {

    private MongoImpl mongoManager;
    private ProductOperation po;
    private MongoClusterConfig config;
    private Environment environment;


    public MongoDbSetupStrategy( Environment environment, MongoClusterConfig config, ProductOperation po,
                                 MongoImpl mongoManager ) {

        Preconditions.checkNotNull( environment, "Environment is null" );
        Preconditions.checkNotNull( config, "Cluster config is null" );
        Preconditions.checkNotNull( po, "Product operation tracker is null" );
        Preconditions.checkNotNull( mongoManager, "Mongo manager is null" );

        this.environment = environment;
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


    @Override
    public MongoClusterConfig setup() throws ClusterSetupException {

        if ( Strings.isNullOrEmpty( config.getClusterName() ) ||
                Strings.isNullOrEmpty( config.getDomainName() ) ||
                Strings.isNullOrEmpty( config.getReplicaSetName() ) ||
                Strings.isNullOrEmpty( config.getTemplateName() ) ||
                !Sets.newHashSet( 1, 3 ).contains( config.getNumberOfConfigServers() ) ||
                !Range.closed( 1, 3 ).contains( config.getNumberOfRouters() ) ||
                !Sets.newHashSet( 3, 5, 7 ).contains( config.getNumberOfDataNodes() ) ||
                !Range.closed( 1024, 65535 ).contains( config.getCfgSrvPort() ) ||
                !Range.closed( 1024, 65535 ).contains( config.getRouterPort() ) ||
                !Range.closed( 1024, 65535 ).contains( config.getDataNodePort() ) ) {
            throw new ClusterSetupException( "Malformed cluster configuration" );
        }

        if ( mongoManager.getCluster( config.getClusterName() ) != null ) {
            throw new ClusterSetupException(
                    String.format( "Cluster with name '%s' already exists",
                            config.getClusterName() ) );
        }

        if ( environment.getNodes().isEmpty() ) {
            throw new ClusterSetupException( "Environment has no nodes" );
        }

        int totalNodesRequired =
                config.getNumberOfRouters() + config.getNumberOfConfigServers() + config.getNumberOfDataNodes();
        if ( environment.getNodes().size() < totalNodesRequired ) {
            throw new ClusterSetupException(
                    String.format( "Environment needs to have %d but has %d nodes", totalNodesRequired,
                            environment.getNodes().size() ) );
        }

        Set<Agent> mongoAgents = new HashSet<>();
        Set<Node> mongoNodes = new HashSet<>();
        for ( Node node : environment.getNodes() ) {
            if ( node.getTemplate().getProducts()
                     .contains( Common.PACKAGE_PREFIX + MongoClusterConfig.PRODUCT_NAME ) ) {
                mongoAgents.add( node.getAgent() );
                mongoNodes.add( node );
            }
        }

        if ( mongoAgents.size() < totalNodesRequired ) {
            throw new ClusterSetupException( String.format(
                    "Environment needs to have %d with MongoDb installed but has only %d nodes with MongoDb installed",
                    totalNodesRequired, mongoAgents.size() ) );
        }

        Set<Agent> configServers = new HashSet<>();
        Set<Agent> routers = new HashSet<>();
        Set<Agent> dataNodes = new HashSet<>();
        for ( Node node : mongoNodes ) {
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

        mongoAgents.removeAll( configServers );
        mongoAgents.removeAll( routers );
        mongoAgents.removeAll( dataNodes );

        if ( configServers.size() < config.getNumberOfConfigServers() ) {
            //take necessary number of nodes at random
            int numNeededMore = config.getNumberOfConfigServers() - configServers.size();
            Iterator<Agent> it = mongoAgents.iterator();
            for ( int i = 0; i < numNeededMore; i++ ) {
                configServers.add( it.next() );
                it.remove();
            }
        }

        if ( routers.size() < config.getNumberOfRouters() ) {
            //take necessary number of nodes at random
            int numNeededMore = config.getNumberOfRouters() - routers.size();
            Iterator<Agent> it = mongoAgents.iterator();
            for ( int i = 0; i < numNeededMore; i++ ) {
                routers.add( it.next() );
                it.remove();
            }
        }

        if ( dataNodes.size() < config.getNumberOfDataNodes() ) {
            //take necessary number of nodes at random
            int numNeededMore = config.getNumberOfDataNodes() - dataNodes.size();
            Iterator<Agent> it = mongoAgents.iterator();
            for ( int i = 0; i < numNeededMore; i++ ) {
                dataNodes.add( it.next() );
                it.remove();
            }
        }

        config.setConfigServers( configServers );
        config.setRouterServers( routers );
        config.setDataNodes( dataNodes );


        try {
            configureMongoCluster();
        }
        catch ( ClusterConfigurationException e ) {
            throw new ClusterSetupException( e.getMessage() );
        }

        po.addLog( "Saving cluster information to database..." );

        try {
            mongoManager.getDbManager().saveInfo2( MongoClusterConfig.PRODUCT_KEY, config.getClusterName(), config );
            po.addLog( "Cluster information saved to database" );
        }
        catch ( DBException e ) {
            throw new ClusterSetupException(
                    String.format( "Error saving cluster information to database, %s", e.getMessage() ) );
        }


        return config;
    }


    private void configureMongoCluster() throws ClusterConfigurationException {

        po.addLog( "Configuring cluster..." );

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
                throw new ClusterConfigurationException(
                        String.format( "Command %s failed: %s", command.getDescription(), command.getAllErrors() ) );
            }
        }
    }
}
