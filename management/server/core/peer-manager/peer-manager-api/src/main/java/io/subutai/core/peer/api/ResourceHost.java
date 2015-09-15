package io.subutai.core.peer.api;


import java.util.Set;

import io.subutai.common.metric.ResourceHostMetric;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.Host;


/**
 * Resource host interface.
 */
public interface ResourceHost extends Host
{
    /**
     * Returns resource usage metric of the resource host
     */
    public ResourceHostMetric getHostMetric() throws ResourceHostException;

    /**
     * Returns hosts containers
     */
    public Set<ContainerHost> getContainerHosts();

    /**
     * Returns hosted container by its hostname
     */
    public ContainerHost getContainerHostByName( String hostname ) throws HostNotFoundException;

    /**
     * Returns hosted container by its id
     */
    public ContainerHost getContainerHostById( String id ) throws HostNotFoundException;

    /**
     * Starts hosted container
     */
    public void startContainerHost( ContainerHost containerHost ) throws ResourceHostException;

    /**
     * Stops hosted container
     */
    public void stopContainerHost( ContainerHost containerHost ) throws ResourceHostException;

    /**
     * Destroys hosted container
     */
    public void destroyContainerHost( ContainerHost containerHost ) throws ResourceHostException;

    /**
     * Returns state of hosted container
     */
    public ContainerState getContainerHostState( final ContainerHost container ) throws ResourceHostException;

    /**
     * Creates container on the resource host
     *
     * @param templateName - name of template from which to clone container
     * @param hostname - hostname for the new container
     * @param timeout - timeout to wait until container connects to server
     */
    public ContainerHost createContainer( String templateName, String hostname, int timeout )
            throws ResourceHostException;

    /**
     * Creates container on the resource host
     *
     * @param templateName - name of template from which to clone container
     * @param hostname - hostname for container
     * @param ip - IP to assign to container
     * @param vlan - vlan to assign to container
     * @param gateway - default gateway for container
     * @param timeout - timeout to wait until container connects to server
     */
    public ContainerHost createContainer( String templateName, String hostname, String ip, int vlan, String gateway,
                                          int timeout ) throws ResourceHostException;
}
