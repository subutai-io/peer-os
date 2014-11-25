package org.safehaus.subutai.core.hostregistry.api;


/**
 * Container info
 */
public interface ContainerHostInfo extends HostInfo
{
    /**
     * Returns status/state of container
     */
    public ContainerHostState getStatus();
}
