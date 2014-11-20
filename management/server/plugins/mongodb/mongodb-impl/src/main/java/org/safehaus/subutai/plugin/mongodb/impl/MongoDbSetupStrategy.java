package org.safehaus.subutai.plugin.mongodb.impl;


import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.safehaus.subutai.common.exception.ClusterConfigurationException;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.Criteria;
import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.plugin.mongodb.api.MongoClusterConfig;
import org.safehaus.subutai.plugin.mongodb.api.MongoConfigNode;
import org.safehaus.subutai.plugin.mongodb.api.MongoDataNode;
import org.safehaus.subutai.plugin.mongodb.api.MongoException;
import org.safehaus.subutai.plugin.mongodb.api.MongoRouterNode;
import org.safehaus.subutai.plugin.mongodb.api.NodeType;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;


/**
 * This is a mongodb cluster setup strategy.
 */
public class MongoDbSetupStrategy implements ClusterSetupStrategy
{

    private MongoImpl mongoManager;
    private TrackerOperation po;
    private MongoClusterConfigImpl config;
    private Environment environment;


    public MongoDbSetupStrategy( Environment environment, MongoClusterConfig config, TrackerOperation po,
                                 MongoImpl mongoManager )
    {

        Preconditions.checkNotNull( environment, "Environment is null" );
        Preconditions.checkNotNull( config, "Cluster config is null" );
        Preconditions.checkNotNull( po, "Product operation tracker is null" );
        Preconditions.checkNotNull( mongoManager, "Mongo manager is null" );

        this.environment = environment;
        this.mongoManager = mongoManager;
        this.po = po;
        this.config = ( MongoClusterConfigImpl ) config;
    }


    public static PlacementStrategy getNodePlacementStrategyByNodeType( NodeType nodeType )
    {
        switch ( nodeType )
        {
            case CONFIG_NODE:
                return new PlacementStrategy( "BEST_SERVER", Sets.newHashSet( new Criteria( "MORE_RAM", true ) ) );

            //return PlacementStrategy.MORE_RAM;
            case ROUTER_NODE:
                return new PlacementStrategy( "BEST_SERVER", Sets.newHashSet( new Criteria( "MORE_CPU", true ) ) );

            //                return PlacementStrategy.MORE_CPU;
            case DATA_NODE:
                return new PlacementStrategy( "BEST_SERVER", Sets.newHashSet( new Criteria( "MORE_HDD", true ) ) );

            //                return PlacementStrategy.MORE_HDD;
            default:
                return new PlacementStrategy( "ROUND_ROBIN" );

            //                return PlacementStrategy.ROUND_ROBIN;
        }
    }


