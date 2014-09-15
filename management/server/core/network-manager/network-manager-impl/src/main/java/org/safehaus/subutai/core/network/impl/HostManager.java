package org.safehaus.subutai.core.network.impl;


import java.util.List;

import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.CommandRunner;

import com.google.common.collect.Sets;


/**
 * Created by daralbaev on 04.04.14.
 */
public class HostManager {
    private List<Agent> agentList;
    private String domainName;
    private CommandRunner commandRunner;
    private Commands commands;


    public HostManager( CommandRunner commandRunner, List<Agent> agentList, String domainName ) {
        this.agentList = agentList;
        this.domainName = domainName;
        this.commandRunner = commandRunner;
        this.commands = new Commands( commandRunner );
    }


    public boolean execute() {
        if ( agentList != null && !agentList.isEmpty() ) {
            return write();
        }

        return false;
    }


    private boolean write() {
        //        String hosts = prepareHost();
        //        Command command = commands.getWriteHostsCommand(agentList, hosts);
        Command command = commands.getAddIpHostToEtcHostsCommand( domainName, Sets.newHashSet( agentList ) );
        commandRunner.runCommand( command );

        return command.hasSucceeded();
    }


    public boolean execute( Agent agent ) {
        if ( agentList != null && !agentList.isEmpty() && agent != null ) {
            agentList.add( agent );
            return write();
        }

        return false;
    }


    //    private String prepareHost() {
    //        StringBuilder value = new StringBuilder();
    //
    //        for ( Agent agent : agentList ) {
    //            value.append( agent.getListIP().get( 0 ) );
    //            value.append( "\t" );
    //            value.append( agent.getHostname() );
    //            value.append( "." );
    //            value.append( domainName );
    //            value.append( "\t" );
    //            value.append( agent.getHostname() );
    //            value.append( "\n" );
    //        }
    //        value.append( "127.0.0.1\tlocalhost" );
    //
    //        return value.toString();
    //    }
}
