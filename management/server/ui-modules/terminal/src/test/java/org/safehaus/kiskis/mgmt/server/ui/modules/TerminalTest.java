/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules;

import com.vaadin.ui.Component;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import static org.junit.Assert.*;
import org.osgi.framework.BundleContext;
import org.safehaus.kiskis.mgmt.server.ui.modules.terminal.Terminal;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleService;

/**
 *
 * @author bahadyr
 */
public class TerminalTest {

    public TerminalTest() {
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
     * Test of getName method, of class Terminal.
     */
//    @Test
    public void testGetName() {
        System.out.println("getName");
        Terminal instance = new Terminal();
        String expResult = "";
        String result = instance.getName();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setModuleService method, of class Terminal.
     */
//    @Test
    public void testSetModuleService() {
        System.out.println("setModuleService");
        ModuleService service = null;
        Terminal instance = new Terminal();
        instance.setModuleService(service);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of unsetModuleService method, of class Terminal.
     */
//    @Test
    public void testUnsetModuleService() {
        System.out.println("unsetModuleService");
        ModuleService service = null;
        Terminal instance = new Terminal();
        instance.unsetModuleService(service);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setContext method, of class Terminal.
     */
//    @Test
//    public void testSetContext() {
//        System.out.println("setContext");
//        BundleContext context = null;
//        Terminal instance = new Terminal();
//        instance.setContext(context);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}
