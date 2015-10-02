package io.subutai.core.peer.impl.entity;


import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.subutai.common.host.Interface;


@Entity
@Table( name = "interface" )
@IdClass( InterfaceId.class )
@Access( AccessType.FIELD )
@XmlRootElement
public class HostInterfaceImpl implements Interface, Serializable
{
    //    @Id
    //    @GeneratedValue
    //    @JsonIgnore
    //    private Long id;

    @Column( name = "name", nullable = false )
    private String interfaceName;

    @Id
    @Column( name = "ip", nullable = false )
    private String ip;

    @Id
    @Column( name = "mac", nullable = false )
    private String mac;

    @ManyToOne
    @JoinColumn( name = "host_id" )
    @JsonIgnore
    private AbstractSubutaiHost host;


    protected HostInterfaceImpl()
    {
    }


    public HostInterfaceImpl( final Interface s )
    {
        this.interfaceName = s.getInterfaceName();
        this.ip = s.getIp().replace( "addr:", "" );
        this.mac = s.getMac();
    }
//
//
//    public Long getId()
//    {
//        return id;
//    }
//
//
//    public void setId( final Long id )
//    {
//        this.id = id;
//    }
//

    @Override
    public String getInterfaceName()
    {
        return interfaceName;
    }


    public void setInterfaceName( final String interfaceName )
    {
        this.interfaceName = interfaceName;
    }


    @Override
    public String getIp()
    {
        return ip;
    }


    public void setIp( final String ip )
    {
        this.ip = ip;
    }


    @Override
    public String getMac()
    {
        return mac;
    }


    public void setMac( final String mac )
    {
        this.mac = mac;
    }


    public AbstractSubutaiHost getHost()
    {
        return host;
    }


    public void setHost( final AbstractSubutaiHost host )
    {
        this.host = host;
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

        if ( !ip.equals( that.ip ) )
        {
            return false;
        }
        return mac.equals( that.mac );
    }


    @Override
    public int hashCode()
    {
        int result = ip.hashCode();
        result = 31 * result + mac.hashCode();
        return result;
    }
}

