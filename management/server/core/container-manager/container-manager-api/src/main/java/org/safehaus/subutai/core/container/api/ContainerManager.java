package org.safehaus.subutai.core.container.api;


import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Criteria;
import org.safehaus.subutai.core.strategy.api.ServerMetric;


/**
 * Container Manager provides methods for working with LXC containers.
 */

public interface ContainerManager
{

    /**
     * Returns all registered containers.
     *
     * @return set of all registered containers.
     */
    public Container getContainerByUuid( UUID uuid );


    /**
     * Clones set of containers on particular physical host by given template name, host name and environment ID.
     */
    public Set<Agent> clone( UUID envId, Agent agent, String templateName, Set<String> cloneNames )
            throws ContainerCreateException;

    /**
     * Clones set of containers by given parameters.
     *
     * @param envId environment ID
     * @param templateName template name
     * @param numOfContainers number of requested containers
     * @param strategyId container placement strategy ID
     * @param criteria criteria list for container placement strategy; may be null if placement strategy not required
     * criteria
     *
     * @return set of containers agent
     */
    public Set<Agent> clone( UUID envId, String templateName, int numOfContainers, String strategyId,
                             List<Criteria> criteria ) throws ContainerCreateException;

    /**
     * Clones set of containers by given parameters.
     *
     * @param envId environment ID
     * @param templateName template name
     * @param cloneName name of cloning container
     *
     * @return cloned container agent
     */
    public Agent clone( UUID envId, final String hostName, final String templateName, final String cloneName )
            throws ContainerCreateException;

    /**
     * Destroys containers on physical host
     *
     * @param hostName physical host
     * @param cloneNames set of container host names
     */
    public void destroy( final String hostName, final Set<String> cloneNames ) throws ContainerDestroyException;

    /**
     * Destroys container on physical host
     *
     * @param hostName physical host         s
     * @param cloneName the name of container
     */
    public void destroy( final String hostName, final String cloneName ) throws ContainerDestroyException;

    /**
     * Returns information about what lxc containers each physical servers has at present
     *
     * @return map where key is a hostname of physical server and value is a map where key is state of lxc and value is
     * a list of lxc hostnames
     */
    public Map<String, EnumMap<ContainerState, List<String>>> getContainersOnPhysicalServers();


    public List<ServerMetric> getPhysicalServerMetrics();

    /**
     * Starts lxc on a given physical server
     *
     * @param physicalAgent - physical server
     * @param lxcHostname - hostname of lxc
     *
     * @return true if all went ok, false otherwise
     */
    public boolean startLxcOnHost( Agent physicalAgent, String lxcHostname );

    /**
     * Stops lxc on a given physical server
     *
     * @param physicalAgent - physical server
     * @param lxcHostname - hostname of lxc
     *
     * @return true if all went ok, false otherwise
     */
    public boolean stopLxcOnHost( Agent physicalAgent, String lxcHostname );

    /**
     * Adds container event listener
     */
    public void addListener( ContainerEventListener listener );

    /**
     * Removes container event listener
     */
    public void removeListener( ContainerEventListener listener );

}
