/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.taskrunner;

import java.util.UUID;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.safehaus.kiskis.mgmt.api.communicationmanager.CommunicationManager;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskCallback;
import org.safehaus.kiskis.mgmt.impl.communicationmanager.CommunicationManagerImpl;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

/**
 *
 * @author dilshat
 */
public class TaskRunnerImplTest {

    CommunicationManager communicationService;

    public TaskRunnerImplTest() {
        communicationService = new CommunicationManagerMock();
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
     * Test of setCommunicationService method, of class TaskRunnerImpl.
     */
    @Test
//    @Ignore
    public void testInitTaskRunner() {
        System.out.println("testInitTaskRunner");
        TaskRunnerImpl instance = new TaskRunnerImpl();
        instance.setCommunicationService(communicationService);
        instance.init();

        instance.destroy();

    }

}
