package io.subutai.core.environment.impl.entity;


import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import io.subutai.common.host.HostInterface;


/**
 * {@link HostInterfaceImpl} stores host network interface information. <p> {@link #interfaceName} - host interface name</p>
 * <p> {@link #ip} - ip address</p> <p> {@link #mac} - mac address</p> <p> {@link #host} - target host whose metadata
 * being saved</p>
 *
 * @see HostInterface
 * @see EnvironmentContainerImpl
 */
@Entity
@Table( name = "env_con_intf" )
@Access( AccessType.FIELD )
public class HostInterfaceImpl implements HostInterface, Serializable
{
    @Id
    @GeneratedValue
    private Long id;

    @Column( name = "name", nullable = false )
    private String interfaceName;
    @Column( name = "ip", nullable = false )
    private String ip;
    @Column( name = "mac", nullable = false )
    private String mac;

    @ManyToOne
    @JoinColumn( name = "host_id" )
    private EnvironmentContainerImpl host;


    protected HostInterfaceImpl()
    {
    }


    public HostInterfaceImpl( final HostInterface s )
    {
        this.interfaceName = s.getName();
        this.ip = s.getIp().replace( "addr:", "" );
        this.mac = s.getMac();
    }


    public Long getId()
    {
        return id;
    }


    public void setId( final Long id )
    {
        this.id = id;
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


    public EnvironmentContainerImpl getHost()
    {
        return host;
    }


    public void setHost( final EnvironmentContainerImpl host )
    {
        this.host = host;
    }
}

