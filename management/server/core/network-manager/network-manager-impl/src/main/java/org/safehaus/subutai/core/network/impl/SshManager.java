package org.safehaus.subutai.core.network.impl;


import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.safehaus.subutai.common.command.AgentResult;
import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.protocol.Agent;

import com.google.common.base.Strings;


/**
 * Created by daralbaev on 04.04.14.
 */
public class SshManager {

    protected static final Logger LOG = Logger.getLogger( HostManager.class.getName() );

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
        if ( agentList != null && !agentList.isEmpty() )
        {
            if ( create() )
            {
                if ( read() )
                {
                    if ( write() )
                    {
                        return config();
                    }
                }
            }
        }

        return false;
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
            LOG.severe( String.format( "Error in write: %s", e.getMessage() ) );
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
            LOG.severe( String.format( "Error in write: %s", e.getMessage() ) );
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
            LOG.severe( String.format( "Error in write: %s", e.getMessage() ) );
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
            LOG.severe( String.format( "Error in write: %s", e.getMessage() ) );
        }


        return command.hasSucceeded();
    }


    public boolean execute( Agent agent )
    {
        if ( agentList != null && !agentList.isEmpty() && agent != null )
        {
            if ( create( agent ) )
            {
                agentList.add( agent );

                if ( read() )
                {
                    if ( write() )
                    {
                        return config();
                    }
                }
            }
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
            LOG.severe( String.format( "Error in write: %s", e.getMessage() ) );
        }

        return command.hasSucceeded();
    }
}
