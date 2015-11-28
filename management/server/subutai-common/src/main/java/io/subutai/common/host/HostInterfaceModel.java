package io.subutai.common.host;


import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.base.MoreObjects;
import com.google.gson.annotations.SerializedName;


/**
 * Host interface
 */
public class HostInterfaceModel implements HostInterface
{
    @SerializedName( "name" )
    @JsonProperty( "name" )
    private String interfaceName;
    @SerializedName( "ip" )
    @JsonProperty( "ip" )
    private String ip;
    @SerializedName( "mac" )
    @JsonProperty( "mac" )
    private String mac;


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


    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper( this ).add( "interfaceName", interfaceName ).add( "ip", ip )
                          .add( "mac", mac ).toString();
    }
}
