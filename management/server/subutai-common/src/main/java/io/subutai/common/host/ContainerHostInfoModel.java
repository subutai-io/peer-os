package io.subutai.common.host;


import org.codehaus.jackson.annotate.JsonProperty;

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


    public ContainerHostInfoModel( @JsonProperty( "id" ) final String id,
                                   @JsonProperty( "hostname" ) final String hostname,
                                   @JsonProperty( "containerName" ) final String containerName,
                                   @JsonProperty( "interfaces" ) final HostInterfaces hostInterfaces,
                                   @JsonProperty( "arch" ) final HostArchitecture hostArchitecture,
                                   @JsonProperty( "status" ) final ContainerHostState state )
    {
        super( id, hostname, hostInterfaces, hostArchitecture );
        this.state = state;
        this.name = containerName;
    }


    public ContainerHostInfoModel( final ContainerHostInfo info )
    {
        super( info );
        this.state = info.getState();
        this.name = info.getContainerName();
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
                          .toString();
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
