package org.safehaus.subutai.api.containermanager;

import org.safehaus.subutai.api.strategymanager.Criteria;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.PlacementStrategy;

import java.util.*;

/**
 * Created by timur on 9/4/14.
 */
public interface ContainerManager {

    public Map<Agent, Integer> getPlacementDistribution(int nodesCount, PlacementStrategy strategy, List<Criteria> criteria);
    public Set<Agent> clone(UUID envId, String templateName, int nodesCount, Collection<Agent> hosts,
                            PlacementStrategy strategy,
                            List<Criteria> criteria) throws ContainerCreateException;

    public void clonesDestroy(final String hostName, final Set<String> cloneNames) throws ContainerDestroyException;

    public void clonesCreate(final String hostName, final String templateName, final Set<String> cloneNames)
            throws ContainerCreateException;

    public void clone(final String hostName, final String templateName, final String cloneName)
            throws ContainerCreateException;

    public void destroy(final String hostName, final String cloneName)
            throws ContainerDestroyException;

    /**
     * Returns information about what lxc containers each physical servers has at present
     *
     * @return map where key is a hostname of physical server and value is a map where key is state of lxc and value is
     * a list of lxc hostnames
     */
    public Map<String, EnumMap<ContainerState, List<String>>> getContainersOnPhysicalServers();

//    /**
//     * Returns number of lxc slots that each currently connected physical server can host. This method uses default lxc
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
}
