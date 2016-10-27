package io.subutai.common.host;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.gson.annotations.SerializedName;


/**
 * Host interface
 */
public class HostInterfaceModel implements HostInterface
{
    @SerializedName( "interfaceName" )
    @JsonProperty( "interfaceName" )
    private String name;
    @SerializedName( "ip" )
    @JsonProperty( "ip" )
    private String ip;


    public HostInterfaceModel()
    {
    }


    public HostInterfaceModel( final String name, final String ip )
    {
        this.name = name;
        this.ip = ip;
    }


    public HostInterfaceModel( final HostInterface hostInterface )
    {
        this.name = hostInterface.getName();
        this.ip = hostInterface.getIp();
    }


    @Override
    public String getName()
    {
        return name;
    }


    @Override
    public String getIp()
    {
        return ip;
    }


    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper( this ).add( "name", name ).add( "ip", ip ).toString();
    }
}
