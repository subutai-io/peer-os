package io.subutai.core.network.impl;


import io.subutai.common.protocol.P2PConnection;


/**
 * P2PConnection implementation
 */
public class P2PConnectionImpl implements P2PConnection
{

    private final String interfaceName;
    private final String localIp;
    private final String p2pHash;


    public P2PConnectionImpl( final String interfaceName, final String localIp, final String p2pHash )
    {
        this.localIp = localIp;

        this.interfaceName = interfaceName;
        this.p2pHash = p2pHash;
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
    public String getP2pHash()
    {
        return p2pHash;
    }
}
