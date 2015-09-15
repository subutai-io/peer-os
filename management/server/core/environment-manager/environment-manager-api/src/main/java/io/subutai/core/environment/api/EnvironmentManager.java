package io.subutai.core.environment.api;


import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.environment.Topology;


/**
 * Environment Manager
 */
public interface EnvironmentManager
{

    /**
     * Creates environment based on a passed topology
     *
     * @param name - environment name
     * @param topology - {@code Topology}
     * @param subnetCidr - subnet in CIDR-notation string, e.g. "192.168.0.1/16"
     * @param sshKey - optional ssh key content
     *
     * @return - id of new environment
     */
    String createEnvironment( String name, Topology topology, String subnetCidr, String sshKey );


    /**
     * Grows environment based on a passed topology
     *
     * @param environmentId - environment id
     * @param topology - {@code Topology}
     *
     * @throws EnvironmentNotFoundException - thrown if environment not found
     */
    void growEnvironment( String environmentId, Topology topology ) throws EnvironmentNotFoundException;
}
