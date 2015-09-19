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
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import io.subutai.common.host.HostArchitecture;
import io.subutai.common.host.HostInfo;
import io.subutai.common.host.Interface;
import io.subutai.core.registration.api.service.ContainerInfo;


/**
 * Created by talas on 9/15/15.
 */
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

    @Column( name = "templateName" )
    private String templateName;


    @JoinColumn( name = "net_interfaces" )
    @OneToMany( orphanRemoval = true,
            targetEntity = HostInterface.class,
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER )
    private Set<Interface> netInterfaces = new HashSet<>();

    @Column( name = "arch" )
    @Enumerated( EnumType.STRING )
    private HostArchitecture arch;

    @ManyToOne
    @JoinColumn( name = "requested_host" )
    private RequestedHostImpl requestedHost;


    public ContainerInfoImpl()
    {
    }


    public ContainerInfoImpl( final String id, final String hostname, final Set<Interface> netInterfaces,
                              final HostArchitecture arch, String publicKey )
    {
        this.id = id;
        this.hostname = hostname;
        this.netInterfaces = netInterfaces;
        for ( final Interface netInterface : netInterfaces )
        {
            this.netInterfaces.add( new HostInterface( netInterface ) );
        }
        this.arch = arch;
        this.publicKey = publicKey;
        if ( arch == null )
        {
            this.arch = HostArchitecture.AMD64;
        }
    }


    public ContainerInfoImpl( ContainerInfo hostInfo )
    {
        this.id = hostInfo.getId().toString();
        this.hostname = hostInfo.getHostname();
        this.templateName = hostInfo.getTemplateName();
        this.vlan = hostInfo.getVlan();
        this.arch = hostInfo.getArch();
        this.publicKey = hostInfo.getPublicKey();
        if ( arch == null )
        {
            arch = HostArchitecture.AMD64;
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
                ", hostname='" + hostname + '\'' +
                ", netInterfaces=" + netInterfaces +
                ", arch=" + arch +
                '}';
    }
}
