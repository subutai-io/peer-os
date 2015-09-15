package io.subutai.core.registration.impl.entity;


import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

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
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.google.common.collect.Sets;

import io.subutai.common.host.HostArchitecture;
import io.subutai.common.host.HostInfo;
import io.subutai.common.host.Interface;
import io.subutai.core.registration.api.RegistrationStatus;
import io.subutai.core.registration.api.service.RequestedHost;


/**
 * Created by talas on 8/24/15.
 */
@Entity
@Table( name = "node_resource_host_requests" )
@Access( AccessType.FIELD )
public class RequestedHostImpl implements RequestedHost, Serializable
{
    @Id
    @Column( name = "host_id", nullable = false )
    private String id;

    @Column( name = "hostname" )
    private String hostname;

    //    private InterfaceModel interfaceModel;


    //    @Column( name = "interface_model" )
    //    @OneToOne( fetch = FetchType.EAGER, cascade = CascadeType.ALL )
    @JoinColumn( name = "net_interfaces" )
    @OneToMany( orphanRemoval = true,
            targetEntity = HostInterface.class,
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER )
    private Set<Interface> netInterfaces = Sets.newHashSet();

    @Column( name = "arch" )
    @Enumerated( EnumType.STRING )
    private HostArchitecture arch;

    @Column( name = "secret" )
    private String secret;

    @Lob
    @Column( name = "public_key" )
    private String publicKey;

    @Column( name = "rest_hook" )
    private String restHook;

    @Column( name = "status" )
    @Enumerated( EnumType.STRING )
    private RegistrationStatus status;

    @OneToMany( targetEntity = ContainerHostInfoModel.class,
            mappedBy = "requestedHost",
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER,
            orphanRemoval = true )
    private Set<HostInfo> hostInfoSet = Sets.newHashSet();


    public RequestedHostImpl()
    {
    }


    public RequestedHostImpl( final String id, final String hostname, final HostArchitecture arch, final String secret,
                              final String publicKey, final String restHook, final RegistrationStatus status,
                              Set<Interface> netInterfaces )
    {
        this.id = id;
        this.hostname = hostname;
        this.arch = arch;
        this.secret = secret;
        this.publicKey = publicKey;
        this.restHook = restHook;
        this.status = status;

        for ( final Interface anInterface : netInterfaces )
        {
            this.netInterfaces.add( new HostInterface( anInterface ) );
        }
    }


    public RequestedHostImpl( final String id, final String hostname, final HostArchitecture arch, final String secret,
                              final String publicKey, final String restHook, final RegistrationStatus status,
                              final HashSet<Interface> netInterfaces, final HashSet<HostInfo> hostInfoSet )
    {
        this.id = id;
        this.hostname = hostname;
        this.arch = arch;
        this.secret = secret;
        this.publicKey = publicKey;
        this.restHook = restHook;
        this.status = status;
        for ( final Interface anInterface : netInterfaces )
        {
            this.netInterfaces.add( new HostInterface( anInterface ) );
        }
        for ( final HostInfo hostInfo : hostInfoSet )
        {
            ContainerHostInfoModel containerHostInfoModel = new ContainerHostInfoModel( hostInfo );
            containerHostInfoModel.setRequestedHost( this );
            this.hostInfoSet.add( containerHostInfoModel );
        }
    }


    @Override
    public Set<Interface> getNetInterfaces()
    {
        return netInterfaces;
    }


    public Set<HostInfo> getHostInfoSet()
    {
        return hostInfoSet;
    }


    public void setHostInfoSet( final Set<HostInfo> hostInfoSet )
    {
        this.hostInfoSet = hostInfoSet;
    }


    @Override
    public String getId()
    {
        return id;
    }


    @Override
    public String getHostname()
    {
        return hostname;
    }


    @Override
    public HostArchitecture getArch()
    {
        return arch;
    }


    @Override
    public String getSecret()
    {
        return secret;
    }


    @Override
    public String getPublicKey()
    {
        return publicKey;
    }


    @Override
    public String getRestHook()
    {
        return restHook;
    }


    @Override
    public RegistrationStatus getStatus()
    {
        return status;
    }


    @Override
    public void setRestHook( final String restHook )
    {
        this.restHook = restHook;
    }


    public void setStatus( final RegistrationStatus status )
    {
        this.status = status;
    }


    public void setSecret( final String secret )
    {
        this.secret = secret;
    }


    public void setNetInterfaces( final Set<Interface> netInterfaces )
    {
        this.netInterfaces = netInterfaces;
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof RequestedHostImpl ) )
        {
            return false;
        }

        final RequestedHostImpl that = ( RequestedHostImpl ) o;

        return !( id != null ? !id.equals( that.id ) : that.id != null );
    }


    @Override
    public int hashCode()
    {
        return id != null ? id.hashCode() : 0;
    }


    @Override
    public String toString()
    {
        return "RequestedHostImpl{" +
                "id='" + id + '\'' +
                ", hostname='" + hostname + '\'' +
                ", netInterfaces=" + netInterfaces +
                ", arch=" + arch +
                ", secret='" + secret + '\'' +
                ", publicKey='" + publicKey + '\'' +
                ", restHook='" + restHook + '\'' +
                ", status=" + status +
                ", hostInfoSet=" + hostInfoSet +
                '}';
    }
}
