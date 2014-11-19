/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.mongodb.impl.common;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.util.AgentUtil;
import org.safehaus.subutai.core.command.api.command.AgentRequestBuilder;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandRunnerBase;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.Host;
import org.safehaus.subutai.plugin.mongodb.api.MongoClusterConfig;
import org.safehaus.subutai.plugin.mongodb.api.MongoConfigNode;
import org.safehaus.subutai.plugin.mongodb.api.MongoDataNode;
import org.safehaus.subutai.plugin.mongodb.api.MongoRouterNode;
import org.safehaus.subutai.plugin.mongodb.api.Timeouts;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;


/**
 * Holds all mongo related commands
 */
public class Commands
{

    private final CommandRunnerBase commandRunnerBase;


    public Commands( final CommandRunnerBase commandRunnerBase )
    {
        Preconditions.checkNotNull( commandRunnerBase, "Command Runner is null" );

        this.commandRunnerBase = commandRunnerBase;
    }


    public static CommandDef getRegisterSecondaryNodeWithPrimaryCommandLine( String secondaryNodeHostname,
                                                                             int dataNodePort, String domainName )
    {
        return new CommandDef( "Register node with replica",
                String.format( "mongo --port %s --eval \"%s\"", dataNodePort,
                        "rs.add('" + secondaryNodeHostname + "." + domainName + ":" + dataNodePort + "');" ), 900 );
    }


    public Command getRegisterSecondaryNodeWithPrimaryCommandOld( Agent secondaryNodeAgent, int dataNodePort,
                                                                  String domainName, Agent primaryNodeAgent )
    {

        return commandRunnerBase.createCommand( "Register node with replica", new RequestBuilder(
                String.format( "mongo --port %s --eval \"%s\"", dataNodePort,
                        "rs.add('" + secondaryNodeAgent.getHostname() + "." + domainName + ":" + dataNodePort
                                + "');" ) ).withTimeout( 900 ), Sets.newHashSet( primaryNodeAgent ) );
    }


    public static CommandDef getUnregisterSecondaryNodeFromPrimaryCommandLine( int dataNodePort,
                                                                               String removeNodehostname,
                                                                               String domainName )
    {
        return new CommandDef( "Unregister node from replica",
                String.format( "mongo --port %s --eval \"rs.remove('%s.%s:%s');\"", dataNodePort, removeNodehostname,
                        domainName, dataNodePort ), 300 );
    }


    public Command getUnregisterSecondaryNodeFromPrimaryCommandOld( Agent primaryNodeAgent, int dataNodePort,
                                                                    Agent removeNode, String domainName )
    {
        return commandRunnerBase.createCommand( "Unregister node from replica", new RequestBuilder(
                        String.format( "mongo --port %s --eval \"rs.remove('%s.%s:%s');\"", dataNodePort,
                                removeNode.getHostname(), domainName, dataNodePort ) ).withTimeout( 300 ),
                Sets.newHashSet( primaryNodeAgent ) );
    }


    public Command getCheckInstanceRunningCommandOld( Agent node, String domainName, int port )
    {
        return commandRunnerBase.createCommand( "Check node(s)", new RequestBuilder(
                String.format( "mongo --host %s.%s --port %s", node.getHostname(), domainName, port ) )
                .withTimeout( Timeouts.CHECK_NODE_STATUS_TIMEOUT_SEC ), Sets.newHashSet( node ) );
    }


    public static CommandDef getStopNodeCommand()
    {
        return new CommandDef( "Stop node", "/usr/bin/pkill -2 mongo", Timeouts.STOP_NODE_TIMEOUT_SEC );
    }


    public Command getStopNodeCommandOld( Set<Agent> nodes )
    {
        return commandRunnerBase.createCommand( "Stop node(s)",
                new RequestBuilder( "/usr/bin/pkill -2 mongo" ).withTimeout( Timeouts.STOP_NODE_TIMEOUT_SEC ), nodes );
    }


