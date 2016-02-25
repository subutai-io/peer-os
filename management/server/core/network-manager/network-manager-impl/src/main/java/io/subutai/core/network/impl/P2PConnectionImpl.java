package io.subutai.core.network.impl;


import io.subutai.common.protocol.P2PConnection;


/**
 * P2PConnection implementation
 */
public class P2PConnectionImpl implements P2PConnection
{

    private final String interfaceName;
    private final String localIp;
    private final String communityName;


    public P2PConnectionImpl( final String interfaceName, final String localIp, final String communityName )
    {
        this.localIp = localIp;

        this.interfaceName = interfaceName;
        this.communityName = communityName;
    }


    @Override
    public String getLocalIp()
    {
        return localIp;
    }


    @Override
    public String getInterfaceName()
    {
        return interfaceName;
    }


    @Override
    public String getCommunityName()
    {
        return communityName;
    }
}
