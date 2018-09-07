package io.subutai.bazaar.share.dto;


public class PublicKeyContainer
{
    private String hostId;
    private byte[] fingerprint;
    private String key;


    public PublicKeyContainer()
    {
    }


    public PublicKeyContainer( final String hostId, final byte[] fingerprint, final String key )
    {
        this.hostId = hostId;
        this.fingerprint = fingerprint;
        this.key = key;
    }


    public String getHostId()
    {
        return hostId;
    }


    public void setHostId( final String hostId )
    {
        this.hostId = hostId;
    }


    public byte[] getFingerprint()
    {
        return fingerprint;
    }


    public void setFingerprint( final byte[] fingerprint )
    {
        this.fingerprint = fingerprint;
    }


    public String getKey()
    {
        return key;
    }


    public void setKey( final String key )
    {
        this.key = key;
    }
}
