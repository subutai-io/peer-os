package io.subutai.common.host;


import java.util.Set;


/**
 * Parent interface for host info
 */
public interface HostInfo extends Comparable<HostInfo>
{
    /**
     * Returns id of host
     */
    public String getId();


    /**
     * Returns hostname of host
     */
    public String getHostname();


    /**
     * Returns network interfaces of host
     */
    public Set<HostInterface> getInterfaces();


    /**
     * Returns architecture of host
     */
    public HostArchitecture getArch();
}
