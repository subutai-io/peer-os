package io.subutai.common.protocol;


import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;


public class ControlNetworkConfig
{
    @JsonProperty( "peerId" )
    private String peerId;
    @JsonProperty( "fingerprint" )
    private String fingerprint;
    @JsonProperty( "address" )
    private String address;
    @JsonProperty( "secretKey" )
    private String secretKey;
    @JsonProperty( "secretKeyTtlSec" )
    private long secretKeyTtlSec;
    @JsonProperty( "usedNetworks" )
    private List<String> usedNetworks;


    public ControlNetworkConfig( @JsonProperty( "peerId" ) final String peerId,
                                 @JsonProperty( "address" ) final String address,
                                 @JsonProperty( "fingerprint" ) final String fingerprint,
                                 @JsonProperty( "secretKey" ) final String secretKey,
                                 @JsonProperty( "secretKeyTtlSec" ) final long secretKeyTtlSec,
                                 @JsonProperty( "usedNetworks" ) final List<String> usedNetworks )
    {
        this.peerId = peerId;
        this.address = address;
        this.fingerprint= fingerprint;
        this.secretKey = secretKey;
        this.secretKeyTtlSec = secretKeyTtlSec;
        this.usedNetworks = usedNetworks;
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


    public String getFingerprint()
    {
        return fingerprint;
    }


    public String getSecretKey()
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
