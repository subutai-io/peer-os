/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.lxcmanager;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

/**
 *
 * @author dilshat
 *
 */
public interface LxcManager {

    public Map<Agent, Integer> getPhysicalServersWithLxcSlots();

    public Map<Agent, ServerMetric> getPhysicalServerMetrics();

    public Map<String, EnumMap<LxcState, List<String>>> getLxcOnPhysicalServers();

    public boolean cloneLxcOnHost(Agent physicalAgent, String lxcHostname);

    public boolean startLxcOnHost(Agent physicalAgent, String lxcHostname);

    public boolean stopLxcOnHost(Agent physicalAgent, String lxcHostname);

    public boolean destroyLxcOnHost(Agent physicalAgent, String lxcHostname);

    public boolean cloneNStartLxcOnHost(Agent physicalAgent, String lxcHostname);

    public Map<Agent, Set<Agent>> createLxcs(int count) throws LxcCreateException;

    public void destroyLxcs(Set<String> lxcHostnames) throws LxcDestroyException;

    public Map<String, Map<Agent, Set<Agent>>> createLxcsByStrategy(LxcPlacementStrategy strategy) throws LxcCreateException;
}
