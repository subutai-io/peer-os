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


    public Peer getPeer() throws PeerException
    {
        Peer result;
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
        return execute( requestBuilder, null );
    }


    @Override
    public CommandResult execute( final RequestBuilder requestBuilder, final CommandCallback callback )
            throws CommandException
    {
        try
        {
            Peer peer = getPeer();
            return peer.execute( requestBuilder, this, callback );
        }
        catch ( PeerException e )
        {
            throw new CommandException( e.toString() );
        }
    }


    @Override
    public void executeAsync( final RequestBuilder requestBuilder ) throws CommandException
    {
        executeAsync( requestBuilder, null );
    }


    @Override
    public void executeAsync( final RequestBuilder requestBuilder, final CommandCallback callback )
            throws CommandException
    {
        try
        {
            Peer peer = getPeer();
            peer.executeAsync( requestBuilder, this, callback );
        }
        catch ( PeerException e )
        {
            throw new CommandException( e.toString() );
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


    public void resetHeartbeat()
    {
        if ( lastHeartbeat > 10 * 100 * 6 )
        {
            lastHeartbeat -= 10 * 10 * 6;
        }
    }


    @Override
    public boolean isConnected()
    {
        try
        {
            Peer peer = getPeer();
            return peer.isConnected( this );
        }
        catch ( PeerException e )
        {

            return false;
        }
    }


    public long getLastHeartbeat()
    {
        return lastHeartbeat;
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        final SubutaiHost that = ( SubutaiHost ) o;

        if ( !agent.equals( that.agent ) )
        {
            return false;
        }

        return true;
    }


    @Override
    public int hashCode()
    {
        return agent.hashCode();
    }
}
