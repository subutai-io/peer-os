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


/**
 * Created by talas on 8/25/15.
 */
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

    @Column( name = "mac" )
    private String mac;


    public HostInterfaceImpl()
    {

    }


    public HostInterfaceImpl( final String interfaceName, final String ip, final String mac )
    {
        this.interfaceName = interfaceName;
        this.ip = ip;
        this.mac = mac;
    }


    public HostInterfaceImpl( final io.subutai.common.host.HostInterface aHostInterface )
    {
        Preconditions.checkNotNull( aHostInterface, "Invalid null argument aInterface" );

        this.interfaceName = aHostInterface.getName();
        this.ip = aHostInterface.getIp();
        this.mac = aHostInterface.getMac();
    }


    public void setInterfaceName( final String interfaceName )
    {
        this.interfaceName = interfaceName;
    }


    public void setIp( final String ip )
    {
        this.ip = ip;
    }


    public void setMac( final String mac )
    {
        this.mac = mac;
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
    public String getMac()
    {
        return mac;
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

        return interfaceName.equals( that.interfaceName ) && ip.equals( that.ip ) && mac.equals( that.mac );
    }


    @Override
    public int hashCode()
    {
        int result = interfaceName.hashCode();
        result = 31 * result + ip.hashCode();
        result = 31 * result + mac.hashCode();
        return result;
    }


    @Override
    public String toString()
    {
        return "InterfaceModel{" +
                "interfaceName='" + interfaceName + '\'' +
                ", ip='" + ip + '\'' +
                ", mac='" + mac + '\'' +
                '}';
    }
}
