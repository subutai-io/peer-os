package io.subutai.core.registration.impl.resource.entity;


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
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.google.common.collect.Sets;

import io.subutai.common.host.HostArchitecture;
import io.subutai.common.host.Interface;
import io.subutai.core.registration.api.RegistrationStatus;
import io.subutai.core.registration.api.resource.host.RequestedHost;
import io.subutai.core.registration.api.resource.host.VirtualHost;


/**
 * Created by talas on 8/24/15.
 */
@Entity
@Table( name = "resource_host_requests" )
@Access( AccessType.FIELD )
public class RequestedHostImpl implements RequestedHost
{
    @Id
    @Column( name = "host_id", nullable = false )
    private String id;

    @Column( name = "hostname" )
    private String hostname;

    //    private InterfaceModel interfaceModel;


    //    @Column( name = "interface_model" )
    //    @OneToOne( fetch = FetchType.EAGER, cascade = CascadeType.ALL )
    @JoinColumn( name = "raw_interfaces" )
    @OneToMany( orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER )
    private Set<HostInterface> interfaces = Sets.newHashSet();
    //

    //    @JoinColumn( name = "requested_host_id" )
    //    @OneToMany( targetEntity = VirtualHostImpl.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL,
    //            orphanRemoval = true )
    //    private Set<VirtualHostImpl> containers = Sets.newHashSet();

    @Column( name = "arch" )
    @Enumerated( EnumType.STRING )
    private HostArchitecture arch;

    @Column( name = "public_key" )
    private String publicKey;

    @Column( name = "rest_hook" )
    private String restHook;

    @Column( name = "status" )
    @Enumerated( EnumType.STRING )
    private RegistrationStatus status;


    public RequestedHostImpl()
    {
    }


    public RequestedHostImpl( final String id, final String hostname, final HostArchitecture arch,
                              final String publicKey, final String restHook, final RegistrationStatus status )
    {
        this.id = id;
        this.hostname = hostname;
        this.arch = arch;
        this.publicKey = publicKey;
        this.restHook = restHook;
        this.status = status;
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
        Set<Interface> temp = Sets.newHashSet();
        temp.addAll( interfaces );
        return temp;
        //        return Sets.newHashSet();
    }


    public void setInterfaces( final Set<HostInterface> interfaces )
    {
        this.interfaces = interfaces;
    }


    @Override
    public Set<VirtualHost> getContainers()
    {
        //        Set<VirtualHost> temp = Sets.newHashSet();
        //        temp.addAll( containers );
        //        return temp;
        return Sets.newHashSet();
        //        return containers;
    }


    public void setContainers( final Set<VirtualHostImpl> containers )
    {
        //        this.containers = containers;
    }


    @Override
    public HostArchitecture getArch()
    {
        return arch;
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


    public void setStatus( final RegistrationStatus status )
    {
        this.status = status;
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
                ", interfaces=" + interfaces +
                //                ", containers=" + containers +
                ", arch=" + arch +
                ", publicKey='" + publicKey + '\'' +
                ", restHook='" + restHook + '\'' +
                ", status=" + status +
                '}';
    }
}