    public List<Command> getInstallationCommandsOld( MongoClusterConfig config )
    {
        return null;
        //        List<Command> commands = new ArrayList<>();
        //
        //        commands.add( getAddIpHostToEtcHostsCommand( config.getDomainName(), config.getAllNodes() ) );
        //
        //        commands.add( getSetReplicaSetNameCommand( config.getReplicaSetName(), config.getDataNodes() ) );
        //
        //        Command startConfigServersCommand =
        //                getStartConfigServerCommand( config.getCfgSrvPort(), config.getConfigServers() );
        //        startConfigServersCommand.setData( CommandType.START_CONFIG_SERVERS );
        //        commands.add( startConfigServersCommand );
        //
        //        Command startRoutersCommand =
        //                getStartRouterCommand( config.getRouterPort(), config.getCfgSrvPort(), config.getDomainName(),
        //                        config.getConfigServers(), config.getRouterServers() );
        //        startRoutersCommand.setData( CommandType.START_ROUTERS );
        //        commands.add( startRoutersCommand );
        //
        //        commands.add( getStopMongodbService( Sets.newHashSet( config.getDataNodes() ) ) );
        //
        //        Command startDataNodesCommand = getStartDataNodeCommand( config.getDataNodePort(),
        // config.getDataNodes() );
        //        startDataNodesCommand.setData( CommandType.START_DATA_NODES );
        //        commands.add( startDataNodesCommand );
        //
        //        commands.add( getRegisterSecondaryNodesWithPrimaryCommand( config.getDataNodes(),
        // config.getDataNodePort(),
        //                config.getDomainName() ) );
        //
        //        commands.add( getRegisterReplicaWithRouterCommand( config.getDataNodes(), config.getRouterPort(),
        //                config.getDataNodePort(), config.getDomainName(), config.getReplicaSetName(),
        //                config.getRouterServers().iterator().next() ) );
        //
        //        return commands;
    }


    public static CommandDef getStopMongodbService()
    {
        return new CommandDef( "Stop mongodb service", "service mongodb stop", Timeouts.STOP_NODE_TIMEOUT_SEC );
    }


    public Command getStopMongodbServiceOld( Set<Agent> dataNodes )
    {
        return commandRunnerBase.createCommand( "Stop mongodb service",
                new RequestBuilder( "service mongodb stop" ).withTimeout( Timeouts.START_DATE_NODE_TIMEOUT_SEC ),
                dataNodes );
    }


    public CommandDef getAddIpHostToEtcHostsCommand( String domainName, Host containerHost, Set<Host> others )
    {
        StringBuilder cleanHosts = new StringBuilder( "localhost|127.0.0.1|" );
        StringBuilder appendHosts = new StringBuilder();
        for ( Host otherAgent : others )
        {
            if ( containerHost.getId().equals( otherAgent.getId() ) )
            {
                continue;
            }

            String ip = AgentUtil.getAgentIpByMask( otherAgent.getAgent(), Common.IP_MASK );
            String hostname = otherAgent.getHostname();
            cleanHosts.append( ip ).append( "|" ).append( hostname ).append( "|" );
            appendHosts.append( "/bin/echo '" ).
                    append( ip ).append( " " ).
                               append( hostname ).append( "." ).append( domainName ).
                               append( " " ).append( hostname ).
                               append( "' >> '/etc/hosts'; " );
        }
        if ( cleanHosts.length() > 0 )
        {
            //drop pipe | symbol
            cleanHosts.setLength( cleanHosts.length() - 1 );
            cleanHosts.insert( 0, "egrep -v '" );
            cleanHosts.append( "' /etc/hosts > etc-hosts-cleaned; mv etc-hosts-cleaned /etc/hosts;" );
            appendHosts.insert( 0, cleanHosts );
        }

        appendHosts.append( "/bin/echo '127.0.0.1 localhost " ).append( containerHost.getHostname() )
                   .append( "' >> '/etc/hosts';" );

        return new CommandDef( "Add ip-host pair to /etc/hosts", appendHosts.toString(), 30 );
    }


