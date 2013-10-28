/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.safehaus.kiskis.mgmt.server.broker.impl;

import java.util.List;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import static org.junit.Assert.*;
import org.safehaus.kiskis.mgmt.shared.protocol.commands.CommandEnum;
import org.safehaus.kiskis.mgmt.shared.protocol.products.HadoopProduct;
import org.safehaus.kiskismgmt.protocol.Agent;
import org.safehaus.kiskismgmt.protocol.AgentOutput;
import org.safehaus.kiskismgmt.protocol.Product;
import org.safehaus.kiskismgmt.protocol.Request;
import org.safehaus.kiskismgmt.protocol.Response;

/**
 *
 * @author bahadyr
 * Test class junit comment
 */
public class ResponseStorageTest {
    
    public ResponseStorageTest() {
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
     * Test of getRegisteredHosts method, of class ResponseStorage.
     */
    //@org.junit.Test
    public void testGetRegisteredHosts() {
        System.out.println("getRegisteredHosts");
        ResponseStorage instance = new ResponseStorage();
        Set<Agent> expResult = null;
        Set<Agent> result = instance.getRegisteredHosts();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getRegisteredProducts method, of class ResponseStorage.
     */
    //@org.junit.Test
    public void testGetRegisteredProducts() {
        System.out.println("getRegisteredProducts");
        ResponseStorage instance = new ResponseStorage();
        Set<Product> expResult = null;
        Set<Product> result = instance.getRegisteredProducts();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAgentOutput method, of class ResponseStorage.
     */
    //@org.junit.Test
    public void testGetAgentOutput() {
        System.out.println("getAgentOutput");
        Agent agent = null;
        ResponseStorage instance = new ResponseStorage();
        List<AgentOutput> expResult = null;
        List<AgentOutput> result = instance.getAgentOutput(agent);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of execCommand method, of class ResponseStorage.
     */
    @org.junit.Test
    public void testExecCommand() {
        System.out.println("execCommand");
        Agent agent = new Agent();
        agent.setUuid("86716a44-c302-4b57-bed0-c29426b23879");
        Product product = new HadoopProduct();
        Enum command = CommandEnum.INSTALL;
        ResponseStorage instance = new ResponseStorage();
        Boolean expResult = Boolean.TRUE;
        Boolean result = instance.execCommand(agent, product, (CommandEnum) command);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of sendAgentResponse method, of class ResponseStorage.
     */
    //@org.junit.Test
    public void testSendAgentResponse() {
        System.out.println("sendAgentResponse");
        Response response = null;
        ResponseStorage instance = new ResponseStorage();
        Request expResult = null;
        Request result = instance.sendAgentResponse(response);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
