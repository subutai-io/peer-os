package io.subutai.common.host;


import javax.xml.bind.annotation.XmlRootElement;


/**
 * Represent a host network interface
 */
@XmlRootElement
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
