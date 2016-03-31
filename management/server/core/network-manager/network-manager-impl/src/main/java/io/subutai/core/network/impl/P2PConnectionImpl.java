package io.subutai.core.network.impl;


import org.codehaus.jackson.annotate.JsonProperty;

import io.subutai.common.protocol.P2PConnection;


public class P2PConnectionImpl implements P2PConnection
{
    @JsonProperty( "mac" )
    private String mac;
    @JsonProperty( "ip" )
    private String ip;
    @JsonProperty( "hash" )
    private String hash;


    public P2PConnectionImpl( @JsonProperty( "mac" ) final String mac, @JsonProperty( "ip" ) final String ip,
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