    @Override
    public MongoClusterConfig setup() throws ClusterSetupException
    {

        if ( Strings.isNullOrEmpty( config.getClusterName() ) ||
                Strings.isNullOrEmpty( config.getDomainName() ) ||
                Strings.isNullOrEmpty( config.getReplicaSetName() ) ||
                Strings.isNullOrEmpty( config.getTemplateName() ) ||
                !Sets.newHashSet( 1, 3 ).contains( config.getNumberOfConfigServers() ) ||
                !Range.closed( 1, 3 ).contains( config.getNumberOfRouters() ) ||
                !Sets.newHashSet( 3, 5, 7 ).contains( config.getNumberOfDataNodes() ) ||
                !Range.closed( 1024, 65535 ).contains( config.getCfgSrvPort() ) ||
                !Range.closed( 1024, 65535 ).contains( config.getRouterPort() ) ||
                !Range.closed( 1024, 65535 ).contains( config.getDataNodePort() ) )
        {
            throw new ClusterSetupException( "Malformed cluster configuration" );
        }

        if ( mongoManager.getCluster( config.getClusterName() ) != null )
        {
            throw new ClusterSetupException(
                    String.format( "Cluster with name '%s' already exists", config.getClusterName() ) );
        }

        if ( environment.getContainers().isEmpty() )
        {
            throw new ClusterSetupException( "Environment has no nodes" );
        }

        int totalNodesRequired =
                config.getNumberOfRouters() + config.getNumberOfConfigServers() + config.getNumberOfDataNodes();
        if ( environment.getContainers().size() < totalNodesRequired )
        {
            throw new ClusterSetupException(
                    String.format( "Environment needs to have %d but has %d nodes", totalNodesRequired,
                            environment.getContainers().size() ) );
        }

        Set<ContainerHost> mongoContainers = new HashSet<>();
        Set<ContainerHost> mongoEnvironmentContainers = new HashSet<>();
        for ( ContainerHost container : environment.getContainers() )
        {
            try
            {
                Template t = container.getTemplate();
                if ( t.getProducts().contains( Common.PACKAGE_PREFIX + MongoClusterConfig.PRODUCT_NAME ) )
                {
                    mongoContainers.add( container );
                    mongoEnvironmentContainers.add( container );
                }
            }
            catch ( PeerException e )
            {
                throw new ClusterSetupException( e.toString() );
            }
        }

        if ( mongoContainers.size() < totalNodesRequired )
        {
            throw new ClusterSetupException( String.format(
                    "Environment needs to have %d with MongoDb installed but has only %d nodes with MongoDb installed",
                    totalNodesRequired, mongoContainers.size() ) );
        }

        Set<MongoConfigNode> configServers = new HashSet<>();
        Set<MongoRouterNode> routers = new HashSet<>();
        Set<MongoDataNode> dataNodes = new HashSet<>();
        for ( ContainerHost environmentContainer : mongoEnvironmentContainers )
        {
            if ( NodeType.CONFIG_NODE.name().equalsIgnoreCase( environmentContainer.getNodeGroupName() ) )
            {
                MongoConfigNode mongoConfigNode =
                        new MongoConfigNodeImpl( environmentContainer.getAgent(), environmentContainer.getPeerId(),
                                environmentContainer.getEnvironmentId(), config.getDomainName(),
                                config.getCfgSrvPort() );
                configServers.add( mongoConfigNode );
            }
            else if ( NodeType.ROUTER_NODE.name().equalsIgnoreCase( environmentContainer.getNodeGroupName() ) )
            {
                MongoRouterNode mongoRouterNode =
                        new MongoRouterNodeImpl( environmentContainer.getAgent(), environmentContainer.getPeerId(),
                                environmentContainer.getEnvironmentId(), config.getDomainName(), config.getRouterPort(),
                                config.getCfgSrvPort() );
                routers.add( mongoRouterNode );
            }
            else if ( NodeType.DATA_NODE.name().equalsIgnoreCase( environmentContainer.getNodeGroupName() ) )
            {
                MongoDataNode mongoDataNode =
                        new MongoDataNodeImpl( environmentContainer.getAgent(), environmentContainer.getPeerId(),
                                environmentContainer.getEnvironmentId(), config.getDomainName(),
                                config.getDataNodePort() );
                dataNodes.add( mongoDataNode );
            }
        }

        mongoContainers.removeAll( configServers );
        mongoContainers.removeAll( routers );
        mongoContainers.removeAll( dataNodes );

        if ( configServers.size() < config.getNumberOfConfigServers() )
        {
            //take necessary number of nodes at random
            int numNeededMore = config.getNumberOfConfigServers() - configServers.size();
            Iterator<ContainerHost> it = mongoContainers.iterator();
            for ( int i = 0; i < numNeededMore; i++ )
            {
                ContainerHost environmentContainer = it.next();
                MongoConfigNode mongoConfigNode =
                        new MongoConfigNodeImpl( environmentContainer.getAgent(), environmentContainer.getPeerId(),
                                environmentContainer.getEnvironmentId(), config.getDomainName(),
                                config.getCfgSrvPort() );
                configServers.add( mongoConfigNode );
                it.remove();
            }
        }

        if ( routers.size() < config.getNumberOfRouters() )
        {
            //take necessary number of nodes at random
            int numNeededMore = config.getNumberOfRouters() - routers.size();
            Iterator<ContainerHost> it = mongoContainers.iterator();
            for ( int i = 0; i < numNeededMore; i++ )
            {
                ContainerHost environmentContainer = it.next();
                MongoRouterNode mongoRouterNode =
                        new MongoRouterNodeImpl( environmentContainer.getAgent(), environmentContainer.getPeerId(),
                                environmentContainer.getEnvironmentId(), config.getDomainName(), config.getRouterPort(),
                                config.getCfgSrvPort() );
                routers.add( mongoRouterNode );
                it.remove();
            }
        }

        if ( dataNodes.size() < config.getNumberOfDataNodes() )
        {
            //take necessary number of nodes at random
            int numNeededMore = config.getNumberOfDataNodes() - dataNodes.size();
            Iterator<ContainerHost> it = mongoContainers.iterator();
            for ( int i = 0; i < numNeededMore; i++ )
            {
                ContainerHost environmentContainer = it.next();
                MongoDataNode mongoDataNode =
                        new MongoDataNodeImpl( environmentContainer.getAgent(), environmentContainer.getPeerId(),
                                environmentContainer.getEnvironmentId(), config.getDomainName(),
                                config.getRouterPort() );
                dataNodes.add( mongoDataNode );
                it.remove();
            }
        }

        config.setConfigServers( configServers );
        config.setRouterServers( routers );
        config.setDataNodes( dataNodes );


        try
        {
            configureMongoCluster();
        }
        catch ( ClusterConfigurationException e )
        {
            throw new ClusterSetupException( e.getMessage() );
        }

        po.addLog( "Saving cluster information to database..." );

        mongoManager.getPluginDAO().saveInfo( MongoClusterConfig.PRODUCT_KEY, config.getClusterName(), config );
        po.addLog( "Cluster information saved to database" );


        return config;
    }


