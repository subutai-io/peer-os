/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.async.runner;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.shared.protocol.ExpiringCache;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AsyncTaskRunner;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ChainedTaskCallback;
import org.safehaus.kiskis.mgmt.shared.protocol.api.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommunicationService;
import org.safehaus.kiskis.mgmt.shared.protocol.api.EntryExpiryCallback;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ResponseListener;
import org.safehaus.kiskis.mgmt.shared.protocol.api.TaskCallback;

/**
 *
 * @author dilshat
 */
public class AsyncTaskRunnerImpl implements ResponseListener, AsyncTaskRunner {

    private static final Logger LOG = Logger.getLogger(AsyncTaskRunnerImpl.class.getName());

    private static final String MODULE_NAME = "AsyncRunner";
    private CommunicationService communicationService;
    private ChainedTaskRunner taskRunner;
    private final ExpiringCache<UUID, ExecutorService> executors = new ExpiringCache<UUID, ExecutorService>();

    public void setCommunicationService(CommunicationService communicationService) {
        this.communicationService = communicationService;
    }

    public void init() {
        try {
            if (communicationService != null) {
                taskRunner = new ChainedTaskRunner(communicationService);
                communicationService.addListener(this);
                LOG.info(MODULE_NAME + " started");
            } else {
                throw new Exception("Missing CommandManager service");
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
        }
    }

    @Override
    public void executeTask(final Task task, final ChainedTaskCallback taskCallback) {
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

        for (Command cmd : task.getCommands()) {
            cmd.getRequest().setSource(MODULE_NAME);
        }

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
            ExecutorService executor = executors.get(taskUUID);
            if (executor != null) {
                executor.shutdown();
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void executeTask(Task task, final TaskCallback taskCallback) {
        executeTask(task, new ChainedTaskCallback() {

            @Override
            public Task onResponse(Task task, Response response, String stdOut, String stErr) {
                taskCallback.onResponse(task, response);
                return null;
            }
        });
    }
}
