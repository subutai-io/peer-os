package org.safehaus.subutai.core.network.rest;


import org.safehaus.subutai.core.network.api.N2NConnection;


class N2NConnectionImpl implements N2NConnection
{
    private String superNodeIp;
    private int superNodePort;
    private String localIp;
    private String interfaceName;
    private String communityName;


    @Override
    public String getSuperNodeIp()
    {
        return superNodeIp;
    }


    public void setSuperNodeIp( String superNodeIp )
    {
        this.superNodeIp = superNodeIp;
    }


    @Override
    public int getSuperNodePort()
    {
        return superNodePort;
    }


    public void setSuperNodePort( int superNodePort )
    {
        this.superNodePort = superNodePort;
    }


    @Override
    public String getLocalIp()
    {
        return localIp;
    }


    public void setLocalIp( String localIp )
    {
        this.localIp = localIp;
    }


    @Override
    public String getInterfaceName()
    {
        return interfaceName;
    }


    public void setInterfaceName( String interfaceName )
    {
        this.interfaceName = interfaceName;
    }


    @Override
    public String getCommunityName()
    {
        return communityName;
    }


    public void setCommunityName( String communityName )
    {
        this.communityName = communityName;
    }
}

