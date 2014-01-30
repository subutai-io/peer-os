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
import org.safehaus.kiskis.mgmt.shared.protocol.TaskRunner;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AsyncTaskRunner;
import org.safehaus.kiskis.mgmt.shared.protocol.api.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManager;
import org.safehaus.kiskis.mgmt.shared.protocol.api.EntryExpiryCallback;
import org.safehaus.kiskis.mgmt.shared.protocol.api.TaskCallback;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;

/**
 *
 * @author dilshat
 */
public class AsyncTaskRunnerImpl implements CommandListener, AsyncTaskRunner {

    private static final Logger LOG = Logger.getLogger(AsyncTaskRunnerImpl.class.getName());

    private static final String MODULE_NAME = "AsyncRunner";
    private CommandManager commandManager;
    private TaskRunner taskRunner;
    private final ExpiringCache<UUID, ExecutorService> executors = new ExpiringCache<UUID, ExecutorService>();

    public void setCommandManager(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    public void init() {
        try {
            if (commandManager != null) {
                taskRunner = new TaskRunner(commandManager);
                commandManager.addListener(this);
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
            if (commandManager != null) {
                commandManager.removeListener(this);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in destroy", e);
        }
    }

    @Override
    public void onCommand(final Response response) {
        ExecutorService executor = executors.get(response.getTaskUuid());
        if (executor != null) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    taskRunner.feedResponse(response);
                }
            });
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
                        entry.shutdown();
                    } catch (Exception e) {
                    }
                }
            });
        }

        for (Command cmd : task.getCommands()) {
            cmd.getRequest().setSource(MODULE_NAME);
            System.out.println(cmd.getRequest().getSource());
        }

        executor.execute(new Runnable() {
            @Override
            public void run() {
                taskRunner.runTask(task, taskCallback);
            }
        });

    }

    @Override
    public String getName() {
        return MODULE_NAME;
    }
}
