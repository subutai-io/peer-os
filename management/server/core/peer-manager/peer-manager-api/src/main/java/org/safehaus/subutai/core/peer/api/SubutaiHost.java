package org.safehaus.subutai.core.peer.api;


import java.util.UUID;

import javax.naming.NamingException;

import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.CommandCallback;
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
    private UUID peerId;
    private Agent agent = NullAgent.getInstance();
    private Agent parentAgent = NullAgent.getInstance();
    protected long lastHeartbeat = System.currentTimeMillis();


    protected SubutaiHost( final Agent agent, UUID peerId )
    {
        Preconditions.checkNotNull( agent, "Agent is null" );

        this.agent = agent;
        this.peerId = peerId;
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


    @Override
    public void execute( final RequestBuilder requestBuilder, final CommandCallback callback ) throws CommandException
    {
        try
        {
            Peer peer = getPeer( this.getPeerId() );
            peer.execute( requestBuilder, this, callback );
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
        return peerId;
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
        try
        {
            Peer peer = getPeer( this.getPeerId() );
            return peer.isConnected( this );
        }
        catch ( PeerException e )
        {

            return false;
        }
        //return ( System.currentTimeMillis() - lastHeartbeat ) < INACTIVE_TIME;
    }


    @Override
    public long getLastHeartbeat()
    {
        return lastHeartbeat;
    }
}
