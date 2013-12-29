/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.exec;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.Constants;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.ServiceLocator;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ResponseListener;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

/**
 *
 * @author dilshat
 */
public abstract class Operation implements ResponseListener {

    private static final Logger LOG = Logger.getLogger(Operation.class.getName());

    protected final CommandManagerInterface commandManager;
    private final List<Task> tasks = new ArrayList<Task>();
    private final String description;
    private volatile int currentTaskIdx = -1;
    private volatile boolean stopped = true;
    private volatile boolean failed = false;
    private final StringBuilder output = new StringBuilder();

    public Operation(final String description) {
        this.description = description;
        this.commandManager = ServiceLocator.getService(CommandManagerInterface.class);
    }

    public boolean start() {
        try {
            if (!failed) {
                if (stopped) {
                    if (hasMoreTasks()) {
                        stopped = false;
                        executeNextTask();
                        return true;
                    } else {
                        setOutput("Operation completed. No more tasks to run.");
                    }
                } else {
                    setOutput("Operation is already running");
                }
            } else {
                setOutput("Operation failed. Please start from the beginning.");
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in start", e);
        }
        return false;
    }

    public void stop() {
        stopped = true;
    }

    public boolean isStopped() {
        return stopped;
    }

    public boolean isFailed() {
        return failed;
    }

    public Task getCurrentTask() {
        Task task = null;
        if (currentTaskIdx > -1 && currentTaskIdx < tasks.size()) {
            task = tasks.get(currentTaskIdx);
        }

        return task;
    }

    public Task getPreviousTask() {
        Task task = null;
        if (currentTaskIdx > 0) {
            task = tasks.get(currentTaskIdx - 1);
        }

        return task;
    }

    public Task getNextTask() {
        Task task = null;
        if (currentTaskIdx < tasks.size() - 1) {
            task = tasks.get(currentTaskIdx + 1);
        }

        return task;
    }

    private void executeNextTask() {
        try {
            if (stopped || !hasMoreTasks()) {
                return;
            }

            Task currentTask = tasks.get(++currentTaskIdx);

            if (currentTask != null && currentTask.getCommands() != null
                    && !currentTask.getCommands().isEmpty()) {
                for (Command cmd : currentTask.getCommands()) {
                    commandManager.executeCommand(cmd);
                }
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in executeNextTask", e);
        }
    }

    public boolean hasMoreTasks() {
        return currentTaskIdx < tasks.size() - 1;
    }

    public String getDescription() {
        return description;
    }

    protected void addTask(Task task) {
        tasks.add(task);
    }

    public int getOverallTimeout() {
        int timeout = 0;
        try {
            for (Task task : tasks) {
                for (Command command : task.getCommands()) {
                    timeout += command.getRequest().getTimeout();
                }
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in getOverallTimeout", e);
        }
        return timeout;
    }

    @Override
    public void onResponse(Response response) {
        try {
            clearOutput();

            if (getCurrentTask() != null && getCurrentTask().getTaskStatus() == TaskStatus.NEW && response != null
                    && getCurrentTask().getUuid() != null && response.getTaskUuid() != null
                    && getCurrentTask().getUuid().compareTo(response.getTaskUuid()) == 0) {

                int count = commandManager.getResponseCount(getCurrentTask().getUuid());
                System.out.println("execute " + getCurrentTask().getCommands().size() + "   " + count);
                if (getCurrentTask().getCommands().size() == count) {
                    int okCount = commandManager.getSuccessfullResponseCount(getCurrentTask().getUuid());
                    //task completed
                    if (count == okCount
                            || getCurrentTask().getDescription().equalsIgnoreCase(
                                    Constants.MONGO_UNINSTALL_TASK_NAME)) {
                        //task succeeded
                        System.out.println("execute success");
                        getCurrentTask().setTaskStatus(TaskStatus.SUCCESS);
                        commandManager.saveTask(getCurrentTask());
                        setOutput(MessageFormat.format(
                                "Task {0} succeeded.",
                                getCurrentTask().getDescription()));
                        if (hasMoreTasks()) {
                            System.out.println("execute has tasks");
                            if (!isStopped()) {
                                System.out.println("execute has next");
                                executeNextTask();
                                appendOutput(MessageFormat.format(
                                        "Running next task {0}...",
                                        getCurrentTask().getDescription()));
                            } else {
                                System.out.println("execute stopeed");
                                appendOutput(MessageFormat.format(
                                        "Stopped execution before task {0}.",
                                        getNextTask().getDescription()));
                            }
                        } else {
                            System.out.println("execute done");
                            appendOutput(MessageFormat.format(
                                    "Operation \"{0}\" completed successfully.",
                                    getDescription()));
                        }
                    } else {
                        System.out.println("execute failed");
                        //task failed
                        getCurrentTask().setTaskStatus(TaskStatus.FAIL);
                        commandManager.saveTask(getCurrentTask());
                        failed = true;
                        setOutput(MessageFormat.format(
                                "Task {0} failed.\n\nOperation \"{1}\" aborted.",
                                getCurrentTask().getDescription(),
                                getDescription()));
                    }
                }
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in onResponse", e);
        }
    }

    public String getOutput() {
        return output.toString();
    }

    public void appendOutput(String s) {
        if (output.length() == 0) {
            output.append(s);
        } else {
            output.append("\n\n").append(s);
        }
    }

    public void setOutput(String s) {
        clearOutput();
        appendOutput(s);
    }

    public void clearOutput() {
        output.setLength(0);
    }

    @Override
    public String getSource() {
        return getClass().getName();
    }

}
