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
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskStatus;
import org.safehaus.kiskis.mgmt.impl.communicationmanager.CommunicationManagerImpl;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;

/**
 *
 * @author dilshat
 */
@Ignore
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

    public static Request getRequestTemplate() {
        return CommandFactory.newRequest(
                RequestType.EXECUTE_REQUEST, // type
                null, //                        !! agent uuid
                null, //                        source
                null, //                        !! task uuid 
                1, //                           !! request sequence number
                "/", //                         cwd
                "pwd", //                        program
                OutputRedirection.RETURN, //    std output redirection 
                OutputRedirection.RETURN, //    std error redirection
                null, //                        stdout capture file path
                null, //                        stderr capture file path
                "root", //                      runas
                null, //                        arg
                null, //                        env vars
                30); //  
    }

    /**
     * Test of testInitDestroyTaskRunner method, of class TaskRunnerImpl.
     */
    @Test
//    @Ignore
    public void testInitDestroyTaskRunner() {
        System.out.println("testInitDestroyTaskRunner");
        TaskRunnerImpl instance = new TaskRunnerImpl();
        instance.setCommunicationService(communicationService);
        instance.init();

        instance.destroy();

    }

    /**
     * Test of testExecuteTask method, of class TaskRunnerImpl.
     */
    @Test(expected = RuntimeException.class)
//    @Ignore
    public void testExecuteTask() {
        System.out.println("testExecuteTask");
        TaskRunnerImpl instance = new TaskRunnerImpl();
        instance.setCommunicationService(communicationService);
        instance.init();

        instance.executeTask(null);

        instance.destroy();

    }

    /**
     * Test of testExecuteTask2 method, of class TaskRunnerImpl.
     */
    @Test(expected = RuntimeException.class)
//    @Ignore
    public void testExecuteTask2() {
        System.out.println("testExecuteTask2");
        TaskRunnerImpl instance = new TaskRunnerImpl();
        instance.setCommunicationService(communicationService);
        instance.init();

        Task task = new Task();
        instance.executeTask(task);

        instance.destroy();

    }

    /**
     * Test of testExecuteTask3 method, of class TaskRunnerImpl.
     */
    @Test
//    @Ignore
    public void testExecuteTask3() {
        System.out.println("testExecuteTask3");
        TaskRunnerImpl instance = new TaskRunnerImpl();
        instance.setCommunicationService(communicationService);
        instance.init();

        Task task = new Task();
        UUID agentID = UUID.randomUUID();
        Agent agent = new Agent(agentID, "testhost");
        Request req = getRequestTemplate();
        req.setTimeout(1);
        task.addRequest(req, agent);
        instance.executeTask(task);

        instance.destroy();

        assertTrue(task.getTaskStatus() == TaskStatus.TIMEDOUT);
    }

}
