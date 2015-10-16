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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import io.subutai.common.host.HostArchitecture;
import io.subutai.common.host.HostInfo;
import io.subutai.common.host.Interface;
import io.subutai.core.registration.api.RegistrationStatus;
import io.subutai.core.registration.api.service.ContainerInfo;


@Entity
@Access( AccessType.FIELD )
@Table( name = "node_container_host_model" )
public class ContainerInfoImpl implements ContainerInfo, Serializable, HostInfo
{
    @Id
    @Column( name = "id" )
    private String id;

    @Lob
    @Column
    private String publicKey;

    @Column( name = "hostname" )
    private String hostname;

    @Column( name = "vlan" )
    private Integer vlan;

    @Column( name = "gateway" )
    private String gateway = "";

    @Column( name = "templateName" )
    private String templateName;


    @JoinColumn( name = "net_interfaces" )
    @OneToMany( orphanRemoval = true,
            targetEntity = io.subutai.core.registration.impl.entity.HostInterface.class,
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER )
    private Set<Interface> netInterfaces = new HashSet<>();

    @Column( name = "arch" )
    @Enumerated( EnumType.STRING )
    private HostArchitecture arch = HostArchitecture.AMD64;

    @ManyToOne
    @JoinColumn( name = "requested_host" )
    private RequestedHostImpl requestedHost;

    @Column( name = "status" )
    @Enumerated( EnumType.STRING )
    private RegistrationStatus status = RegistrationStatus.REQUESTED;


    public ContainerInfoImpl()
    {
    }


    public ContainerInfoImpl( ContainerInfo hostInfo )
    {
        this.id = hostInfo.getId().toString();
        this.hostname = hostInfo.getHostname();
        this.templateName = hostInfo.getTemplateName();
        this.vlan = hostInfo.getVlan();
        this.arch = hostInfo.getArch();
        this.status = hostInfo.getStatus();
        this.publicKey = hostInfo.getPublicKey();
        this.gateway = hostInfo.getGateway();
        if ( arch == null )
        {
            arch = HostArchitecture.AMD64;
        }
        for ( Interface anInterface : hostInfo.getInterfaces() )
        {
            this.netInterfaces.add( new io.subutai.core.registration.impl.entity.HostInterface( anInterface ) );
        }
    }


    @Override
    public RegistrationStatus getStatus()
    {
        return status;
    }


    public void setStatus( final RegistrationStatus status )
    {
        this.status = status;
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
    public Set<Interface> getInterfaces()
    {
        return netInterfaces;
    }


    @Override
    public HostArchitecture getArch()
    {
        return arch;
    }


    @Override
    public String getTemplateName()
    {
        return templateName;
    }


    public void setTemplateName( final String templateName )
    {
        this.templateName = templateName;
    }


    @Override
    public String getPublicKey()
    {
        return publicKey;
    }


    public void setPublicKey( final String publicKey )
    {
        this.publicKey = publicKey;
    }


    @Override
    public Integer getVlan()
    {
        return vlan;
    }


    @Override
    public String getGateway()
    {
        return gateway;
    }


    public void setGateway( final String gateway )
    {
        this.gateway = gateway;
    }


    @Override
    public int compareTo( final HostInfo o )
    {
        return hostname.compareTo( o.getHostname() );
    }


    public void setVlan( final Integer vlan )
    {
        this.vlan = vlan;
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof ContainerInfoImpl ) )
        {
            return false;
        }

        final ContainerInfoImpl that = ( ContainerInfoImpl ) o;

        return arch == that.arch && hostname.equals( that.hostname ) && id.equals( that.id )
                && netInterfaces.equals( that.netInterfaces );
    }


    @Override
    public int hashCode()
    {
        return id.hashCode();
    }


    @Override
    public String toString()
    {
        return "ContainerInfoImpl{" +
                "id='" + id + '\'' +
                ", publicKey='" + publicKey + '\'' +
                ", hostname='" + hostname + '\'' +
                ", vlan=" + vlan +
                ", templateName='" + templateName + '\'' +
                ", netInterfaces=" + netInterfaces +
                ", arch=" + arch +
                '}';
    }
}
