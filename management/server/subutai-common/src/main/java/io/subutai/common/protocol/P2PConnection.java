package io.subutai.common.protocol;


import org.codehaus.jackson.annotate.JsonProperty;


/**
 * P2P Connection
 */
public class P2PConnection
{

    @JsonProperty( "mac" )
    private String mac;
    @JsonProperty( "ip" )
    private String ip;
    @JsonProperty( "hash" )
    private String hash;


    public P2PConnection( @JsonProperty( "mac" ) final String mac, @JsonProperty( "ip" ) final String ip,
                          @JsonProperty( "hash" ) final String hash )
    {
        this.mac = mac;
        this.ip = ip;
        this.hash = hash;
    }


    public String getMac()
    {
        return mac;
    }


    public String getIp()
    {
        return ip;
    }


    public String getHash()
    {
        return hash;
    }
}
