/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.taskrunner;

import org.junit.*;
import org.safehaus.kiskis.mgmt.api.communicationmanager.ResponseListener;
import org.safehaus.kiskis.mgmt.api.taskrunner.InterruptableTaskCallback;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskCallback;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskRunner;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskStatus;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author dilshat
 */
//@Ignore
public class TaskRunnerImplCallbackTest {

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

    @Test
    public void testFailedTaskCompletion() throws InterruptedException {
        final Task task1 = TestUtils.getTask(1);

        //execute in a thread to simulate async response arrival
        Thread t = new Thread(new Runnable() {

            public void run() {
                try {
                    //wait until task is run
                    Thread.sleep(10);

                    ((ResponseListener) taskrunner).onResponse(TestUtils.getResponse(ResponseType.EXECUTE_RESPONSE, null, task1));

                    //wait till background thread processes response
                    Thread.sleep(10);

                    //complete task
                    ((ResponseListener) taskrunner).onResponse(TestUtils.getResponse(ResponseType.EXECUTE_RESPONSE_DONE, 123, task1));

                    //wait till background thread processes response
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                }
            }
        });
        t.start();

        taskrunner.executeTaskNWait(task1);

        assertTrue(task1.isCompleted());
    }

    @Test
    public void testSuccessfulTaskCompletion() throws InterruptedException {
        final Task task1 = TestUtils.getTask(1);

        //execute in a thread to simulate async response arrival
        Thread t = new Thread(new Runnable() {

            public void run() {
                try {
                    //wait until task is run
                    Thread.sleep(10);

                    ((ResponseListener) taskrunner).onResponse(TestUtils.getResponse(ResponseType.EXECUTE_RESPONSE, null, task1));

                    //wait till background thread processes response
                    Thread.sleep(10);

                    //complete tasks
                    ((ResponseListener) taskrunner).onResponse(TestUtils.getResponse(ResponseType.EXECUTE_RESPONSE_DONE, 0, task1));

                    //wait till background thread processes response
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                }
            }
        });
        t.start();

        taskrunner.executeTaskNWait(task1);

        assertEquals(TaskStatus.SUCCESS, task1.getTaskStatus());
    }

    @Test
    public void testCallbackIsCalledSync() throws InterruptedException {
        final Task task1 = TestUtils.getTask(1);

        //execute in a thread to simulate async response arrival
        Thread t = new Thread(new Runnable() {

            public void run() {
                try {
                    //wait until task is run
                    Thread.sleep(10);

                    ((ResponseListener) taskrunner).onResponse(TestUtils.getResponse(ResponseType.EXECUTE_RESPONSE, null, task1));

                    //wait till background thread processes response
                    Thread.sleep(10);

                    ((ResponseListener) taskrunner).onResponse(TestUtils.getResponse(ResponseType.EXECUTE_RESPONSE_DONE, 0, task1));

                    //wait till background thread processes response
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                }
            }
        });

        t.start();

        final AtomicInteger count = new AtomicInteger(0);
        taskrunner.executeTaskNWait(task1, new TaskCallback() {

            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                count.incrementAndGet();
                return null;
            }
        });

        assertEquals(2, count.get());
    }

    @Test
    public void testCallbackIsCalledAsync() throws InterruptedException {
        final Task task1 = TestUtils.getTask(1);

        final AtomicInteger count = new AtomicInteger(0);

        taskrunner.executeTask(task1, new TaskCallback() {

            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                count.incrementAndGet();
                return null;
            }
        });

        //wait until background thread is initialized
        Thread.sleep(10);

        ((ResponseListener) taskrunner).onResponse(TestUtils.getResponse(ResponseType.EXECUTE_RESPONSE, null, task1));

        //wait till background thread processes response
        Thread.sleep(10);

        ((ResponseListener) taskrunner).onResponse(TestUtils.getResponse(ResponseType.EXECUTE_RESPONSE_DONE, 0, task1));

        //wait till background thread processes response
        Thread.sleep(10);

        assertEquals(2, count.get());
    }

    @Test
    public void testInterruptableCallback() throws InterruptedException {

        final Task task1 = TestUtils.getTask(1);

        //execute in a thread to simulate async response arrival
        Thread t = new Thread(new Runnable() {

            public void run() {
                try {
                    //wait until task is run
                    Thread.sleep(10);

                    ((ResponseListener) taskrunner).onResponse(TestUtils.getResponse(ResponseType.EXECUTE_RESPONSE, null, task1));

                    //wait till background thread processes response
                    Thread.sleep(10);

                    //complete the task - this should not affect the result
                    ((ResponseListener) taskrunner).onResponse(TestUtils.getResponse(ResponseType.EXECUTE_RESPONSE_DONE, 0, task1));

                    //wait till background thread processes response
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                }
            }
        });

        t.start();

        final AtomicInteger count = new AtomicInteger(0);
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

        assertEquals(0, count.get());
    }

    @Test
