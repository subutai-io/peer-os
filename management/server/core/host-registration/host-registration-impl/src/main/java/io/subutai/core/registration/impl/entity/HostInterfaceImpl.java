package io.subutai.core.registration.impl.entity;


import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.common.base.Preconditions;

import io.subutai.common.host.HostInterface;


@Entity
@Table( name = "node_net_interfaces" )
@Access( AccessType.FIELD )
public class HostInterfaceImpl implements HostInterface, Serializable
{
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    private Long id;

    @Column( name = "i_name" )
    private String interfaceName;

    @Column( name = "ip_addr" )
    private String ip;


    public HostInterfaceImpl()
    {

    }


    public HostInterfaceImpl( final io.subutai.common.host.HostInterface aHostInterface )
    {
        Preconditions.checkNotNull( aHostInterface, "Invalid null argument aInterface" );

        this.interfaceName = aHostInterface.getName();
        this.ip = aHostInterface.getIp();
    }


    public void setInterfaceName( final String interfaceName )
    {
        this.interfaceName = interfaceName;
    }


    public void setIp( final String ip )
    {
        this.ip = ip;
    }


    public Long getId()
    {
        return id;
    }


    public void setId( final Long id )
    {
        this.id = id;
    }


    public String getInterfaceName()
    {
        return interfaceName;
    }


    @Override
    public String getName()
    {
        return interfaceName;
    }


    @Override
    public String getIp()
    {
        return ip;
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof HostInterfaceImpl ) )
        {
            return false;
        }

        final HostInterfaceImpl that = ( HostInterfaceImpl ) o;

        return interfaceName.equals( that.interfaceName ) && ip.equals( that.ip );
    }


    @Override
    public int hashCode()
    {
        int result = interfaceName.hashCode();
        result = 31 * result + ip.hashCode();
        return result;
    }


    @Override
    public String toString()
    {
        return "InterfaceModel{" +
                "interfaceName='" + interfaceName + '\'' +
                ", ip='" + ip + '\'' +
                '}';
    }
}
