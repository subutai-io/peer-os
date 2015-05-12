package org.safehaus.subutai.core.peer.impl.entity;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
import org.safehaus.subutai.common.host.HostArchitecture;
import org.safehaus.subutai.common.host.HostInfo;
import org.safehaus.subutai.common.host.Interface;
import org.safehaus.subutai.common.peer.Host;
import org.safehaus.subutai.common.peer.Peer;

import com.google.common.base.Preconditions;


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

    @OneToMany( mappedBy = "host", fetch = FetchType.EAGER, cascade = CascadeType.ALL, targetEntity = HostInterface
            .class )
    protected Set<Interface> interfaces = new HashSet<>();

    @Transient
    protected volatile long lastHeartbeat = 0;


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
        this.hostArchitecture = hostInfo.getArch();

        for ( Interface s : hostInfo.getInterfaces() )
        {
            addInterface( new HostInterface( s ) );
        }
    }


    protected AbstractSubutaiHost()
    {
    }


    @Override
    public Peer getPeer()
    {
        return peer;
    }


    public void setPeer( Peer peer )
    {
        this.peer = peer;
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


    @Override
    public String getHostname()
    {
        return hostname;
    }


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


    public String getHostId()
    {
        return hostId;
    }


    public void addInterface( HostInterface hostInterface )
    {
        Preconditions.checkNotNull( hostInterface, "HostInterface could not be null." );

        hostInterface.setHost( this );
        interfaces.add( hostInterface );
    }


    public void setNetInterfaces( Set<Interface> interfaces )
    {
        this.interfaces.clear();
        for ( Interface iface : interfaces )
        {
            addInterface( new HostInterface( iface ) );
        }
    }


    @Override
    public HostArchitecture getHostArchitecture()
    {
        return this.hostArchitecture;
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

        return getHostId().equals( that.getHostId() );
    }


    @Override
    public int hashCode()
    {
        return hostId != null ? hostId.hashCode() : 0;
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
