package io.subutai.common.peer;


import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import io.subutai.common.host.Interface;

import com.google.common.base.Preconditions;


@Entity
@Table( name = "raw_interface" )
@Access( AccessType.FIELD )
public class InterfaceModel implements Interface
{
    @Column( name = "i_name" )
    private String interfaceName;

    @Column( name = "ip_addr" )
    private String ip;

    @Id
    @Column( name = "mac" )
    private String mac;


    public InterfaceModel()
    {

    }


    public InterfaceModel( final Interface aInterface )
    {
        Preconditions.checkNotNull( aInterface, "Invalid null argument aInterface" );
        this.interfaceName = aInterface.getInterfaceName();
        this.ip = aInterface.getIp();
        this.mac = aInterface.getMac();
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


    @Override
    public String getInterfaceName()
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
        if ( !( o instanceof InterfaceModel ) )
        {
            return false;
        }

        final InterfaceModel that = ( InterfaceModel ) o;

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
}
