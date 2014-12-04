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
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.exception.SubutaiException;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.hostregistry.api.HostArchitecture;
import org.safehaus.subutai.core.hostregistry.api.HostInfo;
import org.safehaus.subutai.core.hostregistry.api.HostRegistry;
import org.safehaus.subutai.core.hostregistry.api.Interface;
import org.safehaus.subutai.core.peer.api.Host;
import org.safehaus.subutai.core.peer.api.HostEvent;
import org.safehaus.subutai.core.peer.api.HostEventListener;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerException;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;


/**
 * Base Subutai host class.
 */
@Entity
@Inheritance( strategy = InheritanceType.TABLE_PER_CLASS )
@Access( AccessType.FIELD )
public abstract class AbstractSubutaiHost implements Host
{
    @Id
    @Column( name = "host_id" )
    protected String hostId;

    @Column( name = "peer_id", nullable = false )
    protected String peerId;

    @Column( name = "host_name", nullable = false )
    protected String hostname;

    @Column( name = "arch" )
    @Enumerated
    private HostArchitecture hostArchitecture;

    @Column( name = "net_intf" )
    private String netInterfaces;

    @OneToMany( mappedBy = "host", fetch = FetchType.EAGER, cascade = CascadeType.ALL, targetEntity = HostInterface
            .class )
    protected Set<Interface> interfaces = new HashSet<>();

    @Transient
    protected volatile long lastHeartbeat = 0;
    @Transient
    protected Set<HostEventListener> eventListeners = Sets.newConcurrentHashSet();

    @Transient
    private Peer peer;


    @Override
    public void init()
    {
        // Empty method
    }


    protected AbstractSubutaiHost( final String peerId, final HostInfo hostInfo )
    {
        Preconditions.checkNotNull( hostInfo, "Host information is null" );
        Preconditions.checkNotNull( peerId, "Peer ID is null" );

        this.peerId = peerId;
        this.hostId = hostInfo.getId().toString();
        this.hostname = hostInfo.getHostname();

        StringBuilder sb = new StringBuilder();

        for ( Interface s : hostInfo.getInterfaces() )
        {
            sb.append( s.getIp().replace( "addr:", "" ) + ";" );
            addInterface( new HostInterface( s ) );
        }
        this.netInterfaces = sb.toString();
    }


    protected AbstractSubutaiHost()
    {
    }


    @Override
    public void addListener( HostEventListener hostEventListener )
    {
        this.eventListeners.add( hostEventListener );
    }


    @Override
    public void removeListener( HostEventListener hostEventListener )
    {
        this.eventListeners.remove( hostEventListener );
    }


    @Override
    public void fireEvent( HostEvent hostEvent )
    {
        for ( HostEventListener hostEventListener : eventListeners )
        {
            try
            {
                hostEventListener.onHostEvent( hostEvent );
            }
            catch ( Exception e )
            {
                eventListeners.remove( hostEventListener );
            }
        }
    }


    @Override
    public Peer getPeer()
    {
        return peer;
        //        Peer result;
        //        try
        //        {
        //            PeerManager peerManager = ServiceLocator.getServiceNoCache( PeerManager.class );
        //            result = peerManager.getPeer( UUID.fromString( peerId ) );
        //            if ( result == null )
        //            {
        //                throw new PeerException( "Peer not registered." );
        //            }
        //        }
        //        catch ( NamingException e )
        //        {
        //            throw new PeerException( "Could not locate PeerManager" );
        //        }
        //
        //        return result;
    }


    @Override
    public void setPeer( Peer peer )
    {
        this.peer = peer;
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
        return peer.execute( requestBuilder, this, callback );
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
        peer.executeAsync( requestBuilder, this, callback );
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
    public String getHostname()
    {
        return hostname;
    }


    @Override
    public void updateHostInfo( final HostInfo hostInfo )
    {
        lastHeartbeat = System.currentTimeMillis();
    }


    @Override
    public boolean isConnected()
    {
        return peer.isConnected( this );
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


    @Override
    public Set<Interface> getNetInterfaces()
    {
        return this.interfaces;
    }


    @Override
    public String getIpByInterfaceName( String interfaceName )
    {
        for ( Interface iface : interfaces )
        {
            if ( iface.getInterfaceName().equalsIgnoreCase( interfaceName ) )
            {
                return iface.getIp();
            }
        }

        return null;
    }


    @Override
    public String getMacByInterfaceName( final String interfaceName )
    {
        for ( Interface iface : interfaces )
        {
            if ( iface.getInterfaceName().equalsIgnoreCase( interfaceName ) )
            {
                return iface.getMac();
            }
        }

        return null;
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
            execute( new RequestBuilder( String.format( "sh -c echo `%s`", appendHosts.toString() ) )
                    .withTimeout( 30 ) );
        }
        catch ( CommandException e )
        {
            throw new SubutaiException( "Could not add to /etc/hosts: " + e.toString() );
        }
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
    public HostArchitecture getHostArchitecture()
    {
        return this.hostArchitecture;
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
