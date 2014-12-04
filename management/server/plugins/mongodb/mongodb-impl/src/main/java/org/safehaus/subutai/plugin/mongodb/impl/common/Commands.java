/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.mongodb.impl.common;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.Host;
import org.safehaus.subutai.plugin.mongodb.api.MongoClusterConfig;
import org.safehaus.subutai.plugin.mongodb.api.MongoConfigNode;
import org.safehaus.subutai.plugin.mongodb.api.MongoDataNode;
import org.safehaus.subutai.plugin.mongodb.api.MongoNode;
import org.safehaus.subutai.plugin.mongodb.api.MongoRouterNode;
import org.safehaus.subutai.plugin.mongodb.api.Timeouts;


/**
 * Holds all mongo related commands
 */
public class Commands
{

    public static CommandDef getRegisterSecondaryNodeWithPrimaryCommandLine( String secondaryNodeHostname,
                                                                             int dataNodePort, String domainName )
    {
        return new CommandDef( "Register node with replica",
                String.format( "mongo --port %s --eval \"%s\"", dataNodePort,
                        "rs.add('" + secondaryNodeHostname + "." + domainName + ":" + dataNodePort + "');" ), 900 );
    }


    public static CommandDef getUnregisterSecondaryNodeFromPrimaryCommandLine( int dataNodePort,
                                                                               String removeNodehostname,
                                                                               String domainName )
    {
        return new CommandDef( "Unregister node from replica",
                String.format( "mongo --port %s --eval \"rs.remove('%s.%s:%s');\"", dataNodePort, removeNodehostname,
                        domainName, dataNodePort ), 300 );
    }


    public static CommandDef getStopNodeCommand()
    {
        return new CommandDef( "Stop node", "/usr/bin/pkill -2 mongo", Timeouts.STOP_NODE_TIMEOUT_SEC );
    }


    public static CommandDef getStopMongodbService()
    {
        return new CommandDef( "Stop mongodb service", "service mongodb stop", Timeouts.STOP_NODE_TIMEOUT_SEC );
    }


    public CommandDef getAddIpHostToEtcHostsCommand( String domainName, Host containerHost, Set<MongoNode> others )
    {
        StringBuilder cleanHosts = new StringBuilder( "localhost|127.0.0.1|" );
        StringBuilder appendHosts = new StringBuilder();
        for ( MongoNode otherNode : others )
        {
            if ( containerHost.getId().equals( otherNode.getContainerHost().getId() ) )
            {
                continue;
            }

            String ip = otherNode.getContainerHost().getIpByMask( Common.IP_MASK );
            String hostname = otherNode.getHostname();
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

        return new CommandDef( "Add ip-host pair to /etc/hosts",
                String.format( "sh -c echo `%s`", appendHosts.toString() ), 30 );
    }


    public static CommandDef getSetReplicaSetNameCommandLine( String replicaSetName )
    {
        return new CommandDef( "Set replica set name",
                String.format( "/bin/sed -i 's/# replSet = setname/replSet = %s/1' '%s'", replicaSetName,
                        Constants.DATA_NODE_CONF_FILE ), 30 );
    }


    // LIFECYCLE COMMANDS =======================================================
    public static CommandDef getStartConfigServerCommand( int cfgSrvPort )
    {
        return new CommandDef( "Start config server(s)", String.format(
                "/bin/mkdir -p %s ; mongod --configsvr --dbpath %s --port %s --fork --logpath %s/mongodb.log",
                Constants.CONFIG_DIR, Constants.CONFIG_DIR, cfgSrvPort, Constants.LOG_DIR ),
                Timeouts.START_CONFIG_SERVER_TIMEOUT_SEC );
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


    public static CommandDef getStartDataNodeCommandLine( int dataNodePort )
    {
        return new CommandDef( "Start data node", String.format(
                "export LANGUAGE=en_US.UTF-8 && export LANG=en_US.UTF-8 && "
                        + "export LC_ALL=en_US.UTF-8 && mongod --config %s --port %s --fork --logpath %s/mongodb.log",
                Constants.DATA_NODE_CONF_FILE, dataNodePort, Constants.LOG_DIR ),
                Timeouts.START_DATE_NODE_TIMEOUT_SEC );
    }


    public static CommandDef getInitiateReplicaSetCommandLine( int port )
    {
        return new CommandDef( "Initiate replica set",
                String.format( "mongo --port %d --eval \"rs.initiate();\" ; sleep 30", port ), 180 );
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


    public void getAddRouterCommands( MongoClusterConfig config, MongoRouterNode newRouterAgent )
    {


        Set<MongoNode> clusterMembers = new HashSet<>( config.getAllNodes() );
        clusterMembers.add( newRouterAgent );
        try
        {
            for ( MongoNode c : clusterMembers )
            {
                CommandDef commandDef =
                        getAddIpHostToEtcHostsCommand( config.getDomainName(), c.getContainerHost(), clusterMembers );

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


    public void getAddDataNodeCommands( MongoClusterConfig config, MongoDataNode newDataNode )
    {


        Set<MongoNode> clusterMembers = new HashSet<>( config.getAllNodes() );
        clusterMembers.add( newDataNode );
        try
        {
            for ( MongoNode c : clusterMembers )
            {
                CommandDef commandDef =
                        getAddIpHostToEtcHostsCommand( config.getDomainName(), c.getContainerHost(), clusterMembers );

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
}
