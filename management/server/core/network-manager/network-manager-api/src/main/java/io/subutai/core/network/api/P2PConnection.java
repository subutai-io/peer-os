package io.subutai.core.network.api;


/**
 * N2N Connection
 */
public interface P2PConnection
{
    public String getSuperNodeIp();

    public int getSuperNodePort();

    public String getLocalIp();

    public String getInterfaceName();

    public String getCommunityName();
}
