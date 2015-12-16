package io.subutai.core.environment.api;


import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import io.subutai.common.environment.Blueprint;
import io.subutai.common.environment.ContainerHostNotFoundException;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentModificationException;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.environment.NodeGroup;
import io.subutai.common.environment.Topology;
import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.network.DomainLoadBalanceStrategy;
import io.subutai.common.peer.AlertHandler;
import io.subutai.common.peer.AlertHandlerPriority;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.EnvironmentAlertHandlers;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.core.environment.api.exception.EnvironmentCreationException;
import io.subutai.core.environment.api.exception.EnvironmentDestructionException;
import io.subutai.core.environment.api.exception.EnvironmentManagerException;


/**
 * Environment Manager
 */
public interface EnvironmentManager
{

    /**
     * Returns all existing environments
     *
     * @return - set of {@code Environment}
     */
    Set<Environment> getEnvironments();


    Environment setupRequisites( Blueprint blueprint ) throws EnvironmentCreationException;


    Environment startEnvironmentBuild( String environmentId, String signedMessage, boolean async )
            throws EnvironmentCreationException;

    /**
     * Creates environment based on a passed topology
     *
     * @param blueprint - {@code Blueprint}
     * @param async - indicates whether environment is created synchronously or asynchronously to the calling party
     *
     * @return - created environment
     *
     * @throws EnvironmentCreationException - thrown if error occurs during environment creation
     */
    Environment createEnvironment( Blueprint blueprint, boolean async ) throws EnvironmentCreationException;


    /**
     * Imports environment based on a passed topology
     *
     * @param name - environment name
     * @param topology - {@code Topology} //@param subnetCidr - subnet in CIDR-notation string, e.g. "192.168.0.1/16"
     * @param ssh - optional ssh key content //@param async - indicates whether environment is created synchronously or
     * asynchronously to the calling party
     *
     * @return - created environment
     *
     * @throws EnvironmentCreationException - thrown if error occurs during environment creation
     */
    Environment importEnvironment( String name, Topology topology, Map<NodeGroup, Set<ContainerHostInfo>> containers,
                                   String ssh, Integer vlan ) throws EnvironmentCreationException;


    /**
     * Grows environment based on a passed topology
     *
     * @param blueprint - {@code Blueprint}
     * @param async - indicates whether environment is grown synchronously or asynchronously to the calling party
     *
     * @return - set of newly created {@code ContainerHost} or empty set if operation is async
     *
     * @throws EnvironmentModificationException - thrown if error occurs during environment modification
     * @throws EnvironmentNotFoundException - thrown if environment not found
     */
    Set<EnvironmentContainerHost> growEnvironment( String environmentId, Blueprint blueprint, boolean async )
            throws EnvironmentModificationException, EnvironmentNotFoundException;

    /**
     * Assigns ssh key to environment and inserts it into authorized_keys file of all the containers within the
     * environment
     *
     * @param environmentId - environment id
     * @param sshKey - ssh key content
     * @param async - indicates whether ssh key is applied synchronously or asynchronously to the calling party
     *
     * @throws io.subutai.common.environment.EnvironmentNotFoundException - thrown if environment not found
     * @throws io.subutai.common.environment.EnvironmentModificationException - thrown if error occurs during key
     * insertion
     */
    void setSshKey( String environmentId, String sshKey, boolean async )
            throws EnvironmentNotFoundException, EnvironmentModificationException;


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
    void destroyEnvironment( String environmentId, boolean async, boolean forceMetadataRemoval )
            throws EnvironmentDestructionException, EnvironmentNotFoundException;


    /**
     * Destroys container. If this is the last container, the associated environment will be removed too
     *
     * @param environmentId - id of container environment
     * @param containerId - id of container to destroy
     * @param async - indicates whether container is destroyed synchronously or asynchronously to the calling party
     * @param forceMetadataRemoval - if true, the call will remove container metadata from database even if container
     * was not destroyed due to some error, otherwise an exception is thrown
     *
     * @throws EnvironmentModificationException - thrown if error occurs during environment modification
     * @throws EnvironmentNotFoundException - thrown if environment not found
     */
    void destroyContainer( String environmentId, String containerId, boolean async, boolean forceMetadataRemoval )
            throws EnvironmentModificationException, EnvironmentNotFoundException;


