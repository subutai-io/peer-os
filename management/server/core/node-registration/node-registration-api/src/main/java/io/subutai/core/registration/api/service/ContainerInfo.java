package io.subutai.core.registration.api.service;


import java.util.Set;

import io.subutai.common.host.HostArchitecture;
import io.subutai.common.host.HostInfo;
import io.subutai.common.host.Interface;
import io.subutai.core.registration.api.RegistrationStatus;


public interface ContainerInfo extends HostInfo
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
    public Set<Interface> getInterfaces();


    /**
     * Returns architecture of host
     */
    public HostArchitecture getArch();

    /**
     * Return container template name
     */
    public String getTemplateName();

    /**
     * Returns container vlan
     */
    public Integer getVlan();


    /**
     * Container host public key
     */
    public String getPublicKey();


    /**
     * Get container host info status
     */
    public RegistrationStatus getStatus();


    /**
     * Returns host gateway, if container has environment relation if not returns empty string
     */
    public String getGateway();
}
