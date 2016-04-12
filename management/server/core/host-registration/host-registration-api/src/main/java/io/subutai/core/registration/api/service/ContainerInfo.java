package io.subutai.core.registration.api.service;


import io.subutai.common.host.ContainerHostInfo;
import io.subutai.core.registration.api.RegistrationStatus;


public interface ContainerInfo extends ContainerHostInfo
{

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