    /**
     * Returns environment by id
     *
     * @param environmentId - environment id
     *
     * @return - {@code Environment}
     *
     * @throws EnvironmentNotFoundException - thrown if environment not found
     */
    Environment loadEnvironment( String environmentId ) throws EnvironmentNotFoundException;


    /**
     * Get default domain name defaultDomainName: intra.lan
     *
     * @return - default domain name
     */
    String getDefaultDomainName();

    /**
     * Removes environment from database only. Used to cleanup environment records.
     *
     * @param environmentId - environment id
     *
     * @throws EnvironmentNotFoundException - thrown if environment not found
     */
    void removeEnvironment( String environmentId ) throws EnvironmentNotFoundException;


    /**
     * Save environment blueprint
     *
     * @param blueprint - blueprint to save
     */
    void saveBlueprint( Blueprint blueprint ) throws EnvironmentManagerException;

    /**
     * Loads environment blueprint from DB
     *
     * @param id blueprint primary key
     *
     * @return environment blueprint
     */
    Blueprint getBlueprint( UUID id ) throws EnvironmentManagerException;

    ;


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
     * Updates environment container hosts metadata (hostname, network interface)
     *
     * @param environmentId - target environment Id
     */
    void updateEnvironmentContainersMetadata( String environmentId ) throws EnvironmentManagerException;

    /**
     * Removes an assigned domain if any from the environment
     *
     * @param environmentId - id of the environment which domain to remove
     */
    void removeEnvironmentDomain( String environmentId )
            throws EnvironmentModificationException, EnvironmentNotFoundException;

    /**
     * Assigns a domain to the environment. External client would be able to access the environment containers via the
     * domain name.
     *
     * @param environmentId - id of the environment to assign the passed domain to
     * @param newDomain - domain url
     * @param domainLoadBalanceStrategy - strategy to load balance requests to the domain
     * @param sslCertPath - path to SSL certificate to enable HTTPS access to domain only, null if not needed
     */
    void assignEnvironmentDomain( String environmentId, String newDomain,
                                  DomainLoadBalanceStrategy domainLoadBalanceStrategy, String sslCertPath )
            throws EnvironmentModificationException, EnvironmentNotFoundException;

    /**
     * Returns the currently assigned domain
     *
     * @param environmentId - id of the environment which domain to return
     *
     * @return - domain url or null if not assigned
     */
    String getEnvironmentDomain( String environmentId )
            throws EnvironmentManagerException, EnvironmentNotFoundException;


    boolean isContainerInEnvironmentDomain( String containerHostId, String environmentId )
            throws EnvironmentManagerException, EnvironmentNotFoundException;


    void addContainerToEnvironmentDomain( String containerHostId, String environmentId )
            throws EnvironmentModificationException, EnvironmentNotFoundException, ContainerHostNotFoundException;

    /**
     * Sets up ssh connectivity for container. Clients can connect to the container via ssh during 30 seconds after this
     * call. Connection will remain active unless client is idle for 30 seconds.
     *
     * @param containerHostId container id
     * @param environmentId env id
     *
     * @return port for ssh connection
     */
    int setupContainerSsh( final String containerHostId, final String environmentId )
            throws EnvironmentModificationException, EnvironmentNotFoundException, ContainerHostNotFoundException;

    void removeContainerFromEnvironmentDomain( String containerHostId, String environmentId )
            throws EnvironmentModificationException, EnvironmentNotFoundException, ContainerHostNotFoundException;

    void notifyOnContainerDestroyed( Environment environment, String containerId );

    void notifyOnContainerStateChanged( Environment environment, ContainerHost containerHost );

    void addAlertHandler( AlertHandler alertHandler );

    void removeAlertHandler( AlertHandler alertHandler );

    Collection<AlertHandler> getRegisteredAlertHandlers();

    EnvironmentAlertHandlers getEnvironmentAlertHandlers( EnvironmentId environmentId )
            throws EnvironmentNotFoundException;

    void startMonitoring( String handlerId, AlertHandlerPriority handlerPriority, String environmentId )
            throws EnvironmentManagerException;

    void stopMonitoring( String handlerId, AlertHandlerPriority handlerPriority, String environmentId )
            throws EnvironmentManagerException;
}
