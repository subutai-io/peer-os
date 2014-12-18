package org.safehaus.subutai.plugin.mongodb.impl.handler;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.exception.SubutaiException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentManagerException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.Host;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.plugin.mongodb.api.MongoClusterConfig;
import org.safehaus.subutai.plugin.mongodb.api.MongoDataNode;
import org.safehaus.subutai.plugin.mongodb.api.MongoNode;
import org.safehaus.subutai.plugin.mongodb.api.MongoRouterNode;
import org.safehaus.subutai.plugin.mongodb.api.NodeType;
import org.safehaus.subutai.plugin.mongodb.impl.MongoConfigNodeImpl;
import org.safehaus.subutai.plugin.mongodb.impl.MongoDataNodeImpl;
import org.safehaus.subutai.plugin.mongodb.impl.MongoDbSetupStrategy;
import org.safehaus.subutai.plugin.mongodb.impl.MongoImpl;
import org.safehaus.subutai.plugin.mongodb.impl.MongoRouterNodeImpl;


/**
 * Handles add mongo node operation
 */
public class AddNodeOperationHandler extends AbstractOperationHandler<MongoImpl, MongoClusterConfig>
{
    private final TrackerOperation po;
    private final NodeType nodeType;


    public AddNodeOperationHandler( MongoImpl manager, String clusterName, NodeType nodeType )
    {
        super( manager, clusterName );
        this.nodeType = nodeType;
        po = manager.getTracker().createTrackerOperation( MongoClusterConfig.PRODUCT_KEY,
                String.format( "Adding %s to %s...", nodeType, clusterName ) );
    }


    @Override
    public UUID getTrackerId()
    {
        return po.getId();
    }


    @Override
    public void run()
    {
        MongoClusterConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            po.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }
        if ( nodeType == NodeType.CONFIG_NODE )
        {
            po.addLogFailed( "Can not add config server" );
            return;
        }
        if ( nodeType == NodeType.DATA_NODE && config.getDataNodes().size() == 7 )
        {
            po.addLogFailed( "Replica set cannot have more than 7 members" );
            return;
        }

        po.addLog( "Creating lxc container..." );

        LocalPeer localPeer = manager.getPeerManager().getLocalPeer();
        MongoNode mongoNode = null;

        try
        {
            List<Template> templates = new ArrayList();
            Template template = localPeer.getTemplate( config.getTemplateName() );
            templates.add( template );
            while ( !"master".equals( template.getTemplateName() ) )
            {
                template = localPeer.getTemplate( template.getParentTemplateName() );
                templates.add( 0, template );
            }
            UUID hostId = manager.getEnvironmentManager()
                                 .addContainer( config.getEnvironmentId(), config.getTemplateName(),
                                         MongoDbSetupStrategy.getNodePlacementStrategyByNodeType( nodeType ),
                                         nodeType.name(), localPeer );
            Environment environment = manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() );
            if ( environment == null )
            {
                throw new PeerException( "Could not obtain cluster environment" );
            }

