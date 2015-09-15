package io.subutai.core.registration.impl.entity;


import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import io.subutai.common.host.HostArchitecture;
import io.subutai.common.host.HostInfo;
import io.subutai.common.host.Interface;


/**
 * Created by talas on 9/15/15.
 */
@Entity
@Access( AccessType.FIELD )
@Table( name = "node_container_host_model" )
public class ContainerHostInfoModel implements HostInfo, Serializable
{
    @Id
    @Column( name = "id" )
    private String id;

    @Column( name = "hostname" )
    private String hostname;


    @JoinColumn( name = "net_interfaces" )
    @OneToMany( orphanRemoval = true,
            targetEntity = HostInterface.class,
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER )
    private Set<Interface> netInterfaces = new HashSet<>();

    @Column( name = "arch" )
    @Enumerated( EnumType.STRING )
    private HostArchitecture hostArchitecture;

    @ManyToOne
    @JoinColumn( name = "requested_host" )
    private RequestedHostImpl requestedHost;


    public ContainerHostInfoModel( final String id, final String hostname, final Set<Interface> netInterfaces,
                                   final HostArchitecture hostArchitecture )
    {
        this.id = id;
        this.hostname = hostname;
        this.netInterfaces = netInterfaces;
        for ( final Interface netInterface : netInterfaces )
        {
            this.netInterfaces.add( new HostInterface( netInterface ) );
        }
        this.hostArchitecture = hostArchitecture;
        if ( hostArchitecture == null )
        {
            this.hostArchitecture = HostArchitecture.AMD64;
        }
    }


    public ContainerHostInfoModel( HostInfo hostInfo )
    {
        this.id = hostInfo.getId().toString();
        this.hostname = hostInfo.getHostname();
        this.hostArchitecture = hostInfo.getArch();
        if ( hostArchitecture == null )
        {
            hostArchitecture = HostArchitecture.AMD64;
        }
        for ( Interface anInterface : hostInfo.getInterfaces() )
        {
            this.netInterfaces.add( new HostInterface( anInterface ) );
        }
    }


    public RequestedHostImpl getRequestedHost()
    {
        return requestedHost;
    }


    public void setRequestedHost( final RequestedHostImpl requestedHost )
    {
        this.requestedHost = requestedHost;
    }


    @Override
    public UUID getId()
    {
        return UUID.fromString( id );
    }


    @Override
    public String getHostname()
    {
        return hostname;
    }


    @Override
    public Set<Interface> getInterfaces()
    {
        return netInterfaces;
    }


    @Override
    public HostArchitecture getArch()
    {
        return hostArchitecture;
    }


    @Override
    public int compareTo( final HostInfo o )
    {
        return hostname.compareTo( o.getHostname() );
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof ContainerHostInfoModel ) )
        {
            return false;
        }

        final ContainerHostInfoModel that = ( ContainerHostInfoModel ) o;

        return hostArchitecture == that.hostArchitecture && hostname.equals( that.hostname ) && id.equals( that.id )
                && netInterfaces.equals( that.netInterfaces );
    }


    @Override
    public int hashCode()
    {
        int result = id.hashCode();
        result = 31 * result + hostname.hashCode();
        result = 31 * result + netInterfaces.hashCode();
        result = 31 * result + hostArchitecture.hashCode();
        return result;
    }


    @Override
    public String toString()
    {
        return "ContainerHostInfoModel{" +
                "id='" + id + '\'' +
                ", hostname='" + hostname + '\'' +
                ", netInterfaces=" + netInterfaces +
                ", hostArchitecture=" + hostArchitecture +
                '}';
    }
}
