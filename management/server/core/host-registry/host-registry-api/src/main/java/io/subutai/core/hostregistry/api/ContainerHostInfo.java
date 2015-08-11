package io.subutai.core.hostregistry.api;


import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostInfo;


/**
 * Container info
 *
 * TODO add method getName() which returns lxc container name
 */
public interface ContainerHostInfo extends HostInfo
{
    /**
     * Returns status/state of container
     */
    public ContainerHostState getStatus();
}
