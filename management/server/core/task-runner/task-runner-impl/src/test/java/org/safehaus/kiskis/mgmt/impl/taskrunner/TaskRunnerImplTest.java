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
import org.safehaus.kiskis.mgmt.api.communicationmanager.ResponseListener;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskCallback;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskRunner;
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

    TaskRunner taskrunner;

    public TaskRunnerImplTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        taskrunner = new TaskRunnerImpl();
        ((TaskRunnerImpl) taskrunner).setCommunicationService(new CommunicationManagerMock());
        ((TaskRunnerImpl) taskrunner).init();
    }

    @After
    public void tearDown() {
        ((TaskRunnerImpl) taskrunner).destroy();
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

    @Test(expected = RuntimeException.class)
    public void testExecuteNullTask() {
        taskrunner.executeTask(null);
    }

    @Test(expected = RuntimeException.class)
    public void testExecuteEmptyTask() {
        Task task = new Task();
        taskrunner.executeTask(task);;
    }

    @Test
    public void testCheckTimedOutTask() {

        Task task = new Task();
        UUID agentID = UUID.randomUUID();
        Agent agent = new Agent(agentID, "testhost");
        Request req = getRequestTemplate();
        req.setTimeout(1);
        task.addRequest(req, agent);
        taskrunner.executeTask(task);

        assertEquals(task.getTaskStatus(), TaskStatus.TIMEDOUT);
    }

    @Test
    public void testFailedTaskAsync() throws InterruptedException {

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

        taskrunner.executeTask(task, new TaskCallback() {

            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                return null;
            }
        });

        Thread.sleep(100);
        ((ResponseListener) taskrunner).onResponse(response);

        Thread.sleep(100);

        assertEquals(task.getTaskStatus(), TaskStatus.FAIL);
    }

    @Test
    public void testExecuteSucceededTaskAsync() throws InterruptedException {

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

        taskrunner.executeTask(task, new TaskCallback() {

            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                return null;
            }
        });

        Thread.sleep(100);
        ((ResponseListener) taskrunner).onResponse(response);

        Thread.sleep(100);

        assertEquals(task.getTaskStatus(), TaskStatus.SUCCESS);
    }

    @Test
    public void testExecuteSucceededTaskSync() throws InterruptedException {

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
                    ((ResponseListener) taskrunner).onResponse(response);
                } catch (InterruptedException ex) {
                }
            }
        });

        t.start();

        taskrunner.executeTask(task);

        Thread.sleep(200);

        assertEquals(task.getTaskStatus(), TaskStatus.SUCCESS);
    }

}
