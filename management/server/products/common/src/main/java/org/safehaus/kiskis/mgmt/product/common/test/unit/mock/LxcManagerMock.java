package org.safehaus.kiskis.mgmt.product.common.test.unit.mock;


import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcCreateException;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcDestroyException;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcManager;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcPlacementStrategy;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcState;
import org.safehaus.kiskis.mgmt.api.lxcmanager.ServerMetric;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;


public class LxcManagerMock implements LxcManager {

    private Map<Agent, Set<Agent>> mockLxcMap = new HashMap<Agent, Set<Agent>>();


    public LxcManagerMock setMockLxcMap( Map<Agent, Set<Agent>> mockLxcMap ) {
        this.mockLxcMap = mockLxcMap;
        return this;
    }


    @Override
    public Map<Agent, Set<Agent>> createLxcs( int count ) throws LxcCreateException {
        return mockLxcMap;
    }


    @Override
    public Map<Agent, Integer> getPhysicalServersWithLxcSlots() {
        return null;
    }


    @Override
    public Map<Agent, ServerMetric> getPhysicalServerMetrics() {
        return null;
    }


    @Override
    public Map<String, EnumMap<LxcState, List<String>>> getLxcOnPhysicalServers() {
        return null;
    }


    @Override
    public boolean cloneLxcOnHost( Agent physicalAgent, String lxcHostname ) {
        return false;
    }


    @Override
    public boolean startLxcOnHost( Agent physicalAgent, String lxcHostname ) {
        return false;
    }


    @Override
    public boolean stopLxcOnHost( Agent physicalAgent, String lxcHostname ) {
        return false;
    }


    @Override
    public boolean destroyLxcOnHost( Agent physicalAgent, String lxcHostname ) {
        return false;
    }


    @Override
    public boolean cloneNStartLxcOnHost( Agent physicalAgent, String lxcHostname ) {
        return false;
    }


    @Override
    public void destroyLxcsByHostname( Map<Agent, Set<String>> agentFamilies ) throws LxcDestroyException {

    }


    @Override
    public void destroyLxcs( Map<Agent, Set<Agent>> agentFamilies ) throws LxcDestroyException {

    }


    @Override
    public void destroyLxcs( Set<Agent> lxcAgents ) throws LxcDestroyException {

    }


    @Override
    public void destroyLxcsByHostname( Set<String> lxcAgentHostnames ) throws LxcDestroyException {

    }


    @Override
    public Map<String, Map<Agent, Set<Agent>>> createLxcsByStrategy( LxcPlacementStrategy strategy )
            throws LxcCreateException {
        return null;
    }
}
