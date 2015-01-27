package org.safehaus.subutai.core.env.api;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.core.env.api.build.Blueprint;
import org.safehaus.subutai.core.env.api.build.Topology;
import org.safehaus.subutai.core.env.api.exception.EnvironmentCreationException;
import org.safehaus.subutai.core.env.api.exception.EnvironmentDestructionException;
import org.safehaus.subutai.core.env.api.exception.EnvironmentManagerException;
import org.safehaus.subutai.core.env.api.exception.EnvironmentModificationException;
import org.safehaus.subutai.core.env.api.exception.EnvironmentNotFoundException;


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
    public Set<Environment> getEnvironments();

    /**
     * Returns environment by id
     *
     * @param environmentId - environment id
     *
     * @return - {@code Environment}
     *
     * @throws EnvironmentNotFoundException - thrown if environment not found
     */
    public Environment findEnvironment( UUID environmentId ) throws EnvironmentNotFoundException;

    /**
     * Creates environment based on a passed topology
     *
     * @param name - environment name
     * @param topology - {@code Topology}
     * @param async - indicates whether environment is created synchronously or asynchronously to the calling party
     *
     * @return - created environment
     *
     * @throws EnvironmentCreationException - thrown if error occurs during environment creation
     */
    public Environment createEnvironment( String name, Topology topology, boolean async )
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
    public void destroyEnvironment( UUID environmentId, boolean async, boolean forceMetadataRemoval )
            throws EnvironmentDestructionException, EnvironmentNotFoundException;


    /**
     * Grows environment based on a passed topology
     *
     * @param environmentId - environment id
     * @param topology - {@code Topology}
     * @param async - indicates whether environment is grown synchronously or asynchronously to the calling party
     *
     * @return - target environment
     *
     * @throws EnvironmentModificationException - thrown if error occurs during environment modification
     * @throws EnvironmentNotFoundException - thrown if environment not found
     */
    public Environment growEnvironment( UUID environmentId, Topology topology, boolean async )
            throws EnvironmentModificationException, EnvironmentNotFoundException;

    /**
     * Assigns ssh key to environment and inserts it into authorized_keys file of all the containers within the
     * environment
     *
     * @param environmentId - environment id
     * @param sshKey - ssh key content
     *
     * @throws EnvironmentNotFoundException - thrown if environment not found
     * @throws EnvironmentManagerException - thrown if error occurs during key insertion
     */
    public void setSshKey( UUID environmentId, String sshKey )
            throws EnvironmentNotFoundException, EnvironmentManagerException;


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
    public void destroyContainer( ContainerHost containerHost, boolean async, boolean forceMetadataRemoval )
            throws EnvironmentModificationException, EnvironmentNotFoundException;


    /**
     * Removes environment from database only. Used to cleanup environment records.
     *
     * @param environmentId - environment id
     *
     * @throws EnvironmentNotFoundException - thrown if environment not found
     */
    public void removeEnvironment( UUID environmentId ) throws EnvironmentNotFoundException;


    public void saveBlueprint( Blueprint blueprint ) throws EnvironmentManagerException;

    public void removeBlueprint( UUID blueprintId ) throws EnvironmentManagerException;

    public Set<Blueprint> getBlueprints() throws EnvironmentManagerException;
}
