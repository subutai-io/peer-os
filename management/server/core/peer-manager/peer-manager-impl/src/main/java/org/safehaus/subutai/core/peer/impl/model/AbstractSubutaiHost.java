package org.safehaus.subutai.core.peer.impl.model;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.naming.NamingException;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.exception.SubutaiException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.hostregistry.api.HostInfo;
import org.safehaus.subutai.core.hostregistry.api.HostRegistry;
import org.safehaus.subutai.core.hostregistry.api.Interface;
import org.safehaus.subutai.core.peer.api.Host;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.PeerManager;

import com.google.common.base.Preconditions;


/**
 * Base Subutai host class.
 */
@Entity
@Table( name = "host" )
@Inheritance( strategy = InheritanceType.SINGLE_TABLE )
@DiscriminatorColumn( name = "host_type", discriminatorType = DiscriminatorType.STRING, length = 1 )
@Access( AccessType.FIELD )
public abstract class AbstractSubutaiHost implements Host
{
    @Id
    @Column( name = "host_id" )
    private String hostId;
    @Column( name = "peer_id", nullable = false )
    private String peerId;
    @Column( name = "host_name", nullable = false )
    private String hostname;

    @Column( name = "net_intf" )
    private String netInterfaces;

    @OneToMany( mappedBy = "host", cascade = CascadeType.ALL )
    private Set<HostInterface> interfaces = new HashSet<>();

    @Transient
    protected long lastHeartbeat = System.currentTimeMillis();


    protected AbstractSubutaiHost( final String peerId, final HostInfo hostInfo )
    {
        Preconditions.checkNotNull( hostInfo, "HostInfo is null" );
        Preconditions.checkNotNull( peerId, "PeerId is null" );

        this.hostId = hostInfo.getId().toString();
        this.peerId = peerId;
        this.hostname = hostInfo.getHostname();

        StringBuilder sb = new StringBuilder();

        for ( Interface s : hostInfo.getInterfaces() )
        {
            sb.append( s.getIp() + ";" );
            addInterface( new HostInterface( s ) );
        }
        this.netInterfaces = sb.toString();
    }


    protected AbstractSubutaiHost()
    {
    }


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


    public HostRegistry getHostRegistry() throws PeerException
    {
        HostRegistry result;
        try
        {
            return ServiceLocator.getServiceNoCache( HostRegistry.class );
        }
        catch ( NamingException e )
        {
            throw new PeerException( "Could not locate PeerManager" );
        }
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
    public UUID getId()
    {
        return UUID.fromString( hostId );
    }


    @Override
    public String getPeerId()
    {
        return peerId;
    }


    public void setPeerId( final String peerId )
    {
        this.peerId = peerId;
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

        if ( !hostId.equals( that.hostId ) )
        {
            return false;
        }

        return true;
    }


    @Override
    public int hashCode()
    {
        return hostId != null ? hostId.hashCode() : 0;
    }


    public Set<HostInterface> getInterfaces()
    {
        return this.interfaces;
    }


    public void setInterfaces( final Set<HostInterface> interfaces )
    {
        this.interfaces = interfaces;
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
        return new Agent( getId(), hostname, null, null, getIps(), false, null );
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


    public String getHostId()
    {
        return hostId;
    }


    public void setHostId( final String hostId )
    {
        this.hostId = hostId;
    }


    public void setHostname( final String hostname )
    {
        this.hostname = hostname;
    }


    public void addInterface( HostInterface hostInterface )
    {
        if ( hostInterface == null )
        {
            throw new IllegalArgumentException( "HostInterface could not be null." );
        }

        hostInterface.setHost( this );
        interfaces.add( hostInterface );
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
