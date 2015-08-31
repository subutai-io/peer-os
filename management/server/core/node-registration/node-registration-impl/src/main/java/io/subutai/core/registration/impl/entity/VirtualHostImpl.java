package io.subutai.core.registration.impl.entity;


import java.util.Set;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.common.collect.Sets;

import io.subutai.common.host.HostArchitecture;
import io.subutai.common.host.Interface;
import io.subutai.core.registration.api.service.VirtualHost;


/**
 * Created by talas on 8/24/15.
 */
@Entity
@Table( name = "resource_host_containers" )
@Access( AccessType.FIELD )
public class VirtualHostImpl implements VirtualHost
{
    @Id
    @Column( name = "container_id" )
    private String id;

    @Column( name = "hostname" )
    private String hostname;

//    @Column( name = "interfaces" )
//    @OneToMany( targetEntity = InterfaceModel.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL,
//            orphanRemoval = true )
//    private Set<Interface> interfaces;

    @Column( name = "arch" )
    @Enumerated( EnumType.STRING )
    private HostArchitecture arch;


    public VirtualHostImpl()
    {
    }


    public VirtualHostImpl( final String id, final String hostname, final HostArchitecture arch )
    {
        this.id = id;
        this.hostname = hostname;
        this.arch = arch;
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
        return Sets.newHashSet();
//        return interfaces;
    }


    @Override
    public HostArchitecture getArch()
    {
        return arch;
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof VirtualHostImpl ) )
        {
            return false;
        }

        final VirtualHostImpl that = ( VirtualHostImpl ) o;

        return id.equals( that.id );
    }


    @Override
    public int hashCode()
    {
        return id.hashCode();
    }


    @Override
    public String toString()
    {
        return "VirtualHostImpl{" +
                "id='" + id + '\'' +
                ", hostname='" + hostname + '\'' +
//                ", interfaces=" + interfaces +
                ", arch=" + arch +
                '}';
    }
}
