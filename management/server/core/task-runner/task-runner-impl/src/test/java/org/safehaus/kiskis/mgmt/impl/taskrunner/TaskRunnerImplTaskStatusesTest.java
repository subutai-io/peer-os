/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.taskrunner;

import org.junit.*;
import org.safehaus.kiskis.mgmt.api.communicationmanager.ResponseListener;
import org.safehaus.kiskis.mgmt.api.taskrunner.*;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
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
        ((TaskRunnerImpl) taskrunner).setCommunicationService(new CommunicationManagerStub());
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
        taskrunner.executeTaskNWait(null);
    }

    @Test(expected = RuntimeException.class)
    public void testExecuteNullTaskSync() {
        taskrunner.executeTask(null, dummyCallback);
    }

    @Test(expected = RuntimeException.class)
    public void testExecuteEmptyTaskSync() {
        taskrunner.executeTaskNWait(new Task());
    }

    @Test(expected = RuntimeException.class)
    public void testExecuteEmptyTaskAsync() {
        taskrunner.executeTask(new Task(), dummyCallback);
    }

    @Test
    public void testTimedOutTaskSync() {
        Task task = getDummyTask(1);

        taskrunner.executeTaskNWait(task);

        assertEquals(TaskStatus.TIMEDOUT, task.getTaskStatus());
    }

    @Test
