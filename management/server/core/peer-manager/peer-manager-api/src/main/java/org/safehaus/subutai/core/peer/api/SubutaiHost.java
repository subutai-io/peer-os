package org.safehaus.subutai.core.peer.api;


import java.util.UUID;

import javax.naming.NamingException;

import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.CommandResult;
import org.safehaus.subutai.common.protocol.NullAgent;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.common.util.ServiceLocator;

import com.google.common.base.Preconditions;


/**
 * Base Subutai host class.
 */
public abstract class SubutaiHost implements Host
{
    private Agent agent = NullAgent.getInstance();
    private Agent parentAgent = NullAgent.getInstance();
    private long lastHeartbeat = System.currentTimeMillis();
    transient private static final long INACTIVE_TIME = 5 * 1000 * 60; // 5 min


    protected SubutaiHost( final Agent agent )
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
            result = peerManager.getPeer( peerId );
            if ( result == null )
            {
                throw new PeerException( "Peer not registered." );
            }
        }
        catch ( NamingException e )
        {
            throw new PeerException( "Coould not locate PeerManager" );
        }

        return result;
    }


    @Override
    public CommandResult execute( final RequestBuilder requestBuilder ) throws CommandException
    {
        try
        {
            Peer peer = getPeer( this.getPeerId() );
            CommandResult commandResult = peer.execute( requestBuilder, this );
            return commandResult;
        }
        catch ( PeerException e )
        {
            throw new CommandException( e.toString() );
        }
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


    public void updateHeartbeat()
    {
        lastHeartbeat = System.currentTimeMillis();
    }


    @Override
    public boolean isConnected()
    {
        return ( System.currentTimeMillis() - lastHeartbeat ) < INACTIVE_TIME;
    }


    @Override
    public long getLastHeartbeat()
    {
        return lastHeartbeat;
    }
}
