package io.subutai.common.peer;


import java.util.Set;

import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostInfo;
import io.subutai.common.host.ResourceHostInfo;
import io.subutai.common.metric.ResourceHostMetric;


/**
 * Resource host interface.
 */
public interface ResourceHost extends Host, ResourceHostInfo
{
    /**
     * Returns resource usage metric of the resource host
     */
    //    public ResourceHostMetric getHostMetric();

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
    public ContainerHostState getContainerHostState( final ContainerHost container ) throws ResourceHostException;


    /**
     * Creates container on the resource host
     *
     * @param templateName - name of template from which to clone container
     * @param hostname - hostname for container
     * @param ip - IP to assign to container
     * @param vlan - vlan to assign to container
     * @param timeout - timeout to wait until container connects to server
     * @param environmentId - id of environment to which the container will belong
     */
    public HostInfo createContainer( String templateName, String hostname, String ip, int vlan, int timeout,
                                     String environmentId ) throws ResourceHostException;

    Set<ContainerHost> getContainerHostsByEnvironmentId( String environmentId );

    Set<ContainerHost> getContainerHostsByOwnerId( String ownerId );

    Set<ContainerHost> getContainerHostsByPeerId( String peerId );

    void addContainerHost( ContainerHost host );

    ResourceHostMetric getMetric();
}
