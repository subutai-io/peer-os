package io.subutai.common.environment;


import java.util.Set;
import java.util.UUID;

import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.Peer;


/**
 * Environment
 */
public interface Environment
{
    /**
     * Return id of environment creator user
     */
    public Long getUserId();

    /**
     * Returns id of environment
     */
    public UUID getId();

    /**
     * Returns name of environment
     */
    public String getName();

    /**
     * Returns status of environment
     *
     * @return @{code EnvironmentStatus}
     */
    public EnvironmentStatus getStatus();

    /**
     * Returns creation timestamp
     */
    public long getCreationTimestamp();

    Set<EnvironmentPeer> getEnvironmentPeers();

    /**
     * Returns ssh key if any of environment
     *
     * @return - key or null
     */
    public String getSshKey();

    /**
     * Returns contained container hosts
     *
     * @return - set of @{code ContainerHost}
     */
    public Set<ContainerHost> getContainerHosts();


    /**
     * Destroys the specified container
     *
     * @param containerHost - container to destroy
     * @param async - sync or async to the calling party
     */
    public void destroyContainer( ContainerHost containerHost, boolean async )
            throws EnvironmentNotFoundException, EnvironmentModificationException;


    /**
     * Grows environment according to the passed topology
     *
     * @param topology = topology to use when growing
     * @param async - sync or async to the calling party
     */
    public Set<ContainerHost> growEnvironment( Topology topology, boolean async )
            throws EnvironmentModificationException;


    /**
     * Sets/removes ssh key
     *
     * @param sshKey - ssh key or null to remove
     * @param async - sync or async to the calling party
     */
    public void setSshKey( String sshKey, boolean async ) throws EnvironmentModificationException;

    /**
     * Returns pees which host any container(s) from this environment
     */
    public Set<Peer> getPeers();

    /**
     * Network subnet of the environment in CIDR format notation.
     *
     * @return subnet string in CIDR format notation
     */
    public String getSubnetCidr();


    /**
     * VNI of the environment.
     */
    public Long getVni();

    /**
     * Searches container by its id withing this environment
     *
     * @param id - id of container to find
     *
     * @return - found container host
     */

    public ContainerHost getContainerHostById( UUID id ) throws ContainerHostNotFoundException;

    /**
     * Searches container by its hostname withing this environment
     *
     * @param hostname - hostname of container to find
     *
     * @return - found container host
     */
    public ContainerHost getContainerHostByHostname( String hostname ) throws ContainerHostNotFoundException;

    public Set<ContainerHost> getContainerHostsByIds( Set<UUID> ids ) throws ContainerHostNotFoundException;

    String findN2nIp( String peerId );

    void addEnvironmentPeer( EnvironmentPeer environmentPeer );


}
