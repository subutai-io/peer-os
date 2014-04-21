/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.taskrunner;

import org.junit.*;
import org.safehaus.kiskis.mgmt.api.communicationmanager.ResponseListener;
import org.safehaus.kiskis.mgmt.api.taskrunner.*;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author dilshat
 */
//@Ignore
public class TaskRunnerImplTaskStatusesTest {

    private static TaskRunner taskrunner;

    @BeforeClass
    public static void setUpClass() {
        taskrunner = new TaskRunnerImpl();
        ((TaskRunnerImpl) taskrunner).setCommunicationService(new CommunicationManagerStub());
        ((TaskRunnerImpl) taskrunner).init();
    }

    @AfterClass
    public static void tearDownClass() {
        ((TaskRunnerImpl) taskrunner).destroy();
    }

    @Test(expected = RuntimeException.class)
    public void testExecuteNullTaskAsync() {
        taskrunner.executeTaskNWait(null);
    }

    @Test(expected = RuntimeException.class)
    public void testExecuteNullTaskSync() {
        taskrunner.executeTask(null, TestUtils.getCallback());
    }

    @Test(expected = RuntimeException.class)
    public void testExecuteEmptyTaskSync() {
        taskrunner.executeTaskNWait(new Task());
    }

    @Test(expected = RuntimeException.class)
    public void testExecuteEmptyTaskAsync() {
        taskrunner.executeTask(new Task(), TestUtils.getCallback());
    }

    @Test
    public void testTimedOutTaskSync() {
        Task task = TestUtils.getTask(1);

        taskrunner.executeTaskNWait(task);

        assertEquals(TaskStatus.TIMEDOUT, task.getTaskStatus());
    }

    @Test
//    @Ignore
    public void testTimedOutTaskAsync() throws InterruptedException {

        Task task = TestUtils.getTask(1);

        taskrunner.executeTask(task, TestUtils.getCallback());

        //sleep 1610 since task is cached for timeout + 500ms just in case and evictor runs every 100 ms
        Thread.sleep(1610);

        assertEquals(TaskStatus.TIMEDOUT, task.getTaskStatus());
    }

    @Test
    public void testFailedTaskAsync() throws InterruptedException {

        Task task = TestUtils.getTask(1);

        taskrunner.executeTask(task, TestUtils.getCallback());

        //wait until background thread is initialized
        Thread.sleep(10);

        //complete the task
        ((ResponseListener) taskrunner).onResponse(TestUtils.getResponse(ResponseType.EXECUTE_RESPONSE_DONE, 1, task));

        //wait till background thread processes response
        Thread.sleep(10);

        assertEquals(TaskStatus.FAIL, task.getTaskStatus());
    }

    @Test
    public void testFailedTaskSync() throws InterruptedException {

        final Task task = TestUtils.getTask(1);

        //run in a thread to have delay before response "arrives"
        Thread t = new Thread(new Runnable() {

            public void run() {
                try {
                    //"delay"
                    Thread.sleep(10);
                    //complete the task
                    ((ResponseListener) taskrunner).onResponse(TestUtils.getResponse(ResponseType.EXECUTE_RESPONSE_DONE, 1, task));
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

        Task task = TestUtils.getTask(1);

        taskrunner.executeTask(task, TestUtils.getCallback());

        //wait until background thread is initialized
        Thread.sleep(10);

        //complete the task
        ((ResponseListener) taskrunner).onResponse(TestUtils.getResponse(ResponseType.EXECUTE_RESPONSE_DONE, 0, task));

        //wait till background thread processes response
        Thread.sleep(10);

        assertEquals(TaskStatus.SUCCESS, task.getTaskStatus());
    }

    @Test
    public void testSucceededTaskSync() throws InterruptedException {

        final Task task = TestUtils.getTask(1);

        //run in a thread to have delay before response "arrives"
        Thread t = new Thread(new Runnable() {

            public void run() {
                try {
                    //"delay"
                    Thread.sleep(10);
                    //complete the task
                    ((ResponseListener) taskrunner).onResponse(TestUtils.getResponse(ResponseType.EXECUTE_RESPONSE_DONE, 0, task));
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

        Task task = TestUtils.getTask(1);

        taskrunner.executeTask(task, TestUtils.getCallback());

        //wait until background thread is initialized
        Thread.sleep(10);

        //complete the task
        ((ResponseListener) taskrunner).onResponse(TestUtils.getResponse(ResponseType.EXECUTE_RESPONSE_DONE, 0, task));

        //wait till background thread processes response
        Thread.sleep(10);

        assertTrue(task.isCompleted());
    }

    @Test
    public void testCompletedTaskSync() throws InterruptedException {

        final Task task = TestUtils.getTask(1);

        //run in a thread to have delay before response "arrives"
        Thread t = new Thread(new Runnable() {

            public void run() {
                try {
                    //"delay"
                    Thread.sleep(10);
                    //complete the task
                    ((ResponseListener) taskrunner).onResponse(TestUtils.getResponse(ResponseType.EXECUTE_RESPONSE_DONE, 0, task));
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

        final Task task = TestUtils.getTask(1);

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

        final Task task = TestUtils.getTask(1);

        taskrunner.executeTask(task, TestUtils.getCallback());

        //wait until background thread is initialized
        Thread.sleep(10);

        assertEquals(TaskStatus.RUNNING, task.getTaskStatus());
    }

    @Test
    public void testIgnoreExitCodeAsync() throws InterruptedException {

        Task task = TestUtils.getTask(1);
        task.setIgnoreExitCode(true);

        taskrunner.executeTask(task, TestUtils.getCallback());

        //wait until background thread is initialized
        Thread.sleep(10);

        //complete the task
        ((ResponseListener) taskrunner).onResponse(TestUtils.getResponse(ResponseType.EXECUTE_RESPONSE_DONE, 1, task));

        //wait till background thread processes response
        Thread.sleep(10);

        assertEquals(TaskStatus.SUCCESS, task.getTaskStatus());
    }

    @Test
    public void testIgnoreExitCodeSync() throws InterruptedException {

        final Task task = TestUtils.getTask(1);
        task.setIgnoreExitCode(true);

        //run in a thread to have delay before response "arrives"
        Thread t = new Thread(new Runnable() {

            public void run() {
                try {
                    //"delay"
                    Thread.sleep(10);
                    //complete the task
                    ((ResponseListener) taskrunner).onResponse(TestUtils.getResponse(ResponseType.EXECUTE_RESPONSE_DONE, 1, task));
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

        Task task = TestUtils.getTask(1);

        taskrunner.executeTask(task, TestUtils.getCallback());

        //wait until background thread is initialized
        Thread.sleep(10);

        Response response = TestUtils.getResponse(ResponseType.EXECUTE_RESPONSE, null, task);
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

        Task task = TestUtils.getTask(1);

        taskrunner.executeTask(task, TestUtils.getCallback());

        //wait until background thread is initialized
        Thread.sleep(10);

        Response response = TestUtils.getResponse(ResponseType.EXECUTE_RESPONSE, null, task);
        response.setStdOut("stdout1");
        response.setStdErr("stderr1");

        //supply outs
        ((ResponseListener) taskrunner).onResponse(response);

        //wait till background thread processes response
        Thread.sleep(10);

        response = TestUtils.getResponse(ResponseType.EXECUTE_RESPONSE, null, task);
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

        Task task = TestUtils.getTask(1);
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

        Response response = TestUtils.getResponse(ResponseType.EXECUTE_RESPONSE, null, task);
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
