package org.safehaus.subutai.api.container;


import org.safehaus.subutai.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.api.lxcmanager.LxcState;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.PlacementStrategy;

import java.util.*;
import java.util.concurrent.TimeUnit;


public interface ContainerManager {

	public Set<Agent> clone(UUID envId, String templateName, int nodesCount, Collection<Agent> hosts,
	                        PlacementStrategy... strategy) throws LxcCreateException;

	/**
	 * Clones containers in parallel, simultaneously
	 */
	public Set<Agent> clone(String templateName, int nodesCount, Collection<Agent> hosts,
	                        PlacementStrategy... strategy) throws LxcCreateException;

	public boolean attachAndExecute(Agent physicalHost, String cloneName, String cmd);

	public boolean attachAndExecute(Agent physicalHost, String cloneName, String cmd, long t, TimeUnit unit);

	public void cloneDestroy(String hostName, String cloneName) throws LxcDestroyException;

	/**
	 * Destroys containers in parallel, simultaneously
	 */
	public void clonesDestroyByHostname(Set<String> cloneNames) throws LxcDestroyException;

	/**
	 * Destroys containers in parallel, simultaneously
	 */
	public void clonesDestroy(Set<Agent> lxcAgents) throws LxcDestroyException;

	public void clonesDestroy(final String hostName, final Set<String> cloneNames) throws LxcDestroyException;

	public void clonesCreate(final String hostName, final String templateName, final Set<String> cloneNames)
			throws LxcCreateException;

    /**
     * Returns number of lxc slots that each currently connected physical server can host. This method uses default lxc
     * placement strategy for calculations
     *
     * @return map where key is a physical server and value is the number of lxc slots
     */
    public Map<Agent, Integer> getPhysicalServersWithLxcSlots();

    /**
     * Returns information about what lxc containers each physical servers has at present
     *
     * @return map where key is a hostname of physical server and value is a map where key is state of lxc and value is
     * a list of lxc hostnames
     */
    public Map<String, EnumMap<LxcState, List<String>>> getLxcOnPhysicalServers();

    public boolean cloneLxcOnHost( Agent physicalAgent, String lxcHostname );


    }
