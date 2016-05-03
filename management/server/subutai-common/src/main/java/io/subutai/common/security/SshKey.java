package io.subutai.common.security;


import org.codehaus.jackson.annotate.JsonProperty;


public class SshKey
{
    @JsonProperty( "hostId" )
    private String hostId;
    @JsonProperty( "encType" )
    private SshEncryptionType encryptionType;
    @JsonProperty( "pubKey" )
    private String publicKey;


    public SshKey( @JsonProperty( "hostId" ) final String hostId,
                   @JsonProperty( "encType" ) final SshEncryptionType encryptionType,
                   @JsonProperty( "pubKey" ) final String publicKey )
    {
        this.hostId = hostId;
        this.encryptionType = encryptionType;
        this.publicKey = publicKey;
    }

    public String getHostId()
    {
        return hostId;
    }


    public SshEncryptionType getEncryptionType()
    {
        return encryptionType;
    }


    public String getPublicKey()
    {
        return publicKey;
    }
}