//    @Ignore
    public void testTimedOutTaskAsync() throws InterruptedException {

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

        //complete the task
        ((ResponseListener) taskrunner).onResponse(getDummyResponse(ResponseType.EXECUTE_RESPONSE_DONE, 1, task));

        //wait till background thread processes response
        Thread.sleep(10);

        assertEquals(TaskStatus.FAIL, task.getTaskStatus());
    }

    @Test
    public void testFailedTaskSync() throws InterruptedException {

        final Task task = getDummyTask(1);

        //run in a thread to have delay before response "arrives"
        Thread t = new Thread(new Runnable() {

            public void run() {
                try {
                    //"delay"
                    Thread.sleep(10);
                    //complete the task
                    ((ResponseListener) taskrunner).onResponse(getDummyResponse(ResponseType.EXECUTE_RESPONSE_DONE, 1, task));
                } catch (InterruptedException ex) {
                }
            }
        });

        t.start();

        taskrunner.executeTaskNWait(task);

        //wait till background thread processes response
        Thread.sleep(20);

        assertEquals(TaskStatus.FAIL, task.getTaskStatus());
    }

    @Test
    public void testSucceededTaskAsync() throws InterruptedException {

        Task task = getDummyTask(1);

        taskrunner.executeTask(task, dummyCallback);

        //wait until background thread is initialized
        Thread.sleep(10);

        //complete the task
        ((ResponseListener) taskrunner).onResponse(getDummyResponse(ResponseType.EXECUTE_RESPONSE_DONE, 0, task));

        //wait till background thread processes response
        Thread.sleep(10);

        assertEquals(TaskStatus.SUCCESS, task.getTaskStatus());
    }

    @Test
    public void testSucceededTaskSync() throws InterruptedException {

        final Task task = getDummyTask(1);

        //run in a thread to have delay before response "arrives"
        Thread t = new Thread(new Runnable() {

            public void run() {
                try {
                    //"delay"
                    Thread.sleep(10);
                    //complete the task
                    ((ResponseListener) taskrunner).onResponse(getDummyResponse(ResponseType.EXECUTE_RESPONSE_DONE, 0, task));
                } catch (InterruptedException ex) {
                }
            }
        });

        t.start();

        taskrunner.executeTaskNWait(task);

        //wait till background thread processes response
        Thread.sleep(20);

        assertEquals(TaskStatus.SUCCESS, task.getTaskStatus());
    }

    @Test
    public void testCompletedTaskAsync() throws InterruptedException {

        Task task = getDummyTask(1);

        taskrunner.executeTask(task, dummyCallback);

        //wait until background thread is initialized
        Thread.sleep(10);

        //complete the task
        ((ResponseListener) taskrunner).onResponse(getDummyResponse(ResponseType.EXECUTE_RESPONSE_DONE, 0, task));

        //wait till background thread processes response
        Thread.sleep(10);

        assertTrue(task.isCompleted());
    }

    @Test
    public void testCompletedTaskSync() throws InterruptedException {

        final Task task = getDummyTask(1);

        //run in a thread to have delay before response "arrives"
        Thread t = new Thread(new Runnable() {

            public void run() {
                try {
                    //"delay"
                    Thread.sleep(10);
                    //complete the task
                    ((ResponseListener) taskrunner).onResponse(getDummyResponse(ResponseType.EXECUTE_RESPONSE_DONE, 0, task));
                } catch (InterruptedException ex) {
                }
            }
        });

        t.start();

        taskrunner.executeTaskNWait(task);

        //wait till background thread processes response
        Thread.sleep(20);

        assertTrue(task.isCompleted());
    }

    @Test
    public void testRunningTaskSync() throws InterruptedException {

        final Task task = getDummyTask(1);

        //run in a thread since this call blocks until task completes or times out
        Thread t = new Thread(new Runnable() {

            public void run() {
                taskrunner.executeTaskNWait(task);
            }
        });

        t.start();

        //wait until background thread is initialized
        Thread.sleep(10);

        assertEquals(TaskStatus.RUNNING, task.getTaskStatus());
    }

    @Test
    public void testRunningTaskAsync() throws InterruptedException {

        final Task task = getDummyTask(1);

        taskrunner.executeTask(task, dummyCallback);

        //wait until background thread is initialized
        Thread.sleep(10);

        assertEquals(TaskStatus.RUNNING, task.getTaskStatus());
    }

    @Test
    public void testIgnoreExitCodeAsync() throws InterruptedException {

        Task task = getDummyTask(1);
        task.setIgnoreExitCode(true);

        taskrunner.executeTask(task, dummyCallback);

        //wait until background thread is initialized
        Thread.sleep(10);

        //complete the task
        ((ResponseListener) taskrunner).onResponse(getDummyResponse(ResponseType.EXECUTE_RESPONSE_DONE, 1, task));

        //wait till background thread processes response
        Thread.sleep(10);

        assertEquals(TaskStatus.SUCCESS, task.getTaskStatus());
    }

    @Test
    public void testIgnoreExitCodeSync() throws InterruptedException {

        final Task task = getDummyTask(1);
        task.setIgnoreExitCode(true);

        //run in a thread to have delay before response "arrives"
        Thread t = new Thread(new Runnable() {

            public void run() {
                try {
                    //"delay"
                    Thread.sleep(10);
                    //complete the task
                    ((ResponseListener) taskrunner).onResponse(getDummyResponse(ResponseType.EXECUTE_RESPONSE_DONE, 1, task));
                } catch (InterruptedException ex) {
                }
            }
        });

        t.start();

        taskrunner.executeTaskNWait(task);

        //wait till background thread processes response
        Thread.sleep(20);

        assertEquals(TaskStatus.SUCCESS, task.getTaskStatus());
    }

    @Test
    public void testTaskResults() throws InterruptedException {

        Task task = getDummyTask(1);

        taskrunner.executeTask(task, dummyCallback);

        //wait until background thread is initialized
        Thread.sleep(10);

        Response response = getDummyResponse(ResponseType.EXECUTE_RESPONSE, null, task);
        response.setStdOut("stdout");
        response.setStdErr("stderr");

        //supply outs
        ((ResponseListener) taskrunner).onResponse(response);

        //wait till background thread processes response
        Thread.sleep(10);

        //wait till background thread processes response
        Thread.sleep(10);

        Result result = task.getResults().get(response.getUuid());

        assertEquals("stdout", result.getStdOut());
        assertEquals("stderr", result.getStdErr());
//        assertEquals(new Integer(1), result.getExitCode());
    }

    @Test
    public void testCumulatedTaskResults() throws InterruptedException {

        Task task = getDummyTask(1);

        taskrunner.executeTask(task, dummyCallback);

        //wait until background thread is initialized
        Thread.sleep(10);

        Response response = getDummyResponse(ResponseType.EXECUTE_RESPONSE, null, task);
        response.setStdOut("stdout1");
        response.setStdErr("stderr1");

        //supply outs
        ((ResponseListener) taskrunner).onResponse(response);

        //wait till background thread processes response
        Thread.sleep(10);

        response = getDummyResponse(ResponseType.EXECUTE_RESPONSE, null, task);
        response.setStdOut("stdout2");
        response.setStdErr("stderr2");

        //supply outs
        ((ResponseListener) taskrunner).onResponse(response);

        //wait till background thread processes response
        Thread.sleep(10);

        //wait till background thread processes response
        Thread.sleep(10);

        Result result = task.getResults().get(response.getUuid());

        assertEquals("stdout1stdout2", result.getStdOut());
        assertEquals("stderr1stderr2", result.getStdErr());
    }

    @Test
    public void testCallbackParams() throws InterruptedException {

        Task task = getDummyTask(1);
        final StringBuilder out = new StringBuilder();
        final StringBuilder err = new StringBuilder();
        final List<Task> tasks = new ArrayList<Task>();
        final List<Response> responses = new ArrayList<Response>();

        taskrunner.executeTask(task, new TaskCallback() {

            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                out.append(stdOut);
                err.append(stdErr);
                tasks.add(task);
                responses.add(response);
                return null;
            }
        });

        //wait until background thread is initialized
        Thread.sleep(10);

        Response response = getDummyResponse(ResponseType.EXECUTE_RESPONSE, null, task);
        response.setStdOut("stdout1");
        response.setStdErr("stderr1");

        //supply outs
        ((ResponseListener) taskrunner).onResponse(response);

        //wait till background thread processes response
        Thread.sleep(10);

        assertEquals("stdout1", out.toString());
        assertEquals("stderr1", err.toString());
        assertEquals(task, tasks.get(0));
        assertEquals(response, responses.get(0));
    }
}
