package io.subutai.core.localpeer.impl.entity;


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
 * {@link HostInterfaceEntity} stores host network interface information. <p> {@link #interfaceName} - host interface
 * name</p> <p> {@link #ip} - ip address</p> <p> {@link #host} - target host whose metadata being saved</p>
 *
 * @see HostInterface
 * @see ContainerHostEntity
 */
@Entity
@Table( name = "net_intf" )
@Access( AccessType.FIELD )
public class HostInterfaceEntity implements HostInterface
{
    @Id
    @GeneratedValue
    private Long id;

    @Column( name = "name", nullable = false )
    private String interfaceName;
    @Column( name = "ip", nullable = false )
    private String ip;

    @ManyToOne
    @JoinColumn( name = "host_id" )
    private ContainerHostEntity host;


    protected HostInterfaceEntity()
    {
    }


    public HostInterfaceEntity( final HostInterface s )
    {
        this.interfaceName = s.getName();
        this.ip = s.getIp();
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


    public ContainerHostEntity getHost()
    {
        return host;
    }


    public void setHost( final ContainerHostEntity host )
    {
        this.host = host;
    }
}

