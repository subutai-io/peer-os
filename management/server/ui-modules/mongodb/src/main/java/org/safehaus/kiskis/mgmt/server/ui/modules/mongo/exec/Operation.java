/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.exec;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.wizard.Wizard;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.ServiceLocator;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;

/**
 *
 * @author dilshat
 */
public abstract class Operation {

    private final Wizard wizard;
    private final List<Task> tasks = new ArrayList<Task>();
    private Iterator<Task> tasksIterator;
    private final String description;
    protected final CommandManagerInterface commandManager;

    public Operation(final Wizard wizard, String description) {
        this.description = description;
        this.wizard = wizard;
        this.commandManager = ServiceLocator.getService(CommandManagerInterface.class);
    }

    public Task executeNextTask() {
        if (tasksIterator == null) {
            tasksIterator = tasks.iterator();
        }
        Task currentTask = null;
        if (tasksIterator.hasNext()) {
            currentTask = tasksIterator.next();
        }
        if (currentTask != null && currentTask.getCommands() != null
                && !currentTask.getCommands().isEmpty()) {
            for (Command cmd : currentTask.getCommands()) {
                commandManager.executeCommand(cmd);
            }
        }
        return currentTask;
    }

    public boolean hasMoreTasks() {
        if (tasksIterator == null) {
            tasksIterator = tasks.iterator();
        }
        return tasksIterator.hasNext();
    }

    public String getDescription() {
        return description;
    }

    protected void addTask(Task task) {
        tasks.add(task);
    }

    public int getOverallTimeout() {
        int timeout = 0;
        for (Task task : tasks) {
            for (Command command : task.getCommands()) {
                timeout += command.getRequest().getTimeout();
            }
        }
        return timeout;
    }

}
