package org.safehaus.subutai.core.network.impl;


import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.safehaus.subutai.common.command.AgentResult;
import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.protocol.Agent;

import com.google.common.base.Strings;


/**
 * Ssh manager for exchanging ssh keys and enabling passwordless communication
 */
public class SshManager
{

    private static final Logger LOG = Logger.getLogger( HostManager.class.getName() );

    private List<Agent> agentList;
    private String keys;
    private Commands commands;


    public SshManager( Commands commands, List<Agent> agentList )
    {
        this.agentList = agentList;
        this.commands = commands;
    }


    public boolean execute()
    {
        return agentList != null && !agentList.isEmpty() && create() && read() && write() && config();
    }


    private boolean create()
    {
        Command command = commands.getCreateSSHCommand( agentList );
        try
        {
            command.execute();
        }
        catch ( CommandException e )
        {
            LOG.log( Level.SEVERE, String.format( "Error in write: %s", e.getMessage() ), e );
        }

        return command.hasSucceeded();
    }


    private boolean read()
    {
        Command command = commands.getReadSSHCommand( agentList );
        try
        {
            command.execute();
        }
        catch ( CommandException e )
        {
            LOG.log( Level.SEVERE, String.format( "Error in write: %s", e.getMessage() ), e );
        }

        StringBuilder value = new StringBuilder();
        if ( command.hasCompleted() )
        {
            for ( Agent agent : agentList )
            {
                AgentResult result = command.getResults().get( agent.getUuid() );
                if ( !Strings.isNullOrEmpty( result.getStdOut() ) )
                {
                    value.append( result.getStdOut() );
                }
            }
        }
        keys = value.toString();

        return !Strings.isNullOrEmpty( keys ) && command.hasSucceeded();
    }


    private boolean write()
    {
        Command command = commands.getWriteSSHCommand( agentList, keys );
        try
        {
            command.execute();
        }
        catch ( CommandException e )
        {
            LOG.log( Level.SEVERE, String.format( "Error in write: %s", e.getMessage() ), e );
        }


        return command.hasSucceeded();
    }


    private boolean config()
    {
        Command command = commands.getConfigSSHCommand( agentList );
        try
        {
            command.execute();
        }
        catch ( CommandException e )
        {
            LOG.log( Level.SEVERE, String.format( "Error in write: %s", e.getMessage() ), e );
        }


        return command.hasSucceeded();
    }


    public boolean execute( Agent agent )
    {
        if ( agentList != null && !agentList.isEmpty() && agent != null && create( agent ) )
        {
            agentList.add( agent );

            return read() && write() && config();
        }

        return false;
    }


    private boolean create( Agent agent )
    {
        Command command = commands.getCreateSSHCommand( Arrays.asList( agent ) );
        try
        {
            command.execute();
        }
        catch ( CommandException e )
        {
            LOG.log( Level.SEVERE, String.format( "Error in write: %s", e.getMessage() ), e );
        }

        return command.hasSucceeded();
    }
}
