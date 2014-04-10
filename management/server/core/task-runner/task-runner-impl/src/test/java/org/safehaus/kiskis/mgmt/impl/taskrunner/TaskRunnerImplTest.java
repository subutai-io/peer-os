/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.taskrunner;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;

/**
 *
 * @author dilshat
 */
//@Ignore
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
    @Ignore
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
    @Ignore
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
    @Ignore
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
    @Ignore
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

        assertEquals(task.getTaskStatus(), TaskStatus.TIMEDOUT);
    }

    @Test
    @Ignore
    public void testExecuteTask4() throws InterruptedException {
        System.out.println("testExecuteTask4");
        TaskRunnerImpl instance = new TaskRunnerImpl();
        instance.setCommunicationService(communicationService);
        instance.init();

        Task task = new Task();
        UUID agentID = UUID.randomUUID();
        Agent agent = new Agent(agentID, "testhost");
        Request req = getRequestTemplate();
        req.setTimeout(1);
        task.addRequest(req, agent);

        Response response = new Response();
        response.setUuid(agentID);
        response.setTaskUuid(task.getUuid());
        response.setType(ResponseType.EXECUTE_RESPONSE_DONE);
        response.setExitCode(123);

        instance.executeTask(task, new TaskCallback() {

            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                return null;
            }
        });

        Thread.sleep(100);
        instance.onResponse(response);

        Thread.sleep(100);

        instance.destroy();

        assertEquals(task.getTaskStatus(), TaskStatus.FAIL);
    }

    @Test
//    @Ignore
    public void testExecuteTask5() throws InterruptedException {
        System.out.println("testExecuteTask5");
        TaskRunnerImpl instance = new TaskRunnerImpl();
        instance.setCommunicationService(communicationService);
        instance.init();

        Task task = new Task();
        UUID agentID = UUID.randomUUID();
        Agent agent = new Agent(agentID, "testhost");
        Request req = getRequestTemplate();
        req.setTimeout(1);
        task.addRequest(req, agent);

        Response response = new Response();
        response.setUuid(agentID);
        response.setTaskUuid(task.getUuid());
        response.setType(ResponseType.EXECUTE_RESPONSE_DONE);
        response.setExitCode(0);

        instance.executeTask(task, new TaskCallback() {

            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                return null;
            }
        });

        Thread.sleep(100);
        instance.onResponse(response);

        Thread.sleep(100);

        instance.destroy();

        assertEquals(task.getTaskStatus(), TaskStatus.SUCCESS);
    }

    @Test
//    @Ignore
    public void testExecuteTask6() throws InterruptedException {
        System.out.println("testExecuteTask6");
        final TaskRunnerImpl instance = new TaskRunnerImpl();
        instance.setCommunicationService(communicationService);
        instance.init();

        Task task = new Task();
        UUID agentID = UUID.randomUUID();
        Agent agent = new Agent(agentID, "testhost");
        Request req = getRequestTemplate();
        req.setTimeout(3);
        task.addRequest(req, agent);

        final Response response = new Response();
        response.setUuid(agentID);
        response.setTaskUuid(task.getUuid());
        response.setType(ResponseType.EXECUTE_RESPONSE_DONE);
        response.setExitCode(0);

        Thread t = new Thread(new Runnable() {

            public void run() {
                try {
                    Thread.sleep(100);
                    instance.onResponse(response);
                } catch (InterruptedException ex) {
                }
            }
        });

        t.start();

        instance.executeTask(task);

        Thread.sleep(200);

        instance.destroy();
        System.out.println(task);

        assertEquals(task.getTaskStatus(), TaskStatus.SUCCESS);
    }

}
