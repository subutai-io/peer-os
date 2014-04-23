/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.lxcmanager;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import static org.mockito.Mockito.mock;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandRunner;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcCreateException;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcDestroyException;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcManager;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcState;
import org.safehaus.kiskis.mgmt.api.lxcmanager.ServerMetric;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

/**
 *
 * @author dilshat
 */
@Ignore
public class LxcManagerImplTest {

    private final static LxcManager lxcManager = new LxcManagerImpl();

    @BeforeClass
    public static void setUpClass() {
        ((LxcManagerImpl) lxcManager).setAgentManager(new AgentManagerFake());
        ((LxcManagerImpl) lxcManager).setCommandRunner(mock(CommandRunner.class));
        ((LxcManagerImpl) lxcManager).setMonitor(new MonitorFake());
        ((LxcManagerImpl) lxcManager).init();
    }

    @AfterClass
    public static void tearDownClass() {
        ((LxcManagerImpl) lxcManager).destroy();
    }

    @Test
    public void testCloneLxcOnHost() {

        boolean result = lxcManager.cloneLxcOnHost(TestUtils.getPhysicalAgent(), "blablabla");

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

        boolean result = lxcManager.startLxcOnHost(TestUtils.getPhysicalAgent(), TestUtils.getLxcAgent().getHostname());

        assertTrue(result);
    }

    @Test
    public void testStopLxcOnHost() {

        boolean result = lxcManager.stopLxcOnHost(TestUtils.getPhysicalAgent(), TestUtils.getLxcAgent().getHostname());

        assertTrue(result);
    }

    @Test
    public void testDestroyLxcOnHost() {

        boolean result = lxcManager.destroyLxcOnHost(TestUtils.getPhysicalAgent(), TestUtils.getLxcAgent().getHostname());

        assertTrue(result);
    }

    @Test
    public void testCloneNStartLxcOnHost() {

        boolean result = lxcManager.cloneNStartLxcOnHost(TestUtils.getPhysicalAgent(), TestUtils.getLxcAgent().getHostname());

        assertTrue(result);
    }

    @Test
    public void testDestroyLxcs() {
        boolean error = false;
        try {
            Set<String> lxcHostnames = new HashSet<String>();
            lxcHostnames.add(TestUtils.getLxcAgent().getHostname());
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
