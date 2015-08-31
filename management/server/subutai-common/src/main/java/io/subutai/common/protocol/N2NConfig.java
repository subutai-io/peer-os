package io.subutai.common.protocol;


import javax.xml.bind.annotation.XmlRootElement;


/**
 * Created by tzhamakeev on 8/31/15.
 */
@XmlRootElement
public class N2NConfig
{
    private String superNodeIp;
    private int n2NPort;
    private String interfaceName;
    private String communityName;
    private String address;
    private String sharedKey;


    private N2NConfig()
    {
    }


    public N2NConfig( final String superNodeIp, final int n2nPort, final String interfaceName,
                      final String communityName, final String address, final String sharedKey )
    {
        this.superNodeIp = superNodeIp;
        this.n2NPort = n2nPort;
        this.interfaceName = interfaceName;
        this.communityName = communityName;
        this.address = address;
        this.sharedKey = sharedKey;
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


    public void setN2nPort( final int n2nPort )
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
}
