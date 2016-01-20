package io.subutai.common.protocol;


import javax.xml.bind.annotation.XmlRootElement;


/**
 * P2P config
 */
@XmlRootElement
public class P2PConfig
{
    private String peerId;
    private String superNodeIp;
    private int P2PPort;
    private String interfaceName;
    private String communityName;
    private String address;
    private String sharedKey;
    private String environmentId;


    public P2PConfig()
    {
    }


    public P2PConfig( final String peerId, final String environmentId, final String superNodeIp, final int p2pPort,
                      final String interfaceName, final String communityName, final String address,
                      final String sharedKey )
    {
        this.peerId = peerId;
        this.environmentId = environmentId;
        this.superNodeIp = superNodeIp;
        this.P2PPort = p2pPort;
        this.interfaceName = interfaceName;
        this.communityName = communityName;
        this.address = address;
        this.sharedKey = sharedKey;
    }


    public P2PConfig( final String address, final String interfaceName, final String communityName )
    {
        this.address = address;
        this.interfaceName = interfaceName;
        this.communityName = communityName;
    }


    public String getPeerId()
    {
        return peerId;
    }


    public void setPeerId( final String peerId )
    {
        this.peerId = peerId;
    }


    public String getSuperNodeIp()
    {
        return superNodeIp;
    }


    public int getP2PPort()
    {
        return P2PPort;
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


    public String getSharedKey()
    {
        return sharedKey;
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
