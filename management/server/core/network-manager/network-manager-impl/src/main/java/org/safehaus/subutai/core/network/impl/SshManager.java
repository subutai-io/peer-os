package org.safehaus.subutai.core.network.impl;


import java.util.Arrays;
import java.util.List;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * Ssh manager for exchanging ssh keys and enabling passwordless communication
 */
public class SshManager
{

    private static final Logger LOG = LoggerFactory.getLogger( SshManager.class.getName() );

    private List<Agent> agentList;
    private String keys;
    private Commands commands;


    public SshManager( Commands commands, List<Agent> agentList )
    {
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( agentList ), "Agent list is empty" );

        this.agentList = agentList;
        this.commands = commands;
    }


    public boolean execute()
    {

        return create() && read() && write() && config();
    }


    private boolean create()
    {
        Command command = commands.getCreateSSHCommand( agentList );
        try
        {
            command.execute();
            return command.hasSucceeded();
        }
        catch ( CommandException e )
        {
            LOG.error( String.format( "Error in create: %s", e.getMessage() ), e );
        }
        return false;
    }


    private boolean read()
    {
        Command command = commands.getReadSSHCommand( agentList );
        try
        {
            command.execute();
            StringBuilder value = new StringBuilder();
            if ( !command.hasCompleted() )
            {
                return false;
            }
            for ( AgentResult result : command.getResults().values() )
            {
                if ( !Strings.isNullOrEmpty( result.getStdOut() ) )
                {
                    value.append( result.getStdOut() );
                }
            }
            keys = value.toString();

            return !Strings.isNullOrEmpty( keys ) && command.hasSucceeded();
        }
        catch ( CommandException e )
        {
            LOG.error( String.format( "Error in read: %s", e.getMessage() ), e );
        }
        return false;
    }


    private boolean write()
    {
        Command command = commands.getWriteSSHCommand( agentList, keys );
        try
        {
            command.execute();
            return command.hasSucceeded();
        }
        catch ( CommandException e )
        {
            LOG.error( String.format( "Error in write: %s", e.getMessage() ), e );
        }
        return false;
    }


    private boolean config()
    {
        Command command = commands.getConfigSSHCommand( agentList );
        try
        {
            command.execute();
            return command.hasSucceeded();
        }
        catch ( CommandException e )
        {
            LOG.error( String.format( "Error in config: %s", e.getMessage() ), e );
        }
        return false;
    }


    public boolean execute( Agent agent )
    {
        Preconditions.checkNotNull( agent, "Agent is null" );

        if ( create( agent ) )
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
            return command.hasSucceeded();
        }
        catch ( CommandException e )
        {
            LOG.error( String.format( "Error in write: %s", e.getMessage() ), e );
        }
        return false;
    }
}
