package io.subutai.common.security;


import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.ser.std.StdArraySerializers;


/**
 * Public key container
 */
public class PublicKeyContainer
{
    @JsonProperty( "hostId" )
    private String hostId;
    @JsonProperty( "fingerprint" )
    private byte[] fingerprint;
    @JsonProperty( "key" )
    private String key;


    public PublicKeyContainer( @JsonProperty( "hostId" ) final String hostId,
                               @JsonSerialize( using = StdArraySerializers.ByteArraySerializer.class )
                               @JsonProperty( "fingerprint" ) final byte[] fingerprint,
                               @JsonProperty( "key" ) final String key )
    {
        this.hostId = hostId;
        this.fingerprint = fingerprint;
        this.key = key;
    }


    public String getHostId()
    {
        return hostId;
    }


    public byte[] getFingerprint()
    {
        return fingerprint;
    }


    public String getKey()
    {
        return key;
    }
}
