/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.safehaus.kiskis.mgmt.server.agent;

import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.PersistenceAgentInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.AgentListener;

/**
 *
 * @author bahadyr
 */
public class AgentManagerTest {
    
    public AgentManagerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getRegisteredAgents method, of class AgentManager.
     */
//    @Test
    public void testGetRegisteredAgents() {
        System.out.println("getRegisteredAgents");
        AgentManager instance = new AgentManager();
        Set<Agent> expResult = null;
        Set<Agent> result = instance.getRegisteredAgents();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of registerAgent method, of class AgentManager.
     */
//    @Test
    public void testRegisterAgent() {
        System.out.println("registerAgent");
        Response response = null;
        AgentManager instance = new AgentManager();
        //instance.registerAgent(response);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addListener method, of class AgentManager.
     */
//    @Test
    public void testAddListener() {
        System.out.println("addListener");
        AgentListener listener = null;
        AgentManager instance = new AgentManager();
        instance.addListener(listener);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of removeListener method, of class AgentManager.
     */
//    @Test
    public void testRemoveListener() {
        System.out.println("removeListener");
        AgentListener listener = null;
        AgentManager instance = new AgentManager();
        instance.removeListener(listener);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setPersistenceAgentService method, of class AgentManager.
     */
//    @Test
    public void testSetPersistenceAgentService() {
        System.out.println("setPersistenceAgentService");
        PersistenceAgentInterface persistenceAgent = null;
        AgentManager instance = new AgentManager();
        instance.setPersistenceAgentService(persistenceAgent);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setCommandManagerService method, of class AgentManager.
     */
//    @Test
    public void testSetCommandManagerService() {
        System.out.println("setCommandManagerService");
        CommandManagerInterface commandManager = null;
        AgentManager instance = new AgentManager();
        instance.setCommandManagerService(commandManager);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
