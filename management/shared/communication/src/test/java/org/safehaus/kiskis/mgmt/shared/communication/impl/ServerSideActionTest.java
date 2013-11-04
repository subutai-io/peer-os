/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.safehaus.kiskis.mgmt.shared.communication.impl;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.safehaus.kiskismgmt.protocol.Command;
import org.safehaus.kiskismgmt.protocol.Request;
import org.safehaus.kiskismgmt.protocol.Response;

/**
 *
 * @author bahadyr
 */
public class ServerSideActionTest {
    
    public ServerSideActionTest() {
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
     * Test of sendRequestToAgent method, of class ServerSideAction.
     */
    //@Test
    public void testSendRequestToAgent() {
        System.out.println("sendRequestToAgent");
        Request request = null;
        ServerSideAction instance = new ServerSideAction();
        Response expResult = null;
        Response result = instance.sendRequestToAgent(request);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of sendCommandToAgent method, of class ServerSideAction.
     */
    //@Test
    public void testSendCommandToAgent() {
        System.out.println("sendCommandToAgent");
        Request req = new Request();
        req.setUuid("uuid-12345");
        Command command = new Command(req);
        ServerSideAction instance = new ServerSideAction();
        Response expResult = null;
        Response result = instance.sendCommandToAgent(command);
        System.out.println(result.toString());
        assertEquals(1, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of thread method, of class ServerSideAction.
     */
    //@Test
    public void testThread() {
        System.out.println("thread");
        Runnable runnable = null;
        boolean daemon = false;
        ServerSideAction.thread(runnable, daemon);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