//    @Ignore
    public void testInterruptableCallbackWithChainedTasks() throws InterruptedException {

        final Task task1 = TestUtils.getTask(1);
        final Task task2 = TestUtils.getTask(1);

        //execute in a thread to simulate async response arrival
        Thread t = new Thread(new Runnable() {

            public void run() {
                try {
                    //wait until task is run
                    Thread.sleep(10);

                    ((ResponseListener) taskrunner).onResponse(TestUtils.getResponse(ResponseType.EXECUTE_RESPONSE, null, task1));

                    //wait till background thread processes response
                    Thread.sleep(10);

                    //complete the task - this should not affect the result
                    ((ResponseListener) taskrunner).onResponse(TestUtils.getResponse(ResponseType.EXECUTE_RESPONSE_DONE, 0, task1));

                    //wait till background thread processes response
                    Thread.sleep(10);

                    ((ResponseListener) taskrunner).onResponse(TestUtils.getResponse(ResponseType.EXECUTE_RESPONSE, null, task2));

                    //wait till background thread processes response
                    Thread.sleep(10);

                    //complete the task - this should not affect the result
                    ((ResponseListener) taskrunner).onResponse(TestUtils.getResponse(ResponseType.EXECUTE_RESPONSE_DONE, 0, task2));

                    //wait till background thread processes response
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                }
            }
        });

        t.start();

        final AtomicInteger count = new AtomicInteger(0);
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

        assertEquals(1, count.get());
    }

    @Test
    public void testRemoveCallback() throws InterruptedException {

        final Task task1 = TestUtils.getTask(1);

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

        ((ResponseListener) taskrunner).onResponse(TestUtils.getResponse(ResponseType.EXECUTE_RESPONSE, null, task1));

        //wait till background thread processes response
        Thread.sleep(10);

        //complete the task - this should not affect the result
        ((ResponseListener) taskrunner).onResponse(TestUtils.getResponse(ResponseType.EXECUTE_RESPONSE_DONE, 0, task1));

        //wait till background thread processes response
        Thread.sleep(10);

        assertEquals(0, count.get());
    }

    @Test
    public void testRemoveCallbackWithChainedTasks() throws InterruptedException {

        final Task task1 = TestUtils.getTask(1);
        final Task task2 = TestUtils.getTask(1);

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

        ((ResponseListener) taskrunner).onResponse(TestUtils.getResponse(ResponseType.EXECUTE_RESPONSE, null, task1));

        //wait till background thread processes response
        Thread.sleep(10);

        //complete the task - this should not affect the result
        ((ResponseListener) taskrunner).onResponse(TestUtils.getResponse(ResponseType.EXECUTE_RESPONSE_DONE, 0, task1));

        //wait till background thread processes response
        Thread.sleep(10);

        ((ResponseListener) taskrunner).onResponse(TestUtils.getResponse(ResponseType.EXECUTE_RESPONSE, null, task2));

        //wait till background thread processes response
        Thread.sleep(10);

        //complete the task - this should not affect the result
        ((ResponseListener) taskrunner).onResponse(TestUtils.getResponse(ResponseType.EXECUTE_RESPONSE_DONE, 0, task2));

        //wait till background thread processes response
        Thread.sleep(10);

        assertEquals(1, count.get());
    }

    @Test
    public void testChainedTasksAsync() throws InterruptedException {

        final Task task1 = TestUtils.getTask(1);
        final Task task2 = TestUtils.getTask(1);

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
        ((ResponseListener) taskrunner).onResponse(TestUtils.getResponse(ResponseType.EXECUTE_RESPONSE_DONE, 0, task1));

        //wait till background thread processes response
        Thread.sleep(10);

        //complete the 2nd task 
        ((ResponseListener) taskrunner).onResponse(TestUtils.getResponse(ResponseType.EXECUTE_RESPONSE_DONE, 0, task2));

        //wait till background thread processes response
        Thread.sleep(10);

        assertEquals(1, count.get());
    }

    @Test
    public void testChainedTasksSync() throws InterruptedException {

        final Task task1 = TestUtils.getTask(1);
        final Task task2 = TestUtils.getTask(1);

        //execute in a thread to simulate async response arrival
        Thread t = new Thread(new Runnable() {

            public void run() {
                try {
                    //wait until task is run
                    Thread.sleep(10);

                    //complete the 1st task - this should not affect the result
                    ((ResponseListener) taskrunner).onResponse(TestUtils.getResponse(ResponseType.EXECUTE_RESPONSE_DONE, 0, task1));

                    //wait till background thread processes response
                    Thread.sleep(10);

                    //complete the 2nd task
                    ((ResponseListener) taskrunner).onResponse(TestUtils.getResponse(ResponseType.EXECUTE_RESPONSE_DONE, 0, task2));

                    //wait till background thread processes response
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                }

            }
        });

        t.start();

        final AtomicInteger count = new AtomicInteger(0);
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

        assertEquals(1, count.get());
    }

    @Test
    public void testTaskWithoutCallback() throws InterruptedException {

        final Task task1 = TestUtils.getTask(1);

        //execute in a thread to simulate async response arrival
        Thread t = new Thread(new Runnable() {

            public void run() {
                try {
                    //wait until task is run
                    Thread.sleep(10);

                    //complete the task
                    ((ResponseListener) taskrunner).onResponse(TestUtils.getResponse(ResponseType.EXECUTE_RESPONSE_DONE, 0, task1));

                    //wait till background thread processes response
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                }
            }
        });

        t.start();

        taskrunner.executeTaskNWait(task1);

        assertTrue(task1.isCompleted());
    }

}
