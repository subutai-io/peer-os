package io.subutai.common.host;


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

    /**
     * Returns lxc container name
     */
    public String getContainerName();
}
