package io.subutai.common.host;


import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.gson.annotations.SerializedName;


/**
 * Implementation of ContainerHostInfo
 */

public class ContainerHostInfoModel extends HostInfoModel implements ContainerHostInfo
{
    @SerializedName( "status" )
    @JsonProperty( "status" )
    protected ContainerHostState state;
    @SerializedName( "name" )
    @JsonProperty( "name" )
    protected String name;
    @SerializedName( "quota" )
    @JsonProperty( "quota" )
    protected Quota quota;
    @SerializedName( "interfaces" )
    @JsonProperty( "interfaces" )
    protected Set<HostInterfaceModel> hostInterfaces = new HashSet<>();
    @JsonProperty( "environmentId" )
    protected String envId;
    @JsonProperty( "vlan" )
    protected Integer vlan;


    public ContainerHostInfoModel( @JsonProperty( "id" ) final String id,
                                   @JsonProperty( "hostname" ) final String hostname,
                                   @JsonProperty( "containerName" ) final String containerName,
                                   @JsonProperty( "interfaces" ) final HostInterfaces hostInterfaces,
                                   @JsonProperty( "arch" ) final HostArchitecture hostArchitecture,
                                   @JsonProperty( "status" ) final ContainerHostState state,
                                   @JsonProperty( "envId" ) final String envId,
                                   @JsonProperty( "vlan" ) final Integer vlan )
    {
        super( id, hostname, hostArchitecture );
        this.state = state;
        this.name = containerName;
        this.envId = envId;
        this.vlan = vlan;
        setHostInterfaces( hostInterfaces );
    }


    @Override
    public Quota getRawQuota()
    {
        return quota;
    }


    @Override
    public String getEnvId()
    {
        return envId;
    }


    @Override
    public Integer getVlan()
    {
        return vlan;
    }


    @Override
    public HostInterfaces getHostInterfaces()
    {
        return new HostInterfaces( this.id, this.hostInterfaces );
    }


    public void setHostInterfaces( final HostInterfaces hostInterfaces )
    {
        this.hostInterfaces.addAll( hostInterfaces.getAll() );
    }


    public ContainerHostInfoModel( final ContainerHostInfo info )
    {
        super( info );
        this.state = info.getState();
        this.name = info.getContainerName();
        this.quota = info.getRawQuota();
    }


    @Override
    public ContainerHostState getState()
    {
        return state;
    }


    @Override
    public String getContainerName()
    {
        return name;
    }


    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper( this ).add( "id", id ).add( "hostname", hostname )
                          .add( "interfaces", hostInterfaces ).add( "status", state ).add( "arch", hostArchitecture )
                          .add( "environmentId", envId ).add( "vlan", vlan ).toString();
    }


    @Override
    public int compareTo( final HostInfo o )
    {
        if ( hostname != null && o != null )
        {
            return hostname.compareTo( o.getHostname() );
        }
        return -1;
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof ContainerHostInfoModel ) )
        {
            return false;
        }

        final ContainerHostInfoModel that = ( ContainerHostInfoModel ) o;

        if ( id != null ? !id.equals( that.id ) : that.id != null )
        {
            return false;
        }

        return true;
    }


    @Override
    public int hashCode()
    {
        return id != null ? id.hashCode() : 0;
    }
}
