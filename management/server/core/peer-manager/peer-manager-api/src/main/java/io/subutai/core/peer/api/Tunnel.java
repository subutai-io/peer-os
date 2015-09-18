package io.subutai.core.peer.api;


/**
 * Represents tunnel between peers
 */
public interface Tunnel
{
    public String getTunnelName();

    public String getTunnelIp();

    public int getTunnelId();
}
