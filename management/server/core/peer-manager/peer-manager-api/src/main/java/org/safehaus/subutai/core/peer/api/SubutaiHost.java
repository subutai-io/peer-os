package org.safehaus.subutai.core.peer.api;


import java.util.Set;
import java.util.UUID;

import javax.naming.NamingException;

import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.exception.SubutaiException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.NullAgent;
import org.safehaus.subutai.common.settings.Common;
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
    //    private UUID id;
    //    private String hostname;
    //    private Set<Interface> interfaces;


    protected SubutaiHost( final Agent agent, UUID peerId )
    {
        Preconditions.checkNotNull( agent, "Agent is null" );

        this.agent = agent;
        this.peerId = peerId;
        //        this.id = agent.getUuid();
        //        this.hostname = agent.getHostname();
        //        this.interfaces = new HashSet<>();
    }

    //
    //    protected SubutaiHost( ResourceHostInfo resourceHostInfo )
    //    {
    //        Preconditions.checkNotNull( resourceHostInfo, "ResourceHostInfo is null" );
    //        hostname = resourceHostInfo.getHostname();
    //        id = resourceHostInfo.getId();
    //        interfaces = resourceHostInfo.getInterfaces();
    //    }


    @Override
    public String getHostId()
    {
        return this.getId() != null ? this.getId().toString() : null;
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
    public String getPeerId()
    {
        return peerId.toString();
    }

    //
    //    @Override
    //    public void setPeerId( final UUID peerId )
    //    {
    //        this.peerId = peerId;
    //    }


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


    @Override
    public String getIpByMask( String mask )
    {
        for ( String ip : agent.getListIP() )
        {
            if ( ip.matches( mask ) )
            {
                return ip;
            }
        }
        return null;
    }


    @Override
    public void addIpHostToEtcHosts( String domainName, Set<Host> others, String mask ) throws SubutaiException
    {
        StringBuilder cleanHosts = new StringBuilder( "localhost|127.0.0.1|" );
        StringBuilder appendHosts = new StringBuilder();
        for ( Host otherHost : others )
        {
            if ( getId().equals( otherHost.getId() ) )
            {
                continue;
            }

            String ip = otherHost.getIpByMask( Common.IP_MASK );
            String hostname = otherHost.getHostname();
            cleanHosts.append( ip ).append( "|" ).append( hostname ).append( "|" );
            appendHosts.append( "/bin/echo '" ).
                    append( ip ).append( " " ).
                               append( hostname ).append( "." ).append( domainName ).
                               append( " " ).append( hostname ).
                               append( "' >> '/etc/hosts'; " );
        }
        if ( cleanHosts.length() > 0 )
        {
            //drop pipe | symbol
            cleanHosts.setLength( cleanHosts.length() - 1 );
            cleanHosts.insert( 0, "egrep -v '" );
            cleanHosts.append( "' /etc/hosts > etc-hosts-cleaned; mv etc-hosts-cleaned /etc/hosts;" );
            appendHosts.insert( 0, cleanHosts );
        }

        appendHosts.append( "/bin/echo '127.0.0.1 localhost " ).append( getHostname() ).append( "' >> '/etc/hosts';" );

        try
        {
            execute( new RequestBuilder( appendHosts.toString() ).withTimeout( 30 ) );
        }
        catch ( CommandException e )
        {
            throw new SubutaiException( "Could not add to /etc/hosts: " + e.toString() );
        }
    }


    @Override
    public String toString()
    {
        return "SubutaiHost{" +
                "peerId=" + peerId +
                ", agent=" + agent +
                ", parentAgent=" + parentAgent +
                ", lastHeartbeat=" + lastHeartbeat +
                '}';
    }
}
