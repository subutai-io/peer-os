package io.subutai.core.lxc.quota.impl.entity;


import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table( name = "quota" )
@Access( AccessType.FIELD )
public class QuotaEntity implements Serializable
{
    @Id
    @Column( name = "id" )
    private String containerId;
    private String peerId;
    private String envId;
    private BigDecimal cpu;
    private BigDecimal ram;
    private BigDecimal rootfs;
    private BigDecimal home;
    private BigDecimal opt;
    private BigDecimal var;


    public QuotaEntity( final String peerId, final String environmentId, final String containerId, final BigDecimal cpu,
                        final BigDecimal ram, final BigDecimal rootfs, final BigDecimal home, final BigDecimal opt,
                        final BigDecimal var )
    {
        this.containerId = containerId;
        this.peerId = peerId;
        this.envId = environmentId;
        this.cpu = cpu;
        this.ram = ram;
        this.rootfs = rootfs;
        this.home = home;
        this.opt = opt;
        this.var = var;
    }


    public String getContainerId()
    {
        return containerId;
    }


    public String getPeerId()
    {
        return peerId;
    }


    public String getEnvId()
    {
        return envId;
    }


    public BigDecimal getCpu()
    {
        return cpu;
    }


    public void setCpu( final BigDecimal cpu )
    {
        this.cpu = cpu;
    }


    public BigDecimal getRam()
    {
        return ram;
    }


    public void setRam( final BigDecimal ram )
    {
        this.ram = ram;
    }


    public BigDecimal getRootfs()
    {
        return rootfs;
    }


    public void setRootfs( final BigDecimal rootfs )
    {
        this.rootfs = rootfs;
    }


    public BigDecimal getHome()
    {
        return home;
    }


    public void setHome( final BigDecimal home )
    {
        this.home = home;
    }


    public BigDecimal getOpt()
    {
        return opt;
    }


    public void setOpt( final BigDecimal opt )
    {
        this.opt = opt;
    }


    public BigDecimal getVar()
    {
        return var;
    }


    public void setVar( final BigDecimal var )
    {
        this.var = var;
    }
}
