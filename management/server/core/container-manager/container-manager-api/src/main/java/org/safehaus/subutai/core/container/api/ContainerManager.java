package org.safehaus.subutai.core.container.api;


import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.strategy.api.ContainerPlacementStrategy;
import org.safehaus.subutai.core.strategy.api.Criteria;


/**
 * Created by timur on 9/4/14.
 */
public interface ContainerManager {

    /**
     * Returns all registered containers.
     *
     * @return set of all registered containers.
     */
    public Container getContainerByUuid( UUID uuid );

    public List<ContainerPlacementStrategy> getPlacementStrategies();

    public Map<Agent, Integer> getPlacementDistribution( int nodesCount, String strategyId, List<Criteria> criteria );

    //    public Set<Agent> clone( UUID envId, String templateName, int nodesCount, Set<Agent> hosts, String strategyId,
    //                             List<Criteria> criteria ) throws ContainerCreateException;
    public Set<Agent> clone( UUID envId, Agent agent, String templateName, Set<String> cloneNames )
            throws ContainerCreateException;

    //    public void clone(UUID envId, final String hostName, final String templateName, final Set<String> cloneNames)
    //            throws ContainerCreateException;
    //
    public void clone( UUID envId, final String hostName, final String templateName, final String cloneName )
            throws ContainerCreateException;

    public void destroy( final String hostName, final Set<String> cloneNames ) throws ContainerDestroyException;

    public void destroy( final String hostName, final String cloneName ) throws ContainerDestroyException;

    /**
     * Returns information about what lxc containers each physical servers has at present
     *
     * @return map where key is a hostname of physical server and value is a map where key is state of lxc and value is
     * a list of lxc hostnames
     */
    public Map<String, EnumMap<ContainerState, List<String>>> getContainersOnPhysicalServers();

    //    /**
    //     * Returns number of lxc slots that each currently connected physical server can host. This method uses
    // default lxc
    //     * placement strategy for calculations
    //     *
    //     * @return map where key is a physical server and value is the number of lxc slots
    //     */
    //    public Map<Agent, Integer> getPhysicalServersWithLxcSlots(String strategy);

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

    public void addListener( ContainerEventListener listener );

    public void removeListener( ContainerEventListener listener );
}
