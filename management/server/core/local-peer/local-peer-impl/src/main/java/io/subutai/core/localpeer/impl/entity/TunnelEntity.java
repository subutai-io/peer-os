package io.subutai.core.localpeer.impl.entity;


import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;


@Entity
@Table( name = "env_tunnel" )
@Access( AccessType.FIELD )
public class TunnelEntity implements Serializable
{
    @Id
    @Column( name = "id" )
    @GeneratedValue( strategy = GenerationType.AUTO )
    private Long id;

    @Version
    private Long version;

    @Column( name = "t_address", nullable = false )
    private String tunnelAddress;

    @Column( name = "i_name", nullable = false )
    private String interfaceName;

    @Column( name = "c_name", nullable = false )
    private String communityName;

    @Column( name = "env_id", nullable = false )
    private String environmentId;


    public String getTunnelAddress()
    {
        return tunnelAddress;
    }


    public void setTunnelAddress( final String tunnelAddress )
    {
        this.tunnelAddress = tunnelAddress;
    }


    public String getInterfaceName()
    {
        return interfaceName;
    }


    public void setInterfaceName( final String interfaceName )
    {
        this.interfaceName = interfaceName;
    }


    public String getCommunityName()
    {
        return communityName;
    }


    public void setCommunityName( final String communityName )
    {
        this.communityName = communityName;
    }


    public String getEnvironmentId()
    {
        return environmentId;
    }


    public void setEnvironmentId( final String environmentId )
    {
        this.environmentId = environmentId;
    }


    public Long getId()
    {
        return id;
    }
}
