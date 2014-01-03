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
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.ServiceLocator;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
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
    private final StringBuilder log = new StringBuilder();
    private int commandCount = 0, okCommandCount = 0;

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
                        onOperationStarted();
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

    private boolean executeNextTask() {
        boolean result = false;
        try {
            if (!(stopped || failed || isCompleted())) {

                Task currentTask = tasks.get(++currentTaskIdx);

                onBeforeTaskRun(currentTask);

                if (currentTask != null && currentTask.getCommands() != null) {
                    if (!currentTask.getCommands().isEmpty()) {
                        commandCount = 0;
                        okCommandCount = 0;
                        for (Command cmd : currentTask.getCommands()) {
                            commandManager.executeCommand(cmd);
                        }
                        result = true;
                    } else {
                        appendOutput(MessageFormat.format(
                                "Task {0} has no commands",
                                getCurrentTask().getDescription()));
                    }
                } else {
                    appendOutput("Malformed task");
                }

                onAfterTaskRun(currentTask);
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in executeNextTask", e);
        }
        return result;
    }

    public boolean isCompleted() {
        return currentTaskIdx >= tasks.size() - 1;
    }

    public String getDescription() {
        return description;
    }

    protected void addTask(Task task) {
        if (task != null) {
            tasks.add(task);
        }
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
        Task task = getCurrentTask();
//        if (task != null && response != null
//                && task.getTaskStatus() == TaskStatus.NEW && response.getType() != null
//                && task.getUuid() != null && response.getTaskUuid() != null
//                && task.getUuid().compareTo(response.getTaskUuid()) == 0) {
        if (response.getType() == ResponseType.EXECUTE_RESPONSE_DONE
                || response.getType() == ResponseType.EXECUTE_TIMEOUTED) {
            commandCount++;
            if ((response.getType() == ResponseType.EXECUTE_RESPONSE_DONE
                    && response.getExitCode() == 0) || task.isIgnoreExitCode()) {
                okCommandCount++;
            }
            task.setCompleted(commandCount == task.getCommands().size());
            if (task.isCompleted()) {
                if (commandCount == okCommandCount) {
                    task.setTaskStatus(TaskStatus.SUCCESS);
                    Util.saveTask(task);
                } else {
                    task.setTaskStatus(TaskStatus.FAIL);
                    Util.saveTask(task);
                    failed = true;
                }
            }
        }
//        }
    }

    protected void onTaskCompleted(Task task) {
    }

    protected void onTaskSucceeded(Task task) {
    }

    protected void onTaskFailed(Task task) {
    }

    protected void onOperationEnded() {
    }

    protected void onOperationStarted() {
        appendOutput(MessageFormat.format("Operation \"{0}\" started.", getDescription()));
    }

    protected void onOperationStopped() {
    }

    protected void onBeforeTaskRun(Task task) {
        appendOutput(MessageFormat.format(
                "Running task {0}...",
                task.getDescription()));
        appendLog(MessageFormat.format(
                "======= Task {0} =======",
                task.getDescription()));
        if (task.isIgnoreExitCode()) {
            appendLog("======= Ignore ExitCode = TRUE =======");
        }
    }

    protected void onAfterTaskRun(Task task) {
    }

    protected void beforeResponseProcessed(Response response) {

        if (!Util.isStringEmpty(response.getStdOut())) {
            appendLog("StdOut:");
            appendLog(response.getStdOut());
        }
        if (!Util.isStringEmpty(response.getStdErr())) {
            appendLog("StdErr:");
            appendLog(response.getStdErr());
        }
        if (response.getType() == ResponseType.EXECUTE_RESPONSE_DONE) {
            appendLog("Exit Code: " + response.getExitCode());
        }
        if (response.getType() == ResponseType.EXECUTE_TIMEOUTED) {
            appendLog("Command timeouted");
        }
    }

    protected void afterResponseProcessed(Response response) {
    }

    protected void fail() {
        Task task = getCurrentTask();
        if (task != null) {
            task.setTaskStatus(TaskStatus.FAIL);
            Util.saveTask(task);
            failed = true;
        }
    }

    @Override
    public void onResponse(Response response) {
        clearOutput();
        clearLog();
        try {
            Task task = getCurrentTask();
            if (task != null && response != null
                    && task.getTaskStatus() == TaskStatus.NEW && response.getType() != null
                    && task.getUuid() != null && response.getTaskUuid() != null
                    && task.getUuid().compareTo(response.getTaskUuid()) == 0) {

                beforeResponseProcessed(response);

                processResponse(response);

                //task completed
                if (task.isCompleted()) {
                    onTaskCompleted(task);
                    //task succeeded or ignoreExitCode is true
                    if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                        onTaskSucceeded(task);
                        //check in case user failed the task in onTaskSucceeded
                        if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                            setOutput(MessageFormat.format(
                                    "Task {0} succeeded.",
                                    task.getDescription()));
                            //operation is not done yet
                            if (!isCompleted()) {
                                //check if stopped by user
                                if (!isStopped()) {
                                    //execute next task

                                    executeNextTask();
                                } // operation is stopped by user
                                else {
                                    appendOutput(MessageFormat.format(
                                            "Stopped execution before task {0}.",
                                            getNextTask().getDescription()));
                                    onOperationStopped();
                                }
                                //operation is done
                            } else {
                                appendOutput(MessageFormat.format(
                                        "Operation \"{0}\" completed successfully.",
                                        getDescription()));
                                onOperationEnded();
                            }
                        } else {
                            //task failed -> operation failed & ended
                            appendOutput(MessageFormat.format(
                                    "Task {0} failed.\n\nOperation \"{1}\" aborted.",
                                    task.getDescription(),
                                    getDescription()));
                            onTaskFailed(task);
                            onOperationEnded();
                        }
                    } else {
                        //task failed -> operation failed & ended
                        appendOutput(MessageFormat.format(
                                "Task {0} failed.\n\nOperation \"{1}\" aborted.",
                                task.getDescription(),
                                getDescription()));
                        onTaskFailed(task);
                        onOperationEnded();
                    }
                }

                afterResponseProcessed(response);

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

    public String getLog() {
        return log.toString();
    }

    public void appendLog(String s) {
        if (log.length() == 0) {
            log.append(s);
        } else {
            log.append("\n\n").append(s);
        }
    }

    public void setLog(String s) {
        clearLog();
        appendLog(s);
    }

    public void clearLog() {
        log.setLength(0);
    }

    @Override
    public String getSource() {
        return getClass().getName();
    }

}
