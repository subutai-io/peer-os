package io.subutai.core.network.api;


/**
 * Represents tunnel between getPeerInfos
 */
public interface Tunnel
{
    public String getTunnelName();

    public String getTunnelIp();

    public int getTunnelId();
}
