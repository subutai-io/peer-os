/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.taskrunner;

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
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskStatus;

/**
 *
 * @author dilshat
 */
public class TaskRunnerImpl implements ResponseListener, TaskRunner {

    private static final Logger LOG = Logger.getLogger(TaskRunnerImpl.class.getName());

    private CommunicationManager communicationService;
    private ChainedTaskRunner taskRunner;
    private final ExpiringCache<UUID, ExecutorService> executors = new ExpiringCache<UUID, ExecutorService>();

    public void setCommunicationService(CommunicationManager communicationService) {
        this.communicationService = communicationService;
    }

    public void init() {
        try {
            if (communicationService != null) {
                taskRunner = new ChainedTaskRunner(communicationService);
                communicationService.addListener(this);
                LOG.info(MODULE_NAME + " started");
            } else {
                throw new Exception("Missing CommunicationManager service");
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in init", e);
        }
    }

    public void destroy() {
        try {
            executors.clear();
            taskRunner.removeAllTaskCallbacks();
            if (communicationService != null) {
                communicationService.removeListener(this);
            }
            LOG.info(MODULE_NAME + " stopped");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in destroy", e);
        }
    }

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

    private void processResponse(final Response response) {
        final ExecutorService executor = executors.get(response.getTaskUuid());
        if (executor != null) {
            try {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ChainedTaskListener tl = taskRunner.feedResponse(response);
                            if (tl == null || tl.getTask().isCompleted()) {
                                executors.remove(response.getTaskUuid());
                                executor.shutdown();
                            } else if (tl.getTask().getUuid().compareTo(response.getTaskUuid()) != 0) {
                                executors.remove(response.getTaskUuid());
                                executor.shutdown();

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

    @Override
    public void executeTask(final Task task, final TaskCallback taskCallback) {
        ExecutorService executor = executors.get(task.getUuid());
        if (executor == null) {
            executor = Executors.newSingleThreadExecutor();
            executors.put(task.getUuid(), executor, task.getAvgTimeout() * 1000 + 10000, new EntryExpiryCallback<ExecutorService>() {

                @Override
                public void onEntryExpiry(ExecutorService entry) {
                    try {
                        executors.remove(task.getUuid());
                        entry.shutdown();
                    } catch (Exception e) {
                    }
                }
            });
        }

        task.setTaskStatus(TaskStatus.RUNNING);

        executor.execute(new Runnable() {
            @Override
            public void run() {
                taskRunner.executeTask(task, taskCallback);
            }
        });

    }

    @Override
    public void removeTaskCallback(UUID taskUUID) {
        try {
            taskRunner.removeTaskCallback(taskUUID);
            ExecutorService executor = executors.remove(taskUUID);
            if (executor != null) {
                executor.shutdown();
            }
        } catch (Exception e) {
        }
    }

    public Task executeTask(Task task) {
        executeTask(task, new TaskCallback() {

            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                if (task.isCompleted()) {
                    synchronized (task) {
                        task.notifyAll();
                    }
                }
                return null;
            }
        });
        synchronized (task) {
            try {
                task.wait(task.getAvgTimeout() * 1000 + 3000);
            } catch (InterruptedException ex) {
            }
        }

        if (!task.isCompleted()) {
            task.setTaskStatus(TaskStatus.FAIL);
        }

        return task;
    }

    public void executeTaskNForget(Task task) {
        executeTask(task, null);
    }

}
