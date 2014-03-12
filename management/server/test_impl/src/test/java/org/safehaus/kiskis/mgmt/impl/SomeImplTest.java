/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl;

import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.impl.dbmanager.DbManagerImpl;

/**
 *
 * @author bahadyr
 */
public class SomeImplTest {

    public SomeImplTest() {
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
     * Test of setDbManager method, of class SomeImpl.
     */
//    @Test
    public void testSetDbManager() {
        System.out.println("setDbManager");

        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of writeLog method, of class SomeImpl.
     */
    @Test
    public void testWriteLog() {
        System.out.println("writeLog");
        String log = "some log data";
        DbManager dbManager = new DbManagerImpl();
        dbManager.setCassandraHost("localhost");
        dbManager.setCassandraKeyspace("kiskis");
        dbManager.setCassandraPort(9042);
        dbManager.init();
        SomeImpl instance = new SomeImpl(dbManager);
        boolean b = instance.writeLog(log);
        assertTrue(b);
        dbManager.truncate("logs");
    }


}
