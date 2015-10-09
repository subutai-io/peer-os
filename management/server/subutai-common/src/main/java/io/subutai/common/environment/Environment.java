package io.subutai.common.environment;


import java.util.Map;
import java.util.Set;

import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.Peer;


/**
 * Environment
 */
public interface Environment
{
    /**
     * Return id of environment creator user
     */
    Long getUserId();

    /**
     * Returns id of environment
     */
    String getId();

    /**
     * Returns name of environment
     */
    String getName();

    /**
     * Returns status of environment
     *
     * @return @{code EnvironmentStatus}
     */
    EnvironmentStatus getStatus();

    /**
     * Returns creation timestamp
     */
    long getCreationTimestamp();

    Set<PeerConf> getPeerConfs();

    /**
     * Returns ssh key if any of environment
     *
     * @return - key or null
     */
    String getSshKey();

    /**
     * Returns contained container hosts
     *
     * @return - set of @{code ContainerHost}
     */
    Set<EnvironmentContainerHost> getContainerHosts();


    /**
     * Destroys the specified container
     *
     * @param containerHost - container to destroy
     * @param async - sync or async to the calling party
     */
    void destroyContainer( EnvironmentContainerHost containerHost, boolean async )
            throws EnvironmentNotFoundException, EnvironmentModificationException;


    /**
     * Grows environment according to the passed topology
     *
     * @param topology = topology to use when growing
     * @param async - sync or async to the calling party
     */
    Set<EnvironmentContainerHost> growEnvironment( Topology topology, boolean async )
            throws EnvironmentModificationException;


    /**
     * Sets/removes ssh key
     *
     * @param sshKey - ssh key or null to remove
     * @param async - sync or async to the calling party
     */
    void setSshKey( String sshKey, boolean async ) throws EnvironmentModificationException;

    /**
     * Returns pees which host any container(s) from this environment
     */
    Set<Peer> getPeers();

    /**
     * Network subnet of the environment in CIDR format notation.
     *
     * @return subnet string in CIDR format notation
     */
    String getSubnetCidr();


    /**
     * VNI of the environment.
     */
    Long getVni();

    String getPeerId();

    /**
     * Searches container by its id withing this environment
     *
     * @param id - id of container to find
     *
     * @return - found container host
     */
    EnvironmentContainerHost getContainerHostById( String id ) throws ContainerHostNotFoundException;

    /**
     * Searches container by its hostname withing this environment
     *
     * @param hostname - hostname of container to find
     *
     * @return - found container host
     */
    EnvironmentContainerHost getContainerHostByHostname( String hostname ) throws ContainerHostNotFoundException;

    Set<EnvironmentContainerHost> getContainerHostsByIds( Set<String> ids ) throws ContainerHostNotFoundException;

    String getTunnelNetwork();

    Map<String, String> getTunnels();

    boolean isMember( Peer peer );

    String getSuperNode();

    int getSuperNodePort();

    String getTunnelInterfaceName();

    String getTunnelCommunityName();
}