    public Command getAddIpHostToEtcHostsCommandOld( String domainName, Set<Agent> agents )
    {
        Set<AgentRequestBuilder> requestBuilders = new HashSet<>();

        for ( Agent agent : agents )
        {
            StringBuilder cleanHosts = new StringBuilder( "localhost|127.0.0.1|" );
            StringBuilder appendHosts = new StringBuilder();
            for ( Agent otherAgent : agents )
            {
                if ( agent != otherAgent )
                {
                    String ip = AgentUtil.getAgentIpByMask( otherAgent, Common.IP_MASK );
                    String hostname = otherAgent.getHostname();
                    cleanHosts.append( ip ).append( "|" ).append( hostname ).append( "|" );
                    appendHosts.append( "/bin/echo '" ).
                            append( ip ).append( " " ).
                                       append( hostname ).append( "." ).append( domainName ).
                                       append( " " ).append( hostname ).
                                       append( "' >> '/etc/hosts'; " );
                }
            }
            if ( cleanHosts.length() > 0 )
            {
                //drop pipe | symbol
                cleanHosts.setLength( cleanHosts.length() - 1 );
                cleanHosts.insert( 0, "egrep -v '" );
                cleanHosts.append( "' /etc/hosts > etc-hosts-cleaned; mv etc-hosts-cleaned /etc/hosts;" );
                appendHosts.insert( 0, cleanHosts );
            }

            appendHosts.append( "/bin/echo '127.0.0.1 localhost " ).append( agent.getHostname() )
                       .append( "' >> '/etc/hosts';" );

            requestBuilders.add( ( AgentRequestBuilder ) new AgentRequestBuilder( agent, appendHosts.toString() )
                    .withTimeout( 30 ) );
        }

        return commandRunnerBase.createCommand( "Add ip-host pair to /etc/hosts", requestBuilders );
    }


    public static CommandDef getSetReplicaSetNameCommandLine( String replicaSetName )
    {
        return new CommandDef( "Set replica set name",
                String.format( "/bin/sed -i 's/# replSet = setname/replSet = %s/1' '%s'", replicaSetName,
                        Constants.DATA_NODE_CONF_FILE ), 30 );
    }


    public Command getSetReplicaSetNameCommandOld( String replicaSetName, Set<Agent> agents )
    {
        return commandRunnerBase.createCommand( "Set replica set name", new RequestBuilder(
                String.format( "/bin/sed -i 's/# replSet = setname/replSet = %s/1' '%s'", replicaSetName,
                        Constants.DATA_NODE_CONF_FILE ) ).withTimeout( 30 ), agents );
    }


    // LIFECYCLE COMMANDS =======================================================
    public static CommandDef getStartConfigServerCommand( int cfgSrvPort )
    {
        return new CommandDef( "Start config server(s)", String.format(
                "/bin/mkdir -p %s ; mongod --configsvr --dbpath %s --port %s --fork --logpath %s/mongodb.log",
                Constants.CONFIG_DIR, Constants.CONFIG_DIR, cfgSrvPort, Constants.LOG_DIR ),
                Timeouts.START_CONFIG_SERVER_TIMEOUT_SEC );
    }


    public Command getStartConfigServerCommandOld( int cfgSrvPort, Set<Agent> configServers )
    {
        return commandRunnerBase.createCommand( "Start config server(s)", new RequestBuilder( String.format(
                "/bin/mkdir -p %s ; mongod --configsvr --dbpath %s --port %s --fork --logpath %s/mongodb.log",
                Constants.CONFIG_DIR, Constants.CONFIG_DIR, cfgSrvPort, Constants.LOG_DIR ) )
                .withTimeout( Timeouts.START_CONFIG_SERVER_TIMEOUT_SEC ), configServers );
    }


