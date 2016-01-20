package io.subutai.common.protocol;


/**
 * P2P config
 */
public class P2PConfig
{
    private String peerId;
    private String interfaceName;
    private String communityName;
    private String address;
    private String secretKey;
    private String environmentId;
    private long secretKeyTtlSec;


    public P2PConfig( final String peerId, final String environmentId, final String interfaceName,
                      final String communityName, final String address, final String secretKey,
                      final long secretKeyTtlSec )
    {
        this.peerId = peerId;
        this.environmentId = environmentId;
        this.interfaceName = interfaceName;
        this.communityName = communityName;
        this.address = address;
        this.secretKey = secretKey;
        this.secretKeyTtlSec = secretKeyTtlSec;
    }


    public long getSecretKeyTtlSec()
    {
        return secretKeyTtlSec;
    }


    public String getPeerId()
    {
        return peerId;
    }


    public void setPeerId( final String peerId )
    {
        this.peerId = peerId;
    }


    public String getInterfaceName()
    {
        return interfaceName;
    }


    public void setInterfaceName( final String interfaceName )
    {
        this.interfaceName = interfaceName;
    }


    public String getCommunityName()
    {
        return communityName;
    }


    public String getAddress()
    {
        return address;
    }


    public void setAddress( final String address )
    {
        this.address = address;
    }


    public String getSecretKey()
    {
        return secretKey;
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof P2PConfig ) )
        {
            return false;
        }

        final P2PConfig config = ( P2PConfig ) o;

        return address.equals( config.address );
    }


    @Override
    public int hashCode()
    {
        return address.hashCode();
    }


    public String getEnvironmentId()
    {
        return environmentId;
    }
}
