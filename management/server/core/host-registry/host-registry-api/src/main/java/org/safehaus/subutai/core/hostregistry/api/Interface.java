package org.safehaus.subutai.core.hostregistry.api;


/**
 * Represent a host network interface
 */
public interface Interface
{
    public String getInterfaceName();

    public String getIp();

    public String getMac();
}