    public CommandDef getStartRouterCommand( int routerPort, int cfgSrvPort, String domainName,
                                             Set<ContainerHost> configServers )
    {

        StringBuilder configServersArg = new StringBuilder();
        for ( ContainerHost c : configServers )
        {
            configServersArg.append( c.getHostname() ).append( "." ).append( domainName ).
                    append( ":" ).append( cfgSrvPort ).append( "," );
        }
        //drop comma
        if ( configServersArg.length() > 0 )
        {
            configServersArg.setLength( configServersArg.length() - 1 );
        }

        return new CommandDef( "Start router(s)",
                String.format( "mongos --configdb %s --port %s --fork --logpath %s/mongodb.log",
                        configServersArg.toString(), routerPort, Constants.LOG_DIR ),
                Timeouts.START_ROUTER_TIMEOUT_SEC );
    }


    public static CommandDef getStartRouterCommandLine( int routerPort, int cfgSrvPort, String domainName,
                                                        Set<MongoConfigNode> configServers )
    {

        StringBuilder configServersArg = new StringBuilder();
        for ( MongoConfigNode c : configServers )
        {
            configServersArg.append( c.getHostname() ).append( "." ).append( domainName ).
                    append( ":" ).append( cfgSrvPort ).append( "," );
        }
        //drop comma
        if ( configServersArg.length() > 0 )
        {
            configServersArg.setLength( configServersArg.length() - 1 );
        }

        return new CommandDef( "Start router(s)",
                String.format( "mongos --configdb %s --port %s --fork --logpath %s/mongodb.log",
                        configServersArg.toString(), routerPort, Constants.LOG_DIR ),
                Timeouts.START_ROUTER_TIMEOUT_SEC );
    }


    public Command getStartRouterCommandOld( int routerPort, int cfgSrvPort, String domainName,
                                             Set<Agent> configServers, Set<Agent> routers )
    {

        StringBuilder configServersArg = new StringBuilder();
        for ( Agent hostname : configServers )
        {
            configServersArg.append( hostname ).append( "." ).append( domainName ).
                    append( ":" ).append( cfgSrvPort ).append( "," );
        }
        //drop comma
        if ( configServersArg.length() > 0 )
        {
            configServersArg.setLength( configServersArg.length() - 1 );
        }


        return commandRunnerBase.createCommand( "Start router(s)", new RequestBuilder(
                String.format( "mongos --configdb %s --port %s --fork --logpath %s/mongodb.log",
                        configServersArg.toString(), routerPort, Constants.LOG_DIR ) )
                .withTimeout( Timeouts.START_ROUTER_TIMEOUT_SEC ), routers );
    }


    public static CommandDef getStartDataNodeCommandLine( int dataNodePort )
    {
        return new CommandDef( "Start data node", String.format(
                "export LANGUAGE=en_US.UTF-8 && export LANG=en_US.UTF-8 && "
                        + "export LC_ALL=en_US.UTF-8 && mongod --config %s --port %s --fork --logpath %s/mongodb.log",
                Constants.DATA_NODE_CONF_FILE, dataNodePort, Constants.LOG_DIR ),
                Timeouts.START_DATE_NODE_TIMEOUT_SEC );
    }


    public Command getStartDataNodeCommandOld( int dataNodePort, Set<Agent> dataNodes )
    {
        return commandRunnerBase.createCommand( "Start data node(s)", new RequestBuilder( String.format(
                "export LANGUAGE=en_US.UTF-8 && export LANG=en_US.UTF-8 && "
                        + "export LC_ALL=en_US.UTF-8 && mongod --config %s --port %s --fork --logpath %s/mongodb.log",
                Constants.DATA_NODE_CONF_FILE, dataNodePort, Constants.LOG_DIR ) )
                .withTimeout( Timeouts.START_DATE_NODE_TIMEOUT_SEC ), dataNodes );
    }


    public static CommandDef getInitiateReplicaSetCommandLine( int port )
    {
        return new CommandDef( "Initiate replica set",
                String.format( "mongo --port %d --eval \"rs.initiate();\" ; sleep 30", port ), 180 );
    }


