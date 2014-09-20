/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.container.api.lxcmanager;


import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.agent.api.AgentManager;


/**
 * Lxc Manager manages all lxc related operations such as creation, destruction and manipulation (start/stop/check
 * status).
 */
public interface LxcManager
{

    /**
     * Returns number of lxc slots that each currently connected physical server can host. This method uses default lxc
     * placement strategy for calculations
     *
     * @return map where key is a physical server and value is the number of lxc slots
     */
    public Map<Agent, Integer> getPhysicalServersWithLxcSlots();

    /**
     * Returns metrics of all physical servers connected to the management server
     *
     * @return map of metrics where key is a physical agent and value is a metric metric
     */
    public Map<Agent, ServerMetric> getPhysicalServerMetrics();

    /**
     * Returns information about what lxc containers each physical servers has at present
     *
     * @return map where key is a hostname of physical server and value is a map where key is state of lxc and value is
     * a list of lxc hostnames
     */
    public Map<String, EnumMap<LxcState, List<String>>> getLxcOnPhysicalServers();

    /**
     * Clones lxc on a given physical server and set its hostname
     *
     * @param physicalAgent - physical server
     * @param lxcHostname - hostname to set for a new lxc
     *
     * @return true if all went ok, false otherwise
     */
    public boolean cloneLxcOnHost( Agent physicalAgent, String lxcHostname );


    /**
     * Returns state of lxc on a given physical server
     *
     * @param physicalAgent - physical server
     * @param lxcHostname - hostname of lxc
     *
     * @return - {@code LxcState}
     */
    public LxcState checkLxcOnHost( Agent physicalAgent, String lxcHostname );


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
     * Destroys lxc on a given physical server
     *
     * @param physicalAgent - physical server
     * @param lxcHostname - hostname of lxc
     *
     * @return true if all went ok, false otherwise
     */
    public boolean destroyLxcOnHost( Agent physicalAgent, String lxcHostname );

    /**
     * Clones and starts lxc on a given physical server, sets hostname of lxc
     *
     * @param physicalAgent - physical server
     * @param lxcHostname - hostname of lxc
     *
     * @return boolean if all went ok, false otherwise
     */
    public boolean cloneNStartLxcOnHost( Agent physicalAgent, String lxcHostname );

    /**
     * Creates specified number of lxs and starts them. Uses default placement strategy for calculating location of lxcs
     * on physical servers
     *
     * @param count - number of lcxs to create
     *
     * @return map where key is physical agent and value is a set of lxc agents on it
     */
    public Map<Agent, Set<Agent>> createLxcs( int count ) throws LxcCreateException;

    /**
     * Destroys specified lxcs
     *
     * @param agentFamilies - map where key is physical agent and values is a set of lxc children's hostnames
     */
    public void destroyLxcsByHostname( Map<Agent, Set<String>> agentFamilies ) throws LxcDestroyException;

    /**
     * Destroys specified lxcs
     *
     * @param agentFamilies - map where key is physical agent and values is a set of lxc children
     */
    public void destroyLxcs( Map<Agent, Set<Agent>> agentFamilies ) throws LxcDestroyException;

    /**
     * Destroys specified lxcs
     *
     * @param lxcAgents - set of lxc agents
     */
    public void destroyLxcs( Set<Agent> lxcAgents ) throws LxcDestroyException;

    /**
     * Destroys specified lxcs
     *
     * @param lxcAgentHostnames - set of lxc agents' hostnames
     */
    public void destroyLxcsByHostname( Set<String> lxcAgentHostnames ) throws LxcDestroyException;

    /**
     * Creates lxcs baed on a supplied strategy.
     *
     * @param strategy - strategy to use for lxc placement
     *
     * @return map where key is type of node and values is a map where key is a physical server and value is set of lxcs
     * on it
     */
    public Map<String, Map<Agent, Set<Agent>>> createLxcsByStrategy( LxcPlacementStrategy strategy )
            throws LxcCreateException;


    public AgentManager getAgentManager();
}
