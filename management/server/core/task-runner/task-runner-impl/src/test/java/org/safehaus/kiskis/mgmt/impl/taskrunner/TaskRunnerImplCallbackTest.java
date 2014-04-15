/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.taskrunner;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

import org.safehaus.kiskis.mgmt.api.communicationmanager.ResponseListener;
import org.safehaus.kiskis.mgmt.api.taskrunner.InterruptableTaskCallback;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskCallback;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskRunner;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;

/**
 * @author dilshat
 */
//@Ignore
public class TaskRunnerImplCallbackTest {

    private TaskRunner taskrunner;

    public TaskRunnerImplCallbackTest() {
    }

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

    @Test
    public void testCallbackIsCalledSync() throws InterruptedException {
        final Task task1 = getDummyTask(1);

        final AtomicInteger count = new AtomicInteger(0);

        //execute in a thread since this call blocks until task is completed or times out
        Thread t = new Thread(new Runnable() {

            public void run() {
                taskrunner.executeTaskNWait(task1, new TaskCallback() {

                    public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                        count.incrementAndGet();
                        return null;
                    }
                });
            }
        });

        t.start();

        //wait until background thread is initialized
        Thread.sleep(10);

        ((ResponseListener) taskrunner).onResponse(getDummyResponse(ResponseType.EXECUTE_RESPONSE, null, task1));

        //wait till background thread processes response
        Thread.sleep(10);

        ((ResponseListener) taskrunner).onResponse(getDummyResponse(ResponseType.EXECUTE_RESPONSE_DONE, 0, task1));

        //wait till background thread processes response
        Thread.sleep(10);

        assertEquals(2, count.get());
    }

    @Test
    public void testCallbackIsCalledAsync() throws InterruptedException {
        final Task task1 = getDummyTask(1);

        final AtomicInteger count = new AtomicInteger(0);

        taskrunner.executeTask(task1, new TaskCallback() {

            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                count.incrementAndGet();
                return null;
            }
        });

        //wait until background thread is initialized
        Thread.sleep(10);

        ((ResponseListener) taskrunner).onResponse(getDummyResponse(ResponseType.EXECUTE_RESPONSE, null, task1));

        //wait till background thread processes response
        Thread.sleep(10);

        ((ResponseListener) taskrunner).onResponse(getDummyResponse(ResponseType.EXECUTE_RESPONSE_DONE, 0, task1));

        //wait till background thread processes response
        Thread.sleep(10);

        assertEquals(2, count.get());
    }

    @Test
    public void testInterruptableCallback() throws InterruptedException {

        final Task task1 = getDummyTask(1);

        final AtomicInteger count = new AtomicInteger(0);

        //execute in a thread since this call blocks until task is completed or times out
        Thread t = new Thread(new Runnable() {

            public void run() {
                taskrunner.executeTaskNWait(task1, new InterruptableTaskCallback() {

                    public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                        interrupt();
                        //should not be called
                        if (task.isCompleted()) {
                            count.incrementAndGet();
                        }
                        return null;
                    }
                });
            }
        });

        t.start();

        //wait until background thread is initialized
        Thread.sleep(10);

        ((ResponseListener) taskrunner).onResponse(getDummyResponse(ResponseType.EXECUTE_RESPONSE, null, task1));

        //wait till background thread processes response
        Thread.sleep(10);

        //complete the task - this should not affect the result
        ((ResponseListener) taskrunner).onResponse(getDummyResponse(ResponseType.EXECUTE_RESPONSE_DONE, 0, task1));

        //wait till background thread processes response
        Thread.sleep(10);

        assertEquals(0, count.get());
    }

    @Test