    public Command getRegisterSecondaryNodesWithPrimaryCommandOld( Set<Agent> dataNodes, int dataNodePort,
                                                                   String domainName )
    {

        StringBuilder secondaryStr = new StringBuilder();
        Iterator<Agent> it = dataNodes.iterator();
        Agent primaryNodeAgent = it.next();
        while ( it.hasNext() )
        {
            Agent secondaryNodeAgent = it.next();
            secondaryStr.append( "rs.add('" ).
                    append( secondaryNodeAgent.getHostname() ).append( "." ).append( domainName ).
                                append( ":" ).append( dataNodePort ).append( "');" );
        }

        return commandRunnerBase.createCommand( "Initiate replica set", new RequestBuilder( String.format(
                        "mongo --port %s --eval \"rs.initiate();\" ; sleep 30 ; mongo --port %s --eval \"%s\"",
                        dataNodePort, dataNodePort, secondaryStr.toString() ) ).withTimeout( 180 ),
                Sets.newHashSet( primaryNodeAgent ) );
    }


    public static CommandDef getRegisterReplicaWithRouterCommandLine( MongoRouterNode routerNode,
                                                                      Set<MongoDataNode> dataNodes,
                                                                      String replicaSetName )
    {
        String domainName = routerNode.getDomainName();
        StringBuilder shard = new StringBuilder();
        for ( MongoDataNode dataNode : dataNodes )
        {
            shard.append( "sh.addShard('" ).append( replicaSetName ).
                    append( "/" ).append( dataNode.getHostname() ).append( "." ).append( domainName ).
                         append( ":" ).append( dataNode.getPort() ).append( "');" );
        }

        return new CommandDef( "Register replica with router",
                String.format( "sleep 30 ; mongo --port %s --eval \"%s\"", routerNode.getPort(), shard.toString() ),
                120 );
    }


    public Command getRegisterReplicaWithRouterCommandOld( Set<Agent> dataNodes, int routerPort, int dataNodePort,
                                                           String domainName, String replicaSetName, Agent router )
    {
        StringBuilder shard = new StringBuilder();
        for ( Agent agent : dataNodes )
        {
            shard.append( "sh.addShard('" ).append( replicaSetName ).
                    append( "/" ).append( agent.getHostname() ).append( "." ).append( domainName ).
                         append( ":" ).append( dataNodePort ).append( "');" );
        }

        return commandRunnerBase.createCommand( "Register replica with router", new RequestBuilder(
                String.format( "sleep 30 ; mongo --port %s --eval \"%s\"", routerPort, shard.toString() ) )
                .withTimeout( 120 ), Sets.newHashSet( router ) );
    }


    public void getAddRouterCommands( MongoClusterConfig config, MongoRouterNode newRouterAgent )
    {


        Set<Host> clusterMembers = new HashSet<Host>( config.getAllNodes() );
        clusterMembers.add( newRouterAgent );
        try
        {
            for ( Host c : clusterMembers )
            {
                CommandDef commandDef = getAddIpHostToEtcHostsCommand( config.getDomainName(), c, clusterMembers );

                c.execute( new RequestBuilder( commandDef.getCommand() ).withTimeout( commandDef.getTimeout() ) );
            }

            CommandDef commandDef =
                    getStartRouterCommandLine( config.getRouterPort(), config.getCfgSrvPort(), config.getDomainName(),
                            config.getConfigServers() );

            newRouterAgent
                    .execute( new RequestBuilder( commandDef.getCommand() ).withTimeout( commandDef.getTimeout() ) );
        }
        catch ( CommandException e )
        {
            e.printStackTrace();
        }
    }


    public List<Command> getAddRouterCommandsOld( MongoClusterConfig config, Agent newRouterAgent )
    {

        List<Command> commands = new ArrayList<>();
        //
        //        Set<Agent> clusterMembers = new HashSet<>( config.getAllNodes() );
        //        clusterMembers.add( newRouterAgent );
        //
        //        commands.add( getAddIpHostToEtcHostsCommand( config.getDomainName(), clusterMembers ) );
        //
        //        Command startRoutersCommand =
        //                getStartRouterCommand( config.getRouterPort(), config.getCfgSrvPort(), config.getDomainName(),
        //                        config.getConfigServers()/*, Sets.newHashSet( newRouterAgent )*/ );
        //
        //        startRoutersCommand.setData( CommandType.START_ROUTERS );
        //
        //        commands.add( startRoutersCommand );

        return commands;
    }


