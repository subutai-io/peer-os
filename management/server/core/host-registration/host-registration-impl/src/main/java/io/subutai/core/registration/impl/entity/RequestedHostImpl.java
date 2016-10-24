package io.subutai.core.registration.impl.entity;


import java.io.Serializable;
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
import com.google.gson.annotations.Expose;

import io.subutai.common.host.HostArchitecture;
import io.subutai.common.host.HostInterface;
import io.subutai.core.registration.api.ResourceHostRegistrationStatus;
import io.subutai.core.registration.api.service.ContainerInfo;
import io.subutai.core.registration.api.service.RequestedHost;


@Entity
@Table( name = "node_resource_host_requests" )
@Access( AccessType.FIELD )
public class RequestedHostImpl implements RequestedHost, Serializable
{
    @Id
    @Column( name = "host_id", nullable = false )
    @Expose
    private String id;

    @Column( name = "hostname" )
    @Expose
    private String hostname;

    @JoinColumn( name = "net_interfaces" )
    @OneToMany( orphanRemoval = true, targetEntity = HostInterfaceImpl.class, cascade = CascadeType.ALL, fetch =
            FetchType.EAGER )
    @Expose
    private Set<HostInterface> interfaces = Sets.newHashSet();

    @Column( name = "arch" )
    @Enumerated( EnumType.STRING )
    @Expose
    private HostArchitecture arch = HostArchitecture.AMD64;

    @Column( name = "secret" )
    @Expose
    private String secret;

    @Lob
    @Column( name = "public_key" )
    @Expose
    private String publicKey;

    @Lob
    @Column( name = "cert" )
    @Expose
    private String cert;


    @Column( name = "status" )
    @Enumerated( EnumType.STRING )
    @Expose
    private ResourceHostRegistrationStatus status = ResourceHostRegistrationStatus.REQUESTED;

    @OneToMany( targetEntity = ContainerInfoImpl.class, mappedBy = "requestedHost", cascade = CascadeType.ALL, fetch
            = FetchType.EAGER, orphanRemoval = true )
    @Expose
    private Set<ContainerInfo> hostInfos = Sets.newHashSet();


    public RequestedHostImpl()
    {
    }


    public RequestedHostImpl( final RequestedHost requestedHost )
    {
        this.id = requestedHost.getId();
        this.hostname = requestedHost.getHostname();
        this.arch = requestedHost.getArch();
        this.secret = requestedHost.getSecret();
        this.publicKey = requestedHost.getPublicKey();
        this.status = requestedHost.getStatus();
        this.cert = requestedHost.getCert();

        if ( this.arch == null )
        {
            this.arch = HostArchitecture.AMD64;
        }

        Set<HostInterface> netHostInterfaces = requestedHost.getInterfaces();
        for ( final HostInterface netHostInterface : netHostInterfaces )
        {
            HostInterfaceImpl hostInterfaceImpl = new HostInterfaceImpl( netHostInterface );
            this.interfaces.add( hostInterfaceImpl );
        }

        Set<ContainerInfo> hostInfoSet = requestedHost.getHostInfos();
        for ( final ContainerInfo containerInfo : hostInfoSet )
        {
            ContainerInfoImpl containerInfoImpl = new ContainerInfoImpl( containerInfo );
            containerInfoImpl.setStatus( ResourceHostRegistrationStatus.REQUESTED );
            containerInfoImpl.setRequestedHost( this );
            this.hostInfos.add( containerInfoImpl );
        }
    }


    public RequestedHostImpl( final String id, final String hostname, final HostArchitecture arch, final String secret,
                              final String publicKey, final ResourceHostRegistrationStatus status,
                              Set<HostInterface> interfaces )
    {
        this.id = id;
        this.hostname = hostname;
        this.arch = arch;
        this.secret = secret;
        this.publicKey = publicKey;
        this.status = status;

        for ( final HostInterface anHostInterface : interfaces )
        {
            this.interfaces.add( new HostInterfaceImpl( anHostInterface ) );
        }

        if ( this.arch == null )
        {
            this.arch = HostArchitecture.AMD64;
        }
    }


    public Set<HostInterface> getInterfaces()
    {
        return interfaces;
    }


    @Override
    public Set<ContainerInfo> getHostInfos()
    {
        return hostInfos;
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


    public void setHostname( final String hostname )
    {
        this.hostname = hostname;
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
    public String getCert()
    {
        return cert;
    }


    @Override
    public ResourceHostRegistrationStatus getStatus()
    {
        return status;
    }


    public void setStatus( final ResourceHostRegistrationStatus status )
    {
        this.status = status;
    }


    public void setSecret( final String secret )
    {
        this.secret = secret;
    }


    public void setInterfaces( final Set<HostInterface> interfaces )
    {
        this.interfaces = interfaces;
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
        return "RequestedHostImpl{" + "id='" + id + '\'' + ", hostname='" + hostname + '\'' + ", status=" + status
                + ", arch=" + arch + ", secret='" + secret + '\'' + ", publicKey='" + publicKey + '\''
                + ", hostInterfaces=" + interfaces + ", hostInfos=" + hostInfos + ", cert=" + cert + '}';
    }
}
