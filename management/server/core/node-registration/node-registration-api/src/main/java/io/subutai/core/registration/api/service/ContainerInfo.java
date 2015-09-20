package io.subutai.core.registration.api.service;


import java.util.Set;
import java.util.UUID;

import io.subutai.common.host.HostArchitecture;
import io.subutai.common.host.HostInfo;
import io.subutai.common.host.Interface;
import io.subutai.core.registration.api.RegistrationStatus;


/**
 * Created by talas on 9/15/15.
 */
public interface ContainerInfo extends HostInfo
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
}
