package io.subutai.common.host;


import org.codehaus.jackson.annotate.JsonProperty;

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
    @SerializedName( "mac" )
    @JsonProperty( "mac" )
    private String mac;


    public HostInterfaceModel()
    {
    }


    public HostInterfaceModel( final String name, final String ip, final String mac )
    {
        this.name = name;
        this.ip = ip;
        this.mac = mac;
    }


    public HostInterfaceModel( final HostInterface hostInterface )
    {
        this.name = hostInterface.getName();
        this.ip = hostInterface.getIp();
        this.mac = hostInterface.getMac();
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
    public String getMac()
    {
        return mac;
    }


    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper( this ).add( "name", name ).add( "ip", ip ).add( "mac", mac ).toString();
    }
}
