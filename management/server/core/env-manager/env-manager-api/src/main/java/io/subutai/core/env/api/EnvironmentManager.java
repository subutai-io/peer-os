package io.subutai.core.env.api;


import java.util.List;
import java.util.Set;
import java.util.UUID;

import io.subutai.common.environment.Blueprint;
import io.subutai.common.environment.ContainerHostNotFoundException;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentModificationException;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.environment.PeerConf;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.Peer;
import io.subutai.common.protocol.N2NConfig;
import io.subutai.core.env.api.exception.EnvironmentCreationException;
import io.subutai.core.env.api.exception.EnvironmentDestructionException;
import io.subutai.core.env.api.exception.EnvironmentManagerException;


/**
 * Environment Manager
 */
public interface EnvironmentManager
{

    /* Returns all existing environments
     *
     * @return - set of {@code Environment}
     */
    Set<Environment> getEnvironments();

    /**
     * Returns environment by id
     *
     * @param environmentId - environment id
     *
     * @return - {@code Environment}
     *
     * @throws EnvironmentNotFoundException - thrown if environment not found
     */
    Environment findEnvironment( UUID environmentId ) throws EnvironmentNotFoundException;

    /**
     * Creates environment based on a passed topology
     *
     * @param name - environment name
     * @param topology - {@code Topology}
     * @param subnetCidr - subnet in CIDR-notation string, e.g. "192.168.0.1/16"
     * @param sshKey - optional ssh key content
     * @param async - indicates whether environment is created synchronously or asynchronously to the calling party
     *
     * @return - created environment
     *
     * @throws EnvironmentCreationException - thrown if error occurs during environment creation
     */
    Environment createEnvironment( String name, Topology topology, String subnetCidr, String sshKey, boolean async )
            throws EnvironmentCreationException;


    /**
     * Destroys environment by id.
     *
     * @param environmentId - environment id
     * @param async - indicates whether environment is destroyed synchronously or asynchronously to the calling party
     * @param forceMetadataRemoval - if true, the call will remove environment metadata from database even if not all
     * containers were destroyed, otherwise an exception is thrown when first error occurs
     *
     * @throws EnvironmentDestructionException - thrown if error occurs during environment destruction
     * @throws EnvironmentNotFoundException - thrown if environment not found
     */
    void destroyEnvironment( UUID environmentId, boolean async, boolean forceMetadataRemoval )
            throws EnvironmentDestructionException, EnvironmentNotFoundException;


    /**
     * Grows environment based on a passed topology
     *
     * @param environmentId - environment id
     * @param topology - {@code Topology}
     * @param async - indicates whether environment is grown synchronously or asynchronously to the calling party
     *
     * @return - set of newly created {@code ContainerHost} or empty set if operation is async
     *
     * @throws EnvironmentModificationException - thrown if error occurs during environment modification
     * @throws EnvironmentNotFoundException - thrown if environment not found
     */
    Set<ContainerHost> growEnvironment( UUID environmentId, Topology topology, boolean async )
            throws EnvironmentModificationException, EnvironmentNotFoundException;

    /**
     * Assigns ssh key to environment and inserts it into authorized_keys file of all the containers within the
     * environment
     *
     * @param environmentId - environment id
     * @param sshKey - ssh key content
     * @param async - indicates whether ssh key is applied synchronously or asynchronously to the calling party
     *
     * @throws EnvironmentNotFoundException - thrown if environment not found
     * @throws EnvironmentModificationException - thrown if error occurs during key insertion
     */
    void setSshKey( UUID environmentId, String sshKey, boolean async )
            throws EnvironmentNotFoundException, EnvironmentModificationException;


    /**
     * Destroys container. If this is the last container, the associated environment will be removed too
     *
     * @param containerHost - container to destroy
     * @param async - indicates whether container is destroyed synchronously or asynchronously to the calling party
     * @param forceMetadataRemoval - if true, the call will remove container metadata from database even if container
     * was not destroyed due to some error, otherwise an exception is thrown
     *
     * @throws EnvironmentModificationException - thrown if error occurs during environment modification
     * @throws EnvironmentNotFoundException - thrown if environment not found
     */
    //todo use containerId instead of containerHost
    void destroyContainer( ContainerHost containerHost, boolean async, boolean forceMetadataRemoval )
            throws EnvironmentModificationException, EnvironmentNotFoundException;


    /**
     * Removes environment from database only. Used to cleanup environment records.
     *
     * @param environmentId - environment id
     *
     * @throws EnvironmentNotFoundException - thrown if environment not found
     */
    void removeEnvironment( UUID environmentId ) throws EnvironmentNotFoundException;


    /**
     * Save environment blueprint
     *
     * @param blueprint - blueprint to save
     */
    void saveBlueprint( Blueprint blueprint ) throws EnvironmentManagerException;


    /**
     * Remove blueprint from database
     *
     * @param blueprintId - blueprint id to remove
     */
    void removeBlueprint( UUID blueprintId ) throws EnvironmentManagerException;


    /**
     * Get All blueprints
     *
     * @return - set of blueprints
     */
    Set<Blueprint> getBlueprints() throws EnvironmentManagerException;


    /**
     * Get default domain name defaultDomainName: intra.lan
     *
     * @return - default domain name
     */
    String getDefaultDomainName();


    /**
     * Updates environment container hosts metadata (hostname, network interface)
     *
     * @param environmentId - target environment Id
     */
    void updateEnvironmentContainersMetadata( UUID environmentId ) throws EnvironmentManagerException;

    /**
     * Removes an assigned domain if any from the environment
     *
     * @param environmentId - id of the environment which domain to remove
     * @param async - indicates whether operation is done synchronously or asynchronously to the calling party
     */
    void removeDomain( UUID environmentId, boolean async )
            throws EnvironmentModificationException, EnvironmentNotFoundException;

    /**
     * Assigns a domain to the environment. External client would be able to access the environment containers via the
     * domain name.
     *
     * @param environmentId - id of the environment to assign the passed domain to
     * @param newDomain - domain url
     * @param async - indicates whether operation is done synchronously or asynchronously to the calling party
     */
    void assignDomain( UUID environmentId, String newDomain, boolean async )
            throws EnvironmentModificationException, EnvironmentNotFoundException;

    /**
     * Returns the currently assigned domain
     *
     * @param environmentId - id of the environment which domain to return
     *
     * @return - domain url or null if not assigned
     */
    String getDomain( UUID environmentId ) throws EnvironmentManagerException, EnvironmentNotFoundException;


    boolean isContainerInDomain( UUID containerHostId, UUID environmentId )
            throws EnvironmentManagerException, EnvironmentNotFoundException;


    void addContainerToDomain( UUID containerHostId, UUID environmentId, boolean async )
            throws EnvironmentModificationException, EnvironmentNotFoundException, ContainerHostNotFoundException;


    void removeContainerFromDomain( UUID containerHostId, UUID environmentId, boolean async )
            throws EnvironmentModificationException, EnvironmentNotFoundException, ContainerHostNotFoundException;


    List<N2NConfig> setupN2NConnection( Set<Peer> peers ) throws EnvironmentManagerException;

    void removeN2NConnection( Environment environment ) throws EnvironmentManagerException;
}
