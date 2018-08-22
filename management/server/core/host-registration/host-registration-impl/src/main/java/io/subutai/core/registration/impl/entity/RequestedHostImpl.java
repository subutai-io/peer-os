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
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.google.common.collect.Sets;
import com.google.gson.annotations.Expose;

import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.HostArchitecture;
import io.subutai.common.host.HostInfo;
import io.subutai.common.host.HostInterface;
import io.subutai.common.host.InstanceType;
import io.subutai.common.host.ResourceHostInfo;
import io.subutai.common.util.CollectionUtil;
import io.subutai.core.registration.api.ResourceHostRegistrationStatus;
import io.subutai.core.registration.api.service.ContainerInfo;
import io.subutai.core.registration.api.service.RequestedHost;


@Entity
@Table( name = "node_resource_host_requests" )
@Access( AccessType.FIELD )
public class RequestedHostImpl implements RequestedHost, ResourceHostInfo, Serializable
{
    @Id
    @Column( name = "host_id", nullable = false )
    @Expose
    private String id;

    @Column( name = "hostname" )
    @Expose
    private String hostname;

    @Column( name = "address" )
    @Expose
    private String address;

    @Column( name = "instanceType" )
    @Enumerated( EnumType.STRING )
    @Expose
    private InstanceType instanceType = InstanceType.LOCAL;

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

    @Column( name = "dateUpdated" )
    private Long dateUpdated = System.currentTimeMillis();


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
        this.dateUpdated = System.currentTimeMillis();
        this.id = requestedHost.getId();
        this.hostname = requestedHost.getHostname();
        this.arch = requestedHost.getArch();
        this.secret = requestedHost.getSecret();
        this.publicKey = requestedHost.getPublicKey();
        this.status = requestedHost.getStatus();
        this.cert = requestedHost.getCert();
        this.address = requestedHost.getAddress();

        if ( this.arch == null )
        {
            this.arch = HostArchitecture.AMD64;
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


    @Deprecated
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

        if ( this.arch == null )
        {
            this.arch = HostArchitecture.AMD64;
        }
    }


    @Override
    public Set<ContainerInfo> getHostInfos()
    {
        if ( CollectionUtil.isCollectionEmpty( hostInfos ) )
        {
            return Sets.newHashSet();
        }
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


    @Override
    public Set<ContainerHostInfo> getContainers()
    {
        Set<ContainerHostInfo> containerHostInfos = Sets.newHashSet();

        containerHostInfos.addAll( hostInfos );

        return containerHostInfos;
    }


    @Override
    public InstanceType getInstanceType()
    {
        return instanceType;
    }


    @Override
    public String getAddress()
    {
        return address;
    }


    public Long getDateUpdated()
    {
        return dateUpdated;
    }


    public void refreshDateUpdated()
    {
        this.dateUpdated = System.currentTimeMillis();
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
        return "RequestedHostImpl{" + "id='" + id + '\'' + ", hostname='" + hostname + '\'' + ", address='" + address
                + '\'' + ", instanceType=" + instanceType + ", arch=" + arch + ", secret='" + secret + '\''
                + ", publicKey='" + publicKey + '\'' + ", cert='" + cert + '\'' + ", status=" + status + ", hostInfos="
                + hostInfos + '}';
    }


    @Override
    public int compareTo( final HostInfo o )
    {
        if ( hostname != null && o != null )
        {
            return hostname.compareTo( o.getHostname() );
        }
        return -1;
    }
}
