package org.safehaus.subutai.core.hostregistry.api;


/**
 * Represent a host network interface
 */
public interface Interface
{
    /**
     * returns network interface name
     */
    public String getInterfaceName();

    /**
     * returns ip address
     */
    public String getIp();

    /**
     * returns MAC address
     */
    public String getMac();
}
