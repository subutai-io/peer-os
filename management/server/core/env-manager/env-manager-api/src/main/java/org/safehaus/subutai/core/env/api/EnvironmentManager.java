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
     *
     * @return - created environment
     *
     * @throws EnvironmentCreationException - thrown if error occurs during environment creation
     */
    public Environment createEnvironment( String name, Topology topology ) throws EnvironmentCreationException;

    /**
     * Creates environment based on a passed topology  asynchronously
     *
     * @param name - environment name
     * @param topology - {@code Topology}
     */
    public void createEnvironmentAsync( String name, Topology topology );


    /**
     * Destroys environment by id
     *
     * @param environmentId - environment id
     *
     * @throws EnvironmentDestructionException - thrown if error occurs during environment destruction
     * @throws EnvironmentNotFoundException - thrown if environment not found
     */
    public void destroyEnvironment( UUID environmentId )
            throws EnvironmentDestructionException, EnvironmentNotFoundException;

    /**
     * Destroys environment by id asynchronously
     *
     * @param environmentId - environment id
     *
     * @throws EnvironmentNotFoundException - thrown if environment not found
     */
    public void destroyEnvironmentAsync( UUID environmentId ) throws EnvironmentNotFoundException;

    /**
     * Grows environment based on a passed topology
     *
     * @param environmentId - environment id
     * @param topology - {@code Topology}
     *
     * @return - target environment
     *
     * @throws EnvironmentModificationException - thrown if error occurs during environment modification
     * @throws EnvironmentNotFoundException - thrown if environment not found
     */
    public Environment growEnvironment( UUID environmentId, Topology topology )
            throws EnvironmentModificationException, EnvironmentNotFoundException;

    /**
     * Destroys container
     *
     * @param containerHost - container to destroy
     *
     * @throws EnvironmentModificationException - thrown if error occurs during environment modification
     * @throws EnvironmentNotFoundException - thrown if environment not found
     */
    public void destroyContainer( ContainerHost containerHost )
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