            ContainerHost containerHost = environment.getContainerHostById( hostId );
            switch ( nodeType )
            {
                case CONFIG_NODE:
                    mongoNode =
                            new MongoConfigNodeImpl( containerHost, config.getDomainName(), config.getCfgSrvPort() );
                    break;
                case ROUTER_NODE:
                    mongoNode = new MongoRouterNodeImpl( containerHost, config.getDomainName(), config.getRouterPort(),
                            config.getCfgSrvPort() );
                    break;
                case DATA_NODE:
                    mongoNode =
                            new MongoDataNodeImpl( containerHost, config.getDomainName(), config.getDataNodePort() );
                    break;
            }
            config.addNode( mongoNode, nodeType );
            po.addLog( "Lxc container created successfully\nConfiguring cluster..." );
        }
        catch ( EnvironmentManagerException | PeerException e )
        {
            po.addLogFailed( e.toString() );
            return;
        }

        boolean result = true;
        //add node
        if ( nodeType == NodeType.DATA_NODE )
        {
            result = addDataNode( po, config, ( MongoDataNode ) mongoNode );
        }
        else if ( nodeType == NodeType.ROUTER_NODE )
        {
            result = addRouter( po, config, ( MongoRouterNode ) mongoNode );
        }

        if ( result )
        {
            po.addLog( "Updating cluster information in database..." );

            String json = manager.getGSON().toJson( config.prepare() );
            manager.getPluginDAO().saveInfo( MongoClusterConfig.PRODUCT_KEY, config.getClusterName(), json );
            po.addLogDone( "Cluster information updated in database" );
        }
        else
        {
            po.addLogFailed( "Node addition failed" );
        }
    }


    private boolean addDataNode( TrackerOperation po, final MongoClusterConfig config, MongoDataNode newDataNode )
    {

        Set<Host> clusterMembers = new HashSet<Host>();
        for ( MongoNode mongoNode : config.getAllNodes() )
        {
            clusterMembers.add( mongoNode.getContainerHost() );
        }
        clusterMembers.add( newDataNode.getContainerHost() );
        CommandResult commandResult = null;
        try
        {
            for ( Host c : clusterMembers )
            {
                c.addIpHostToEtcHosts( config.getDomainName(), clusterMembers, Common.IP_MASK );
            }

            newDataNode.setReplicaSetName( config.getReplicaSetName() );
            po.addLog( String.format( "Set replica set name succeeded" ) );
            po.addLog( String.format( "Stopping node..." ) );
            newDataNode.stop();
            po.addLog( String.format( "Starting node..." ) );
            newDataNode.start();

            po.addLog( String.format( "Data node started successfully" ) );

            MongoDataNode primaryNode = config.findPrimaryNode();

            if ( primaryNode != null )
            {

                primaryNode.registerSecondaryNode( newDataNode );

                po.addLog( String.format( "Secondary node registered successfully." ) );
                return true;
            }
        }
        catch ( SubutaiException e )
        {
            po.addLog( String.format( "Error: %s", e.toString() ) );
        }
        return false;

        //        List<Command> commands = manager.getCommands().getAddDataNodeCommands( config, agent );
        //
        //        boolean additionOK = true;
        //        Command findPrimaryNodeCommand = null;
        //
        //        for ( Command command : commands )
        //        {
        //            po.addLog( String.format( "Running command: %s", command.getDescription() ) );
        //            final AtomicBoolean commandOK = new AtomicBoolean();
        //
        //            if ( command.getData() == CommandType.FIND_PRIMARY_NODE )
        //            {
        //                findPrimaryNodeCommand = command;
        //            }
        //
        //            if ( command.getData() == CommandType.START_DATA_NODES )
        //            {
        //                manager.getCommandRunner().runCommand( command, new CommandCallback()
        //                {
        //
        //                    @Override
        //                    public void onResponse( Response response, AgentResult agentResult, Command command )
        //                    {
        //                        if ( agentResult.getStdOut().contains( "child process started successfully,
        // parent exiting" ) )
        //                        {
        //                            commandOK.set( true );
        //                            stop();
        //                        }
        //                    }
        //                } );
        //            }
        //            else
        //            {
        //                manager.getCommandRunner().runCommand( command );
        //            }
        //
        //            if ( command.hasSucceeded() || commandOK.get() )
        //            {
        //                po.addLog( String.format( "Command %s succeeded", command.getDescription() ) );
        //            }
        //            else
        //            {
        //                po.addLog( String.format( "Command %s failed: %s", command.getDescription(),
        // command.getAllErrors() ) );
        //                additionOK = false;
        //                break;
        //            }
        //        }

        //        //parse result of findPrimaryNodeCommand
        //        if ( additionOK )
        //        {
        //            if ( findPrimaryNodeCommand != null && !findPrimaryNodeCommand.getResults().isEmpty() )
        //            {
        //                ContainerHost primaryNodeAgent = null;
        //                Pattern p = Pattern.compile( "primary\" : \"(.*)\"" );
        //                AgentResult result = findPrimaryNodeCommand.getResults().entrySet().iterator().next()
        // .getValue();
        //                Matcher m = p.matcher( result.getStdOut() );
        //                if ( m.find() )
        //                {
        //                    String primaryNodeHost = m.group( 1 );
        //                    if ( !Strings.isNullOrEmpty( primaryNodeHost ) )
        //                    {
        //                        String hostname = primaryNodeHost.split( ":" )[0].replace( "." + config
        // .getDomainName(), "" );
        //                        primaryNodeAgent = findNodeInCluster( hostname );
        //                    }
        //                }
        //
        //                if ( primaryNodeAgent != null )
        //                {
        //                    CommandDef registerSecondaryNodeCommandDef = manager.getCommands()
        //
        // .getRegisterSecondaryNodeWithPrimaryCommand(
        //                                                                                agent.getHostname(),
        //                                                                                config.getDataNodePort(),
        //                                                                                config.getDomainName() );
        //                    try
        //                    {
        //                        primaryNodeAgent.execute( new RequestBuilder( registerSecondaryNodeCommandDef
        // .getCommand() ) );
        //                        po.addLog( String.format( "Command %s succeeded",
        //                                registerSecondaryNodeCommandDef.getDescription() ) );
        //                        return true;
        //                    }
        //                    catch ( CommandException e )
        //                    {
        //                        po.addLog( String.format( "Command %s failed: %s",
        //                                registerSecondaryNodeCommandDef.getDescription(), e.toString() ) );
        //                    }
        //                }
        //                else
        //                {
        //                    po.addLog( "Could not find primary node" );
        //                }
        //            }
        //            else
        //            {
        //                po.addLog( "Could not find primary node" );
        //            }
        //        }
        //
        //        return false;
    }


    private boolean addRouter( TrackerOperation po, final MongoClusterConfig config, MongoRouterNode newRouter )
    {

        Set<Host> clusterMembers = new HashSet<Host>();
        for ( MongoNode mongoNode : config.getAllNodes() )
        {
            clusterMembers.add( mongoNode.getContainerHost() );
        }
        clusterMembers.add( newRouter.getContainerHost() );
        try
        {
            for ( Host c : clusterMembers )
            {
                c.addIpHostToEtcHosts( config.getDomainName(), clusterMembers, Common.IP_MASK );
            }

            po.addLog( String.format( "Starting router: %s", newRouter.getHostname() ) );
            newRouter.setConfigServers( config.getConfigServers() );
            newRouter.start();
            return true;
            //            CommandDef commandDef = manager.getCommands()
            //                                           .getStartRouterCommand( config.getRouterPort(),
            // config.getCfgSrvPort(),
            //                                                   config.getDomainName(), config.getConfigServers() );

            //            CommandResult commandResult = newRouter
            //                    .execute( new RequestBuilder( commandDef.getCommand() ).withTimeout( commandDef
            // .getTimeout() ) );
            //            if ( commandResult.getStdOut().contains( "child process started successfully,
            // parent exiting" ) )
            //            {
            //                return true;
            //            }
        }
        catch ( SubutaiException e )
        {
            po.addLog( String.format( "Could not add router node: %s", e.toString() ) );
        }


        return false;
    }


    //    private boolean addRouterOld( TrackerOperation po, final MongoClusterConfig config, Agent agent )
    //    {
    //        List<Command> commands = manager.getCommands().getAddRouterCommandsOld( config, agent );
    //
    //        boolean additionOK = true;
    //
    //        for ( Command command : commands )
    //        {
    //            po.addLog( String.format( "Running command: %s", command.getDescription() ) );
    //            final AtomicBoolean commandOK = new AtomicBoolean();
    //
    //            if ( command.getData() == CommandType.START_ROUTERS )
    //            {
    //                manager.getCommandRunner().runCommand( command, new CommandCallback()
    //                {
    //
    //                    @Override
    //                    public void onResponse( Response response, AgentResult agentResult, Command command )
    //                    {
    //                        if ( agentResult.getStdOut().contains( "child process started successfully,
    // parent exiting" ) )
    //                        {
    //                            commandOK.set( true );
    //                            stop();
    //                        }
    //                    }
    //                } );
    //            }
    //            else
    //            {
    //                manager.getCommandRunner().runCommand( command );
    //            }
    //
    //            if ( command.hasSucceeded() || commandOK.get() )
    //            {
    //                po.addLog( String.format( "Command %s succeeded", command.getDescription() ) );
    //            }
    //            else
    //            {
    //                po.addLog( String.format( "Command %s failed: %s", command.getDescription(),
    // command.getAllErrors() ) );
    //                additionOK = false;
    //                break;
    //            }
    //        }
    //
    //        return additionOK;
    //    }
}
