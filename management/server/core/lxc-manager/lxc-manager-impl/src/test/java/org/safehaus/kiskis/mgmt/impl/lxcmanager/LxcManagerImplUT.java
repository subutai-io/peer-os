/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.lxcmanager;

import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcCreateException;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcDestroyException;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcManager;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcState;
import org.safehaus.kiskis.mgmt.api.lxcmanager.ServerMetric;
import org.safehaus.kiskis.mgmt.api.monitor.Metric;
import org.safehaus.kiskis.mgmt.api.monitor.Monitor;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

/**
 *
 * @author dilshat
 */
//@Ignore
public class LxcManagerImplUT {

    private final static LxcManager lxcManager = new LxcManagerImpl();

    @Before
    public void setUpMethod() {
        ((LxcManagerImpl) lxcManager).setAgentManager(new AgentManagerFake());
        ((LxcManagerImpl) lxcManager).setCommandRunner(MockUtils.getAutoCommandRunner());
        Monitor monitor = mock(Monitor.class);
        when(monitor.getData(any(String.class), any(Metric.class), any(Date.class), any(Date.class)))
                .thenReturn(Collections.EMPTY_MAP);
        ((LxcManagerImpl) lxcManager).setMonitor(monitor);
        ((LxcManagerImpl) lxcManager).init();
    }

    @After
    public void tearDownMethod() {

        ((LxcManagerImpl) lxcManager).destroy();
    }

    @Test
    public void testCloneLxcOnHost() {

        boolean result = lxcManager.cloneLxcOnHost(MockUtils.getPhysicalAgent(), MockUtils.getLxcAgent().getHostname());

        assertTrue(result);
    }

    @Test
    public void testGetLxcOnPhysicalServers() {

        Map<String, EnumMap<LxcState, List<String>>> result = lxcManager.getLxcOnPhysicalServers();

        assertFalse(result.isEmpty());
    }

    @Test
    public void testGetPhysicalServerMetrics() {

        Map<Agent, ServerMetric> result = lxcManager.getPhysicalServerMetrics();

        assertFalse(result.isEmpty());
    }

    @Test
    public void testGetPhysicalServersWithLxcSlots() {

        Map<Agent, Integer> result = lxcManager.getPhysicalServersWithLxcSlots();

        assertTrue(result.entrySet().iterator().next().getValue() > 0);
    }

    @Test
    public void testStartLxcOnHost() {

        boolean result = lxcManager.startLxcOnHost(MockUtils.getPhysicalAgent(), MockUtils.getLxcAgent().getHostname());

        assertTrue(result);
    }

    @Test
    public void testStopLxcOnHost() {
        ((LxcManagerImpl) lxcManager).setCommandRunner(
                MockUtils.getHardCodedCommandRunner(true, true, 0, "STOPPED", null));

        boolean result = lxcManager.stopLxcOnHost(MockUtils.getPhysicalAgent(), MockUtils.getLxcAgent().getHostname());

        assertTrue(result);
    }

    @Test
    public void testDestroyLxcOnHost() {

        boolean result = lxcManager.destroyLxcOnHost(MockUtils.getPhysicalAgent(), MockUtils.getLxcAgent().getHostname());

        assertTrue(result);
    }

    @Test
    public void testCloneNStartLxcOnHost() {

        boolean result = lxcManager.cloneNStartLxcOnHost(MockUtils.getPhysicalAgent(), MockUtils.getLxcAgent().getHostname());

        assertTrue(result);
    }

    @Test
    public void testDestroyLxcs() {

        boolean error = false;
        try {
            Set<String> lxcHostnames = new HashSet<String>();
            lxcHostnames.add(MockUtils.getLxcAgent().getHostname());
            lxcManager.destroyLxcs(lxcHostnames);
        } catch (LxcDestroyException ex) {
            error = true;
        }
        assertFalse(error);
    }

    @Test
    public void testCreateLxcsByStrategy() throws LxcCreateException {

        Map<String, Map<Agent, Set<Agent>>> agentMap = lxcManager.createLxcsByStrategy(new DefaultLxcPlacementStrategy(1));

        assertFalse(agentMap.isEmpty());
    }

    @Test
    public void testCreateLxcs() throws LxcCreateException {

        Map<Agent, Set<Agent>> agentMap = lxcManager.createLxcs(1);

        assertFalse(agentMap.isEmpty());
    }

}
