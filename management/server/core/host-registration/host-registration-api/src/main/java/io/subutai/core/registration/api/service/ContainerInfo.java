package io.subutai.core.registration.api.service;


import io.subutai.common.host.ContainerHostInfo;
import io.subutai.core.registration.api.ResourceHostRegistrationStatus;


public interface ContainerInfo extends ContainerHostInfo
{

    /**
     * Return container template name
     */
    String getTemplateName();

    /**
     * Returns container vlan
     */
    Integer getVlan();


    /**
     * Container host public key
     */
    String getPublicKey();


    /**
     * Get container host info status
     */
    ResourceHostRegistrationStatus getStatus();


    /**
     * Returns host gateway, if container has environment relation if not returns empty string
     */
    String getGateway();
}
