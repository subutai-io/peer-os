package org.safehaus.subutai.core.peer.impl;


import java.util.UUID;

import javax.naming.NamingException;

import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.CommandResult;
import org.safehaus.subutai.common.protocol.CommandStatus;
import org.safehaus.subutai.common.protocol.NullAgent;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandCallback;
import org.safehaus.subutai.core.dispatcher.api.CommandDispatcher;
import org.safehaus.subutai.core.peer.api.Host;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
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


    protected HostImpl( final Agent agent )
    {
        Preconditions.checkNotNull( agent, "Agent is null" );

        this.agent = agent;
    }


    public Agent getAgent()
    {
        return agent;
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


    @Override
    public CommandResult execute( final RequestBuilder requestBuilder,
                                  final org.safehaus.subutai.common.protocol.CommandCallback commandCallback )
            throws CommandException
    {

        CommandDispatcher commandDispatcher = getCommandDispatcher();

        Command command = commandDispatcher.createCommand( requestBuilder, Sets.newHashSet( agent ) );
        command.execute( new HostCommandCallback( commandCallback ) );

        AgentResult agentResult = command.getResults().get( agent.getUuid() );

        if ( agentResult != null )
        {
            return new CommandResult( agentResult.getExitCode(), agentResult.getStdOut(), agentResult.getStdErr(),
                    command.getCommandStatus() );
        }
        else
        {
            return new CommandResult( null, null, null, CommandStatus.TIMEOUT );
        }
    }


    @Override
    public void executeAsync( final RequestBuilder requestBuilder ) throws CommandException
    {
        executeAsync( requestBuilder, new org.safehaus.subutai.common.protocol.CommandCallback()
        {
            @Override
            public void onResponse( final Response response, final CommandResult commandResult )
            {

            }
        } );
    }


    @Override
    public CommandResult execute( RequestBuilder requestBuilder ) throws CommandException
    {
        return execute( requestBuilder, new org.safehaus.subutai.common.protocol.CommandCallback()
        {
            @Override
            public void onResponse( final Response response, final CommandResult commandResult )
            {

            }
        } );
    }


    @Override
    public void executeAsync( final RequestBuilder requestBuilder,
                              final org.safehaus.subutai.common.protocol.CommandCallback commandCallback )
            throws CommandException
    {

        CommandDispatcher commandDispatcher = getCommandDispatcher();

        Command command = commandDispatcher.createCommand( requestBuilder, Sets.newHashSet( agent ) );
        command.executeAsync( new HostCommandCallback( commandCallback ) );
    }


    public void executeAsync( RequestBuilder requestBilder, CommandCallback commandCallback ) throws CommandException
    {
        CommandDispatcher commandDispatcher = getCommandDispatcher();

        Command command = commandDispatcher.createCommand( requestBilder, Sets.newHashSet( agent ) );
        command.executeAsync( commandCallback );
    }


    private CommandDispatcher getCommandDispatcher() throws CommandException
    {
        try
        {
            return ServiceLocator.getServiceNoCache( CommandDispatcher.class );
        }
        catch ( NamingException e )
        {
            throw new CommandException( e );
        }
    }


    public boolean isConnected()
    {
        return System.currentTimeMillis() - lastHeartbeat > inactiveTime;
    }


    public String echo( String text ) throws CommandException
    {
        RequestBuilder requestBuilder = new RequestBuilder( "echo " + text );
        CommandResult result = execute( requestBuilder );
        if ( result.hasSucceeded() )
        {
            return result.getStdOut();
        }
        else
        {
            if ( result.hasTimedOut() )
            {
                throw new CommandException( "Command timed out" );
            }
            else
            {
                throw new CommandException( "Echo execution error: " + result.getStdErr() );
            }
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
