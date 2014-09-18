package org.safehaus.subutai.core.network.impl;


import java.util.List;
import java.util.logging.Logger;

import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.protocol.Agent;

import com.google.common.collect.Sets;


/**
 * Created by daralbaev on 04.04.14.
 */
public class HostManager {
    protected static final Logger LOG = Logger.getLogger( HostManager.class.getName() );

    private List<Agent> agentList;
    private String domainName;
    private Commands commands;


    public HostManager( Commands commands, List<Agent> agentList, String domainName )
    {
        this.agentList = agentList;
        this.domainName = domainName;
        this.commands = commands;
    }


    public boolean execute()
    {
        return agentList != null && !agentList.isEmpty() && write();
    }


    private boolean write()
    {
        //        String hosts = prepareHost();
        //        Command command = commands.getWriteHostsCommand(agentList, hosts);
        Command command = commands.getAddIpHostToEtcHostsCommand( domainName, Sets.newHashSet( agentList ) );
        try
        {
            command.execute();
        }
        catch ( CommandException e )
        {
            LOG.severe( String.format( "Error in write: %s", e.getMessage() ) );
        }

        return command.hasSucceeded();
    }


    public boolean execute( Agent agent )
    {
        if ( agentList != null && !agentList.isEmpty() && agent != null )
        {
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