//    @Ignore
    public void testInterruptableCallbackWithChainedTasks() throws InterruptedException {

        final Task task1 = getDummyTask(1);
        final Task task2 = getDummyTask(1);

        final AtomicInteger count = new AtomicInteger(0);

        //execute in a thread since this call blocks until task is completed or times out
        Thread t = new Thread(new Runnable() {

            public void run() {
                taskrunner.executeTaskNWait(task1, new InterruptableTaskCallback() {

                    public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                        if (task.equals(task2)) {
                            interrupt();
                        }
                        //should be called only once
                        if (task.isCompleted()) {
                            count.incrementAndGet();
                            if (task.equals(task1)) {
                                return task2;
                            }
                        }
                        return null;
                    }
                });
            }
        });

        t.start();

        //wait until background thread is initialized
        Thread.sleep(10);

        ((ResponseListener) taskrunner).onResponse(getDummyResponse(ResponseType.EXECUTE_RESPONSE, null, task1));

        //wait till background thread processes response
        Thread.sleep(10);

        //complete the task - this should not affect the result
        ((ResponseListener) taskrunner).onResponse(getDummyResponse(ResponseType.EXECUTE_RESPONSE_DONE, 0, task1));

        //wait till background thread processes response
        Thread.sleep(10);

        ((ResponseListener) taskrunner).onResponse(getDummyResponse(ResponseType.EXECUTE_RESPONSE, null, task2));

        //wait till background thread processes response
        Thread.sleep(10);

        //complete the task - this should not affect the result
        ((ResponseListener) taskrunner).onResponse(getDummyResponse(ResponseType.EXECUTE_RESPONSE_DONE, 0, task2));

        //wait till background thread processes response
        Thread.sleep(10);

        assertEquals(1, count.get());
    }

    @Test
    public void testRemoveCallback() throws InterruptedException {

        final Task task1 = getDummyTask(1);

        final AtomicInteger count = new AtomicInteger(0);

        taskrunner.executeTask(task1, new TaskCallback() {

            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                taskrunner.removeTaskCallback(task.getUuid());
                //should not be called
                if (task.isCompleted()) {
                    count.incrementAndGet();
                }
                return null;
            }
        });

        //wait until background thread is initialized
        Thread.sleep(10);

        ((ResponseListener) taskrunner).onResponse(getDummyResponse(ResponseType.EXECUTE_RESPONSE, null, task1));

        //wait till background thread processes response
        Thread.sleep(10);

        //complete the task - this should not affect the result
        ((ResponseListener) taskrunner).onResponse(getDummyResponse(ResponseType.EXECUTE_RESPONSE_DONE, 0, task1));

        //wait till background thread processes response
        Thread.sleep(10);

        assertEquals(0, count.get());
    }

    @Test
    public void testRemoveCallbackWithChainedTasks() throws InterruptedException {

        final Task task1 = getDummyTask(1);
        final Task task2 = getDummyTask(1);

        final AtomicInteger count = new AtomicInteger(0);

        taskrunner.executeTask(task1, new TaskCallback() {

            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                if (task.equals(task2)) {
                    taskrunner.removeTaskCallback(task.getUuid());
                }
                //should only be called once
                if (task.isCompleted()) {
                    count.incrementAndGet();
                    if (task.equals(task1)) {
                        return task2;
                    }
                }
                return null;
            }
        });

        //wait until background thread is initialized
        Thread.sleep(10);

        ((ResponseListener) taskrunner).onResponse(getDummyResponse(ResponseType.EXECUTE_RESPONSE, null, task1));

        //wait till background thread processes response
        Thread.sleep(10);

        //complete the task - this should not affect the result
        ((ResponseListener) taskrunner).onResponse(getDummyResponse(ResponseType.EXECUTE_RESPONSE_DONE, 0, task1));

        //wait till background thread processes response
        Thread.sleep(10);

        ((ResponseListener) taskrunner).onResponse(getDummyResponse(ResponseType.EXECUTE_RESPONSE, null, task2));

        //wait till background thread processes response
        Thread.sleep(10);

        //complete the task - this should not affect the result
        ((ResponseListener) taskrunner).onResponse(getDummyResponse(ResponseType.EXECUTE_RESPONSE_DONE, 0, task2));

        //wait till background thread processes response
        Thread.sleep(10);

        assertEquals(1, count.get());
    }

    @Test
    public void testChainedTasksAsync() throws InterruptedException {

        final Task task1 = getDummyTask(1);
        final Task task2 = getDummyTask(1);

        final AtomicInteger count = new AtomicInteger(0);

        taskrunner.executeTask(task1, new TaskCallback() {

            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                if (task.equals(task1)) {
                    return task2;
                }
                //should be called only once by task2
                if (task.isCompleted()) {
                    count.incrementAndGet();
                }
                return null;
            }
        });

        //wait until background thread is initialized
        Thread.sleep(10);

        //complete the 1st task - this should not affect the result
        ((ResponseListener) taskrunner).onResponse(getDummyResponse(ResponseType.EXECUTE_RESPONSE_DONE, 0, task1));

        //wait till background thread processes response
        Thread.sleep(10);

        //complete the 2nd task 
        ((ResponseListener) taskrunner).onResponse(getDummyResponse(ResponseType.EXECUTE_RESPONSE_DONE, 0, task2));

        //wait till background thread processes response
        Thread.sleep(10);

        assertEquals(1, count.get());
    }

    @Test
    public void testChainedTasksSync() throws InterruptedException {

        final Task task1 = getDummyTask(1);
        final Task task2 = getDummyTask(1);

        final AtomicInteger count = new AtomicInteger(0);

        //execute in a thread since this call blocks until task is completed or times out
        Thread t = new Thread(new Runnable() {

            public void run() {
                taskrunner.executeTaskNWait(task1, new TaskCallback() {

                    public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                        if (task.equals(task1)) {
                            return task2;
                        }
                        //should be called only once
                        if (task.isCompleted()) {
                            count.incrementAndGet();
                        }
                        return null;
                    }
                });
            }
        });

        t.start();
        //wait until background thread is initialized
        Thread.sleep(10);

        //complete the 1st task - this should not affect the result
        ((ResponseListener) taskrunner).onResponse(getDummyResponse(ResponseType.EXECUTE_RESPONSE_DONE, 0, task1));

        //wait till background thread processes response
        Thread.sleep(10);

        //complete the 2nd task 
        ((ResponseListener) taskrunner).onResponse(getDummyResponse(ResponseType.EXECUTE_RESPONSE_DONE, 0, task2));

        //wait till background thread processes response
        Thread.sleep(10);

        assertEquals(1, count.get());
    }

    @Test
    public void testTaskWithoutCallback() throws InterruptedException {

        final Task task1 = getDummyTask(1);

        //execute in a thread since this call blocks until task is completed or times out
        Thread t = new Thread(new Runnable() {

            public void run() {
                taskrunner.executeTaskNWait(task1);
            }
        });

        t.start();

        //wait until background thread is initialized
        Thread.sleep(10);

        //complete the task
        ((ResponseListener) taskrunner).onResponse(getDummyResponse(ResponseType.EXECUTE_RESPONSE_DONE, 0, task1));

        //wait till background thread processes response
        Thread.sleep(10);

        assertTrue(task1.isCompleted());
    }

}
