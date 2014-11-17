package org.safehaus.subutai.core.hostregistry.api;


/**
 * Container info
 */
public interface ContainerHostInfo extends HostInfo
{
    public ContainerHostState getStatus();
}
