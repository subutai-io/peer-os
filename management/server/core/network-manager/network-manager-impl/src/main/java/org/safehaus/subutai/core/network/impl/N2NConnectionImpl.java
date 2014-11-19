package org.safehaus.subutai.core.network.impl;


import org.safehaus.subutai.core.network.api.N2NConnection;


/**
 * N2NConnection implementation
 */
public class N2NConnectionImpl implements N2NConnection
{

    private final String localIp;
    private final String superNodeIp;
    private final int superNodePort;
    private final String interfaceName;
    private final String communityName;


    public N2NConnectionImpl( final String localIp, final String superNodeIp, final int superNodePort,
                              final String interfaceName, final String communityName )
    {
        this.localIp = localIp;
        this.superNodeIp = superNodeIp;
        this.superNodePort = superNodePort;
        this.interfaceName = interfaceName;
        this.communityName = communityName;
    }


    @Override
    public String getSuperNodeIp()
    {
        return null;
    }


    @Override
    public int getSuperNodePort()
    {
        return 0;
    }


    @Override
    public String getLocalIp()
    {
        return null;
    }


    @Override
    public String getInterfaceName()
    {
        return null;
    }


    @Override
    public String getCommunityName()
    {
        return null;
    }
}
