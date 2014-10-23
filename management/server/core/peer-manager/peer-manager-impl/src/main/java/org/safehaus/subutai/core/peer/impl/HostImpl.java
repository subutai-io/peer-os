package org.safehaus.subutai.core.peer.impl;


import java.util.Map;
import java.util.UUID;

import javax.naming.NamingException;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.NullAgent;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandCallback;
import org.safehaus.subutai.core.command.api.command.CommandException;
import org.safehaus.subutai.core.command.api.command.RequestBuilder;
import org.safehaus.subutai.core.dispatcher.api.CommandDispatcher;
import org.safehaus.subutai.core.peer.api.Host;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;


/**
 * Base class for Host.
 */
public abstract class HostImpl implements Host
{
    private static final Logger LOG = LoggerFactory.getLogger( HostImpl.class );

    private Agent agent = NullAgent.getInstance();
    private Agent parentAgent = NullAgent.getInstance();
    private long lastHeartbeat;
    private long inactiveTime = 5 * 1000 * 60; // 5 min


    public Agent getAgent()
    {
        return agent;
    }


    public void setAgent( final Agent agent )
    {
        this.agent = agent;
    }


    public Agent getParentAgent()
    {
        return parentAgent;
    }


    public void setParentAgent( final Agent parentAgent )
    {
        this.parentAgent = parentAgent;
    }


    public Peer getPeer( UUID peerId ) throws PeerException
    {
        Peer result = null;
        try
        {
            PeerManager peerManager = ServiceLocator.getServiceNoCache( PeerManager.class );
            peerManager.getPeer( peerId );
        }
        catch ( NamingException e )
        {
            throw new PeerException( "Coould not locate PeerManager" );
        }

        return result;
    }


    public Command execute( RequestBuilder requestBilder ) throws CommandException
    {
        if ( agent == null || !isConnected() )
        {
            throw new CommandException( "Could not execute command. Agent is null or disconnected" );
        }
        CommandDispatcher commandDispatcher = getCommandDispatcher();

        Command command = commandDispatcher.createCommand( requestBilder, Sets.newHashSet( agent ) );
        command.execute();
        return command;
    }


    public void executeAsync( RequestBuilder requestBilder, CommandCallback commandCallback ) throws CommandException
    {
        CommandDispatcher commandDispatcher = getCommandDispatcher();

        Command command = commandDispatcher.createCommand( requestBilder, Sets.newHashSet( agent ) );
        command.executeAsync( commandCallback );
    }


    private CommandDispatcher getCommandDispatcher() throws CommandException
    {
        CommandDispatcher commandDispatcher = null;
        try
        {
            ServiceLocator.getServiceNoCache( CommandDispatcher.class );
        }
        catch ( NamingException e )
        {
            throw new CommandException( e.toString() );
        }

        return commandDispatcher;
    }


    public boolean isConnected()
    {
        return System.currentTimeMillis() - lastHeartbeat > inactiveTime;
    }


    public String echo( String text ) throws CommandException
    {
        RequestBuilder requestBuilder = new RequestBuilder( "echo " + text );
        Command command = execute( requestBuilder );
        if ( command.hasSucceeded() )
        {
            Map<UUID, AgentResult> results = command.getResults();
            StringBuilder sb = new StringBuilder();
            for ( UUID agentId : results.keySet() )
            {
                if ( agentId.equals( agent.getUuid() ) )
                {
                    sb.append( results.get( agentId ).getStdOut() );
                }
                else
                {
                    throw new CommandException( "Unknown agentID in AgentResult." );
                }
            }
            return sb.toString();
        }
        else
        {
            throw new CommandException( "Echo execution error: " + command.getAllErrors() );
        }
    }


    @Override
    public UUID getPeerId()
    {
        return agent.getSiteId();
    }


    @Override
    public UUID getId()
    {
        return agent.getUuid();
    }


    @Override
    public String getParentHostname()
    {
        return agent.getParentHostName();
    }


    @Override
    public String getHostname()
    {
        return agent.getHostname();
    }


    @Override
    public void updateHeartbeat()
    {
        lastHeartbeat = System.currentTimeMillis();
    }
}
