package io.subutai.common.protocol;


import javax.xml.bind.annotation.XmlRootElement;


/**
 * N2N config
 */
@XmlRootElement
public class N2NConfig
{
    private String peerId;
    private String superNodeIp;
    private int n2NPort;
    private String interfaceName;
    private String communityName;
    private String address;
    private String sharedKey;
    private String environmentId;


    public N2NConfig()
    {
    }


    public N2NConfig( final String peerId, final String environmentId, final String superNodeIp, final int n2nPort,
                      final String interfaceName, final String communityName, final String address,
                      final String sharedKey )
    {
        this.peerId = peerId;
        this.environmentId = environmentId;
        this.superNodeIp = superNodeIp;
        this.n2NPort = n2nPort;
        this.interfaceName = interfaceName;
        this.communityName = communityName;
        this.address = address;
        this.sharedKey = sharedKey;
    }


    public N2NConfig( final String address, final String interfaceName, final String communityName )
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


    public void setSuperNodeIp( final String superNodeIp )
    {
        this.superNodeIp = superNodeIp;
    }


    public int getN2NPort()
    {
        return n2NPort;
    }


    public void setN2NPort( final int n2nPort )
    {
        this.n2NPort = n2nPort;
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


    public void setCommunityName( final String communityName )
    {
        this.communityName = communityName;
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


    public void setSharedKey( final String sharedKey )
    {
        this.sharedKey = sharedKey;
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof N2NConfig ) )
        {
            return false;
        }

        final N2NConfig config = ( N2NConfig ) o;

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
