package io.subutai.common.protocol;


import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;


public class ControlNetworkConfig
{
    @JsonProperty( "peerId" )
    private String peerId;
    @JsonProperty( "p2pHash" )
    private String p2pHash;
    @JsonProperty( "address" )
    private String address;
    @JsonProperty( "secretKey" )
    private byte[] secretKey;
    @JsonProperty( "secretKeyTtlSec" )
    private long secretKeyTtlSec;
    @JsonProperty( "usedNetworks" )
    private List<String> usedNetworks;


    public ControlNetworkConfig( @JsonProperty( "peerId" ) final String peerId,
                                 @JsonProperty( "address" ) final String address,
                                 @JsonProperty( "p2pHash" ) final String p2pHash,
                                 @JsonProperty( "secretKey" ) final byte[] secretKey,
                                 @JsonProperty( "secretKeyTtlSec" ) final long secretKeyTtlSec,
                                 @JsonProperty( "usedNetworks" ) final List<String> usedNetworks )
    {
        this.peerId = peerId;
        this.address = address;
        this.p2pHash = p2pHash;
        this.secretKey = secretKey;
        this.secretKeyTtlSec = secretKeyTtlSec;
        this.usedNetworks = usedNetworks;
    }


    public ControlNetworkConfig( final String peerId, final String address, final String p2pHash,
                                 final List<String> usedNetworks )
    {
        this.peerId = peerId;
        this.address = address;
        this.p2pHash = p2pHash;
        this.usedNetworks = usedNetworks;
    }


    public ControlNetworkConfig( final String peerId, final String address, final String p2pHash,
                                 final byte[] secretKey, final long secretKeyTtlSec )
    {
        this.peerId = peerId;
        this.address = address;
        this.p2pHash = p2pHash;
        this.secretKey = secretKey;
        this.secretKeyTtlSec = secretKeyTtlSec;
    }


    public String getPeerId()
    {
        return peerId;
    }


    public String getAddress()
    {
        return address;
    }


    public void setAddress( final String address )
    {
        this.address = address;
    }


    public String getP2pHash()
    {
        return p2pHash;
    }


    public byte[] getSecretKey()
    {
        return secretKey;
    }


    public long getSecretKeyTtlSec()
    {
        return secretKeyTtlSec;
    }


    public List<String> getUsedNetworks()
    {
        return usedNetworks;
    }
}
