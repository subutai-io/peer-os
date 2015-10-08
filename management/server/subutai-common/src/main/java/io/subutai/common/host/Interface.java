package io.subutai.common.host;


/**
 * Represent a host network interface
 */
public interface Interface
{
    /**
     * returns network interface name
     */
    public String getName();

    /**
     * returns ip address
     */
    public String getIp();

    /**
     * returns MAC address
     */
    public String getMac();
}