    private void configureMongoCluster() throws ClusterConfigurationException
    {

        po.addLog( "Configuring cluster..." );
        try
        {
            for ( MongoDataNode dataNode : config.getDataNodes() )
            {
                po.addLog( "Setting replicaSetname: " + dataNode.getHostname() );
                dataNode.setReplicaSetName( config.getReplicaSetName() );
            }

            for ( MongoConfigNode configNode : config.getConfigServers() )
            {
                po.addLog( "Starting config node: " + configNode.getHostname() );
                configNode.start();
            }

            for ( MongoRouterNode routerNode : config.getRouterServers() )
            {
                po.addLog( "Starting router node: " + routerNode.getHostname() );
                routerNode.setConfigServers( config.getConfigServers() );
                routerNode.start();
            }

            for ( MongoDataNode dataNode : config.getDataNodes() )
            {
                po.addLog( "Stopping data node: " + dataNode.getHostname() );
                dataNode.stop();
            }

            MongoDataNode primaryDataNode = null;
            for ( MongoDataNode dataNode : config.getDataNodes() )
            {
                po.addLog( "Starting data node: " + dataNode.getHostname() );
                dataNode.start();
                if ( primaryDataNode == null )
                {
                    primaryDataNode = dataNode;
                    primaryDataNode.initiateReplicaSet();
                    po.addLog( "Primary data node: " + dataNode.getHostname() );
                }
                else
                {
                    po.addLog( "registering secondary data node: " + dataNode.getHostname() );
                    primaryDataNode.registerSecondaryNode( dataNode );
                }
            }
        }
        catch ( MongoException e )
        {
            e.printStackTrace();
            throw new ClusterConfigurationException( e );
        }
        po.addLogDone( String.format( "Cluster %s configured successfully.", config.getClusterName() ) );

        //                List<Command> installationCommands = mongoManager.getCommands().getInstallationCommands(
        // config );
        //
        //        for ( Command command : installationCommands )
        //        {
        //            po.addLog( String.format( "Running command: %s", command.getDescription() ) );
        //            final AtomicBoolean commandOK = new AtomicBoolean();
        //
        //            if ( command.getData() == CommandType.START_CONFIG_SERVERS || command.getData() == CommandType
        // .START_ROUTERS
        //                    || command.getData() == CommandType.START_DATA_NODES )
        //            {
        //                mongoManager.getCommandRunner().runCommand( command, new CommandCallback()
        //                {
        //
        //                    @Override
        //                    public void onResponse( Response response, AgentResult agentResult, Command command )
        //                    {
        //
        //                        int count = 0;
        //                        for ( AgentResult result : command.getResults().values() )
        //                        {
        //                            if ( result.getStdOut().contains( "child process started successfully,
        // parent exiting" ) )
        //                            {
        //                                count++;
        //                            }
        //                        }
        //                        if ( command.getData() == CommandType.START_CONFIG_SERVERS )
        //                        {
        //                            if ( count == config.getConfigServers().size() )
        //                            {
        //                                commandOK.set( true );
        //                            }
        //                        }
        //                        else if ( command.getData() == CommandType.START_ROUTERS )
        //                        {
        //                            if ( count == config.getRouterServers().size() )
        //                            {
        //                                commandOK.set( true );
        //                            }
        //                        }
        //                        else if ( command.getData() == CommandType.START_DATA_NODES )
        //                        {
        //                            if ( count == config.getDataNodes().size() )
        //                            {
        //                                commandOK.set( true );
        //                            }
        //                        }
        //                        if ( commandOK.get() )
        //                        {
        //                            stop();
        //                        }
        //                    }
        //                } );
        //            }
        //            else
        //            {
        //                mongoManager.getCommandRunner().runCommand( command );
        //            }
        //
        //            if ( command.hasSucceeded() || commandOK.get() )
        //            {
        //                po.addLog( String.format( "Command %s succeeded", command.getDescription() ) );
        //            }
        //            else
        //            {
        //                throw new ClusterConfigurationException(
        //                        String.format( "Command %s failed: %s", command.getDescription(),
        // command.getAllErrors() ) );
        //            }
        //        }
    }
}
