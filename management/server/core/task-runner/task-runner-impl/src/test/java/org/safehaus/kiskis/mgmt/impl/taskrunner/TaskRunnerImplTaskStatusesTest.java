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
import org.safehaus.kiskis.mgmt.api.communicationmanager.ResponseListener;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskCallback;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskRunner;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskStatus;
import static org.safehaus.kiskis.mgmt.impl.taskrunner.TaskRunnerImplCallbackTest.getRequestTemplate;
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
public class TaskRunnerImplTaskStatusesTest {

    private TaskRunner taskrunner;
    private final TaskCallback dummyCallback = new TaskCallback() {

        public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
            return null;
        }
    };

    private Task getDummyTask(int timeout) {
        Task task = new Task();
        UUID agentID = UUID.randomUUID();
        Agent agent = new Agent(agentID, "testhost");
        Request req = getRequestTemplate();
        req.setTimeout(timeout);
        task.addRequest(req, agent);
        return task;
    }

    private Response getDummyResponse(ResponseType responseType, Integer exitCode, Task task) {
        Response response = new Response();
        response.setUuid(task.getRequests().get(0).getUuid());
        response.setTaskUuid(task.getUuid());
        response.setType(responseType);
        response.setExitCode(exitCode);

        return response;
    }

    public TaskRunnerImplTaskStatusesTest() {
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
    public void testExecuteNullTaskAsync() {
        taskrunner.executeTask(null);
    }

    @Test(expected = RuntimeException.class)
    public void testExecuteNullTaskSync() {
        taskrunner.executeTask(null, dummyCallback);
    }

    @Test(expected = RuntimeException.class)
    public void testExecuteEmptyTaskSync() {
        taskrunner.executeTask(new Task());;
    }

    @Test(expected = RuntimeException.class)
    public void testExecuteEmptyTaskAsync() {
        taskrunner.executeTask(new Task(), dummyCallback);
    }

    @Test
    public void testCheckTimedOutTaskSync() {
        Task task = getDummyTask(1);

        taskrunner.executeTask(task);

        assertEquals(TaskStatus.TIMEDOUT, task.getTaskStatus());
    }

    @Test
//    @Ignore
    public void testCheckTimedOutTaskAsync() throws InterruptedException {

        Task task = getDummyTask(1);

        taskrunner.executeTask(task, dummyCallback);

        //sleep 1610 since task is cached for timeout + 500ms just in case and evictor runs every 100 ms
        Thread.sleep(1610);

        assertEquals(TaskStatus.TIMEDOUT, task.getTaskStatus());
    }

    @Test
    public void testFailedTaskAsync() throws InterruptedException {

        Task task = getDummyTask(1);

        taskrunner.executeTask(task, dummyCallback);

        //wait until background thread is initialized
        Thread.sleep(10);
        ((ResponseListener) taskrunner).onResponse(getDummyResponse(ResponseType.EXECUTE_RESPONSE_DONE, 1, task));

        //wait till background thread processes response
        Thread.sleep(10);

        assertEquals(TaskStatus.FAIL, task.getTaskStatus());
    }

    @Test
    public void testExecuteSucceededTaskAsync() throws InterruptedException {

        Task task = getDummyTask(1);

        taskrunner.executeTask(task, dummyCallback);

        //wait until background thread is initialized
        Thread.sleep(10);
        ((ResponseListener) taskrunner).onResponse(getDummyResponse(ResponseType.EXECUTE_RESPONSE_DONE, 0, task));

        //wait till background thread processes response
        Thread.sleep(10);

        assertEquals(TaskStatus.SUCCESS, task.getTaskStatus());
    }

    @Test
    public void testExecuteSucceededTaskSync() throws InterruptedException {

        final Task task = getDummyTask(1);

        Thread t = new Thread(new Runnable() {

            public void run() {
                try {
                    Thread.sleep(10);
                    ((ResponseListener) taskrunner).onResponse(getDummyResponse(ResponseType.EXECUTE_RESPONSE_DONE, 0, task));
                } catch (InterruptedException ex) {
                }
            }
        });

        t.start();

        taskrunner.executeTask(task);

        //wait till background thread processes response
        Thread.sleep(20);

        assertEquals(TaskStatus.SUCCESS, task.getTaskStatus());
    }

    @Test
    public void testExecuteFailedTaskSync() throws InterruptedException {

        final Task task = getDummyTask(3);

        Thread t = new Thread(new Runnable() {

            public void run() {
                try {
                    Thread.sleep(10);
                    ((ResponseListener) taskrunner).onResponse(getDummyResponse(ResponseType.EXECUTE_RESPONSE_DONE, 1, task));
                } catch (InterruptedException ex) {
                }
            }
        });

        t.start();

        taskrunner.executeTask(task);

        //wait till background thread processes response
        Thread.sleep(20);

        assertEquals(TaskStatus.FAIL, task.getTaskStatus());
    }

    @Test
    public void testCheckRunningTaskSync() throws InterruptedException {

        final Task task = getDummyTask(3);

        Thread t = new Thread(new Runnable() {

            public void run() {
                taskrunner.executeTask(task);
            }
        });

        t.start();

        //wait until background thread is initialized
        Thread.sleep(10);

        assertEquals(TaskStatus.RUNNING, task.getTaskStatus());
    }

    @Test
    public void testCheckRunningTaskAsync() throws InterruptedException {

        final Task task = getDummyTask(3);

        taskrunner.executeTask(task, dummyCallback);

        //wait until background thread is initialized
        Thread.sleep(10);

        assertEquals(TaskStatus.RUNNING, task.getTaskStatus());
    }

}
