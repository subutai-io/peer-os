package io.subutai.common.host;


/**
 * Container info
 */
public interface ContainerHostInfo extends HostInfo
{
    /**
     * Returns status/state of container
     */
    public ContainerHostState getStatus();

    /**
     * Returns lxc container name
     */
    public String getContainerName();
}
