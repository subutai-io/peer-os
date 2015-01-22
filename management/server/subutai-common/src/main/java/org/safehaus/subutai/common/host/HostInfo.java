package org.safehaus.subutai.common.host;


import java.util.Set;
import java.util.UUID;


/**
 * Parent interface for host info
 */
public interface HostInfo extends Comparable<HostInfo>
{
    /**
     * Returns id of host
     */
    public UUID getId();


    /**
     * Returns hostname of host
     */
    public String getHostname();


    /**
     * Returns network interfaces of host
     */
    public Set<Interface> getInterfaces();


    /**
     * Returns architecture of host
     */
    public HostArchitecture getArch();
}
