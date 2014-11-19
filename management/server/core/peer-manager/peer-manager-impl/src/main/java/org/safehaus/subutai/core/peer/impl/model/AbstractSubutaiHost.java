package org.safehaus.subutai.core.peer.impl.model;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.naming.NamingException;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.exception.SubutaiException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.hostregistry.api.Interface;
import org.safehaus.subutai.core.peer.api.Host;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.PeerManager;

import com.google.common.base.Preconditions;


/**
 * Base Subutai host class.
 */
@MappedSuperclass
@Access( AccessType.FIELD )
public abstract class AbstractSubutaiHost implements Host
{

    @Id
    private String id;
    @Column
    private String peerId;
    @Column
    private String hostname;
    @Column
    private String parentHostname;
    @Column
    private String netInterfaces;

    @Transient
    protected long lastHeartbeat = System.currentTimeMillis();
    @Transient
    private Set<Interface> interfaces = new HashSet<>();


    protected AbstractSubutaiHost( final Agent agent, UUID peerId )
    {
        Preconditions.checkNotNull( agent, "Agent is null" );

        //        this.agent = agent;
        this.id = agent.getUuid().toString();
        this.peerId = peerId.toString();
        this.hostname = agent.getHostname();
        this.parentHostname = agent.getParentHostName();
        StringBuilder sb = new StringBuilder();

        for ( String s : agent.getListIP() )
        {
            sb.append( s + ";" );
        }
        this.netInterfaces = sb.toString();
        //        this.id = agent.getUuid();
        //        this.hostname = agent.getHostname();
        //        this.interfaces = new HashSet<>();
    }


    protected AbstractSubutaiHost()
    {
    }

    //
    //    protected SubutaiHost( ResourceHostInfo resourceHostInfo )
    //    {
    //        Preconditions.checkNotNull( resourceHostInfo, "ResourceHostInfo is null" );
    //        hostname = resourceHostInfo.getHostname();
    //        id = resourceHostInfo.getId();
    //        interfaces = resourceHostInfo.getInterfaces();
    //    }


    //        public Agent getParentAgent()
    //        {
    //            return parentAgent;
    //        }
    //
    //
    //        public void setParentAgent( final Agent parentAgent )
    //        {
    //            this.parentAgent = parentAgent;
    //        }


    public Peer getPeer() throws PeerException
    {
        Peer result;
        try
        {
            PeerManager peerManager = ServiceLocator.getServiceNoCache( PeerManager.class );
            result = peerManager.getPeer( UUID.fromString( peerId ) );
            if ( result == null )
            {
                throw new PeerException( "Peer not registered." );
            }
        }
        catch ( NamingException e )
        {
            throw new PeerException( "Could not locate PeerManager" );
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
        return UUID.fromString( peerId );
    }


    public void setPeerId( final String peerId )
    {
        this.peerId = peerId;
    }


    @Override
    public UUID getId()
    {
        return UUID.fromString( id );
    }


    @Override
    public String getParentHostname()
    {
        return getAgent().getParentHostName();
    }


    @Override
    public String getHostname()
    {
        return getAgent().getHostname();
    }


    @Override
    public void updateHeartbeat()
    {
        lastHeartbeat = System.currentTimeMillis();
    }


    @Override
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

        final AbstractSubutaiHost that = ( AbstractSubutaiHost ) o;

        if ( !id.equals( that.id ) )
        {
            return false;
        }

        return true;
    }


    @Override
    public int hashCode()
    {
        return id != null ? id.hashCode() : 0;
    }


    public Set<Interface> getInterfaces()
    {
        return this.interfaces;
    }


    @Override
    public String getIpByMask( String mask )
    {
        String[] s = this.netInterfaces.split( ";" );
        for ( String ip : s )
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


    public Agent getAgent()
    {
        return new Agent( UUID.fromString( id ), hostname, parentHostname, null, getIps(), false, null );
    }


    private List<String> getIps()
    {
        String[] str = this.netInterfaces.split( ";" );
        List<String> result = new ArrayList<>();

        for ( String s : str )
        {
            result.add( s );
        }
        return result;
    }


    @Override
    public Agent getParentAgent()
    {
        throw new UnsupportedOperationException();
    }


    @Override
    public void setParentAgent( final Agent agent )
    {
        throw new UnsupportedOperationException();
    }


    @Override
    public String toString()
    {
        return "SubutaiHost{" +
                "peerId=" + peerId +
                ", lastHeartbeat=" + lastHeartbeat +
                '}';
    }
}
