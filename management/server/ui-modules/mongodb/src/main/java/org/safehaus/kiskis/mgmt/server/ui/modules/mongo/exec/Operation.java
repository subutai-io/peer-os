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
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.ServiceLocator;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ResponseListener;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;
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
    private int commandCount = 0, okCommandCount = 0;
    private final TaskState taskState = new TaskState();

    public Operation(final String description) {
        this.description = description;
        this.commandManager = ServiceLocator.getService(CommandManagerInterface.class);
    }

    public boolean start() {
        try {
            if (!failed) {
                if (stopped) {
                    if (!isCompleted()) {
                        stopped = false;
                        executeNextTask();
                        onOperationStarted();
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
            if (stopped || failed || isCompleted()) {
                return;
            }

            Task currentTask = tasks.get(++currentTaskIdx);

            if (currentTask != null && currentTask.getCommands() != null
                    && !currentTask.getCommands().isEmpty()) {
                onBeforeTaskRun(currentTask);
                commandCount = 0;
                okCommandCount = 0;
                for (Command cmd : currentTask.getCommands()) {
                    commandManager.executeCommand(cmd);
                }
                onAfterTaskRun(currentTask);
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in executeNextTask", e);
        }
    }

    public boolean isCompleted() {
        return currentTaskIdx >= tasks.size() - 1;
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

    protected void processResponse(Response response) {
        taskState.setCompleted(false);
        taskState.setSuccessfull(false);
        if (getCurrentTask() != null && response != null
                && getCurrentTask().getTaskStatus() == TaskStatus.NEW && response.getType() != null
                && getCurrentTask().getUuid() != null && response.getTaskUuid() != null
                && getCurrentTask().getUuid().compareTo(response.getTaskUuid()) == 0) {
            if (response.getType() == ResponseType.EXECUTE_RESPONSE_DONE
                    || response.getType() == ResponseType.EXECUTE_TIMEOUTED) {
                commandCount++;
                if (response.getType() == ResponseType.EXECUTE_RESPONSE_DONE
                        && (response.getExitCode() == 0 || getCurrentTask().isIgnoreExitCode())) {
                    okCommandCount++;
                }
                taskState.setCompleted(commandCount == getCurrentTask().getCommands().size());
                taskState.setSuccessfull(commandCount == okCommandCount);
                if (taskState.isCompleted()) {
                    if (taskState.isSuccessfull()) {
                        Util.saveTask(getCurrentTask(), TaskStatus.SUCCESS);
                    } else {
                        Util.saveTask(getCurrentTask(), TaskStatus.FAIL);
                        failed = true;
                    }
                }
            }
        }
    }

    public void onTaskCompleted(Task task) {
    }

    public void onTaskSucceeded(Task task) {
    }

    public void onTaskFailed(Task task) {
    }

    public void onOperationEnded() {
    }

    public void onOperationStarted() {
    }

    public void onOperationStopped() {
    }

    public void onBeforeTaskRun(Task task) {
    }

    public void onAfterTaskRun(Task task) {
    }

    @Override
    public void onResponse(Response response) {
        try {
            processResponse(response);
            clearOutput();
            //task completed
            if (taskState.isCompleted()) {
                onTaskCompleted(getCurrentTask());
                //task succeeded or task is uninstall (which can fail in case product is not found)
                if (taskState.isSuccessfull()) {
                    onTaskSucceeded(getCurrentTask());
                    setOutput(MessageFormat.format(
                            "Task {0} succeeded.",
                            getCurrentTask().getDescription()));
                    //operation is not done yet
                    if (!isCompleted()) {
                        //check if stopped by user
                        if (!isStopped()) {
                            //execute next task
                            executeNextTask();
                            appendOutput(MessageFormat.format(
                                    "Running next task {0}...",
                                    getCurrentTask().getDescription()));
                        } // operation is stopped by user
                        else {
                            onOperationStopped();
                            appendOutput(MessageFormat.format(
                                    "Stopped execution before task {0}.",
                                    getNextTask().getDescription()));
                        }
                        //operation is done
                    } else {
                        onOperationEnded();
                        appendOutput(MessageFormat.format(
                                "Operation \"{0}\" completed successfully.",
                                getDescription()));
                    }
                } else {
                    //task failed -> operation failed & ended
                    onTaskFailed(getCurrentTask());
                    onOperationEnded();
                    setOutput(MessageFormat.format(
                            "Task {0} failed.\n\nOperation \"{1}\" aborted.",
                            getCurrentTask().getDescription(),
                            getDescription()));
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
