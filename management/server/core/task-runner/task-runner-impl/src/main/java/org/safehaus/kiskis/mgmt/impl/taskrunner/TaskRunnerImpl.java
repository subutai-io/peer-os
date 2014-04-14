/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.taskrunner;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskCallback;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskRunner;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.api.communicationmanager.CommunicationManager;
import org.safehaus.kiskis.mgmt.api.communicationmanager.ResponseListener;
import org.safehaus.kiskis.mgmt.api.taskrunner.InterruptableTaskCallback;

/**
 * Implementation of {@code TaskRunner} interface.
 *
 *
 * @author dilshat
 */
public class TaskRunnerImpl implements ResponseListener, TaskRunner {

    private static final Logger LOG = Logger.getLogger(TaskRunnerImpl.class.getName());

    /**
     * reference to communication manager service
     */
    private CommunicationManager communicationService;
    /**
     * taskMediator
     */
    private TaskMediator taskMediator;

    /**
     * map of executors for processing responses and triggering corresponding
     * {@code TaskCallback} where key is UUID of agent/node
     */
    private ExecutorService executor;
    private ExpiringCache<UUID, ExecutorService> taskExecutors;

    public void setCommunicationService(CommunicationManager communicationService) {
        this.communicationService = communicationService;
    }

    /**
     * Initializes TaskRunnerImpl.
     */
    public void init() {
        try {
            if (communicationService != null) {
                executor = Executors.newCachedThreadPool();
                taskExecutors = new ExpiringCache<UUID, ExecutorService>(executor);
                taskMediator = new TaskMediator(communicationService, executor);
                communicationService.addListener(this);
                LOG.info(TaskRunner.MODULE_NAME + " started");
            } else {
                throw new Exception("Missing CommunicationManager service");
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in init", e);
        }
    }

    /**
     * Disposes TaskRunnerImpl.
     */
    public void destroy() {
        try {
            Map<UUID, CacheEntry<ExecutorService>> entries = taskExecutors.getEntries();

            for (Map.Entry<UUID, CacheEntry<ExecutorService>> entry : entries.entrySet()) {
                try {
                    entry.getValue().getValue().shutdown();
                } catch (Exception e) {
                }
            }
            taskExecutors.clear();
            taskMediator.removeAllTaskCallbacks();
            if (communicationService != null) {
                communicationService.removeListener(this);
            }
            executor.shutdown();
            LOG.info(MODULE_NAME + " stopped");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in destroy", e);
        }
    }

    /**
     * This method is called by communication manager whenever a new response
     * arrives from agents/nodes
     *
     * @param response - response from node
     */
    @Override
    public void onResponse(Response response) {
        switch (response.getType()) {
            case EXECUTE_TIMEOUTED:
            case EXECUTE_RESPONSE:
            case EXECUTE_RESPONSE_DONE: {
                processResponse(response);
                break;
            }
            default: {
                break;
            }
        }

    }

    /**
     * This method processes responses from agents/nodes.
     *
     * For each response its single threaded executor is retrieved from
     * executors map. The response is submitted for further processing by means
     * of this executor. If task completes or new task is submitted by its
     * {@code TaskCallback} the current executor gets disposed. For new task new
     * executor is bootstrapped.
     *
     *
     * @param response - response from node
     */
    private void processResponse(final Response response) {
        final ExecutorService taskExecutor = taskExecutors.get(response.getTaskUuid());
        if (taskExecutor != null) {
            try {
                taskExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            TaskListener tl = taskMediator.feedResponse(response);
                            if (tl == null || tl.getTask().isCompleted()) {
                                taskExecutors.remove(response.getTaskUuid());
                                taskExecutor.shutdown();

                            } else if (tl.getTask().getUuid().compareTo(response.getTaskUuid()) != 0) {
                                taskExecutors.remove(response.getTaskUuid());
                                taskExecutor.shutdown();

                                //run new task
                                executeTask(tl.getTask(), tl.getTaskCallback());
                            }
                        } catch (Exception e) {
                        }
                    }
                });
            } catch (Exception e) {
            }
        }
    }

    /**
     * Executes {@code Task} asynchronously to the calling party. The supplied
     * {@code TaskCallBack} is triggered every time a response is received for
     * this task. If null for callback is supplied, then no further processing
     * or responses for this task is done. This methods adds response for
     * processing to the queue of corresponding executor. This guarantees order
     * of processing of responses for each particular task without much overhead
     * of synchronizing various threads in thread pools. If task never
     * completes, it gets expired and its executor is disposed at the moment
     * when expiry callback for this executor is called by {@code ExpiringCache}
     *
     * @param task
     * @param taskCallback
     */
    @Override
    public void executeTask(final Task task, final TaskCallback taskCallback) {
        if (task == null) {
            throw new RuntimeException("Task is null");
        }
        if (task.getRequests().isEmpty()) {
            throw new RuntimeException("Task has no requests");
        }

        ExecutorService taskExecutor = taskExecutors.get(task.getUuid());
        if (taskExecutor == null) {
            taskExecutor = Executors.newSingleThreadExecutor();
            taskExecutors.put(task.getUuid(), taskExecutor, task.getAvgTimeout() * 1000 + 500, new EntryExpiryCallback<ExecutorService>() {

                @Override
                public void onEntryExpiry(ExecutorService entry) {
                    try {
                        entry.shutdown();

                    } catch (Exception e) {
                    }
                }
            });
        }

        taskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                taskMediator.executeTask(task, taskCallback);
            }
        });
    }

    /**
     * Removes {@code TaskCallback} for the supplied task UUID. Should be used
     * only with asynchronous executeTask
     *
     * @param taskUUID
     */
    @Override
    public void removeTaskCallback(UUID taskUUID) {
        try {
            taskMediator.removeTaskCallback(taskUUID);
            ExecutorService taskExecutor = taskExecutors.remove(taskUUID);
            if (taskExecutor != null) {
                taskExecutor.shutdown();
            }
        } catch (Exception e) {
        }
    }

    /**
     * Executes {@code Task} synchronously to the calling party. The method
     * returns when either task is completed or timed out. This method waits 1
     * hour maximum and them times out. Calling party should examine the
     * returned/supplied task to see its status after this method returns.
     *
     * @param task - task to execute
     * @return task which is supplied when calling this method;
     */
    @Override
    public Task executeTaskNWait(Task task) {
        TaskCallback callback = new TaskCallback() {

            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                //dummy callback
                return null;
            }
        };

        executeTaskNWait(task, callback);

        return task;
    }

    /**
     * Executes {@code Task} synchronously to the calling party. The method
     * returns when either task is completed or timed out. This method waits 1
     * hour maximum and them times out. Calling party should examine the
     * returned/supplied task to see its status after this method returns.
     *
     * @param task - task to execute
     * @param callback - task callback
     */
    @Override
    public void executeTaskNWait(Task task, TaskCallback callback) {
        if (callback != null) {
            executeTask(task, callback);

            synchronized (callback) {
                try {
                    callback.wait(3600 * 1000); //wait 1 hr maximum
                } catch (InterruptedException ex) {
                }
            }

            removeTaskCallback(task.getUuid());
        } else {
            throw new RuntimeException("Callback is null");
        }
    }

    /**
     * Executes {@code Task} synchronously to the calling party. The method
     * returns when either task is completed or timed out. This method waits 1
     * hour maximum and them times out. Calling party should examine the
     * returned/supplied task to see its status after this method returns.
     *
     * @param task - task to execute
     * @param callback - task callback
     */
    @Override
    public void executeTaskNWait(Task task, final InterruptableTaskCallback interruptableCallback) {
        if (interruptableCallback != null) {
            executeTask(task, interruptableCallback.getCallback());

            synchronized (interruptableCallback.getCallback()) {
                try {
                    interruptableCallback.getCallback().wait(3600 * 1000); //wait 1 hr maximum
                } catch (InterruptedException ex) {
                }
            }
            if (interruptableCallback.getLastTask() != null) {
                removeTaskCallback(interruptableCallback.getLastTask().getUuid());
            } else {
                removeTaskCallback(task.getUuid());
            }
        } else {
            throw new RuntimeException("Callback is null");
        }
    }

}