    public void getAddDataNodeCommands( MongoClusterConfig config, MongoDataNode newDataNode )
    {


        Set<Host> clusterMembers = new HashSet<Host>( config.getAllNodes() );
        clusterMembers.add( newDataNode );
        try
        {
            for ( Host c : clusterMembers )
            {
                CommandDef commandDef = getAddIpHostToEtcHostsCommand( config.getDomainName(), c, clusterMembers );

                c.execute( new RequestBuilder( commandDef.getCommand() ).withTimeout( commandDef.getTimeout() ) );
            }

            CommandDef commandDef = getSetReplicaSetNameCommandLine( config.getReplicaSetName() );

            newDataNode.execute( new RequestBuilder( commandDef.getCommand() ).withTimeout( commandDef.getTimeout() ) );

            commandDef = getStartDataNodeCommandLine( config.getDataNodePort() );

            newDataNode.execute( new RequestBuilder( commandDef.getCommand() ).withTimeout( commandDef.getTimeout() ) );

            commandDef = getStartDataNodeCommandLine( config.getDataNodePort() );

            newDataNode.execute( new RequestBuilder( commandDef.getCommand() ).withTimeout( commandDef.getTimeout() ) );

            commandDef = getFindPrimaryNodeCommandLine( config.getDataNodePort() );

            config.getDataNodes().iterator().next()
                  .execute( new RequestBuilder( commandDef.getCommand() ).withTimeout( commandDef.getTimeout() ) );
        }
        catch ( CommandException e )
        {
            e.printStackTrace();
        }
    }


    public List<Command> getAddDataNodeCommandsOld( MongoClusterConfig config, Agent newDataNodeAgent )
    {
        return null;
        //        List<Command> commands = new ArrayList<>();
        //
        //        Set<String> clusterMembers = new HashSet<>( config.getAllNodes() );
        //        clusterMembers.add( newDataNodeAgent.getHostname() );
        //
        //        commands.add( getAddIpHostToEtcHostsCommand( config.getDomainName(), clusterMembers ) );
        //
        //        commands.add( getSetReplicaSetNameCommand( config.getReplicaSetName(),
        // Sets.newHashSet( newDataNodeAgent ) ) );
        //
        //        Command startDataNodesCommand =
        //                getStartDataNodeCommand( config.getDataNodePort(), Sets.newHashSet( newDataNodeAgent ) );
        //        startDataNodesCommand.setData( CommandType.START_DATA_NODES );
        //        commands.add( startDataNodesCommand );
        //
        //        Command findPrimaryNodeCommand =
        //                getFindPrimaryNodeCommand( config.getDataNodes().iterator().next(),
        // config.getDataNodePort() );
        //        findPrimaryNodeCommand.setData( CommandType.FIND_PRIMARY_NODE );
        //        commands.add( findPrimaryNodeCommand );
        //
        //        return commands;
    }


    public static CommandDef getFindPrimaryNodeCommandLine( int dataNodePort )
    {
        return new CommandDef( "Find primary node",
                String.format( "/bin/echo 'db.isMaster()' | mongo --port %s", dataNodePort ), 30 );
    }


    public static CommandDef getCheckInstanceRunningCommand( String hostname, String domainName, int port )
    {
        return new CommandDef( "Check node(s)",
                String.format( "mongo --host %s.%s --port %s", hostname, domainName, port ),
                Timeouts.CHECK_NODE_STATUS_TIMEOUT_SEC );
    }


    public Command getFindPrimaryNodeCommandOld( Agent secondaryNode, int dataNodePort )
    {
        return commandRunnerBase.createCommand( "Find primary node",
                new RequestBuilder( String.format( "/bin/echo 'db.isMaster()' | mongo --port %s", dataNodePort ) )
                        .withTimeout( 30 ), Sets.newHashSet( secondaryNode ) );
    }
}
