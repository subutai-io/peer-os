package io.subutai.common.protocol;


import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * N2N config
 */
@XmlRootElement
@Embeddable
public class N2NConfig
{
    @Column( name = "n2n_peer_id" )
    private String peerId;
    @Column( name = "n2n_supernode" )
    private String superNodeIp;
    @Column( name = "n2n_supernode_port" )
    private int n2NPort;
    @Column( name = "n2n_interface_name" )
    private String interfaceName;
    @Column( name = "n2n_community_name" )
    private String communityName;
    @Column( name = "n2n_address" )
    private String address;
    @Transient
    private String sharedKey;


    public N2NConfig()
    {
    }


    public N2NConfig( final String peerId, final String superNodeIp, final int n2nPort, final String interfaceName,
                      final String communityName, final String address, final String sharedKey )
    {
        this.peerId = peerId;
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
}
