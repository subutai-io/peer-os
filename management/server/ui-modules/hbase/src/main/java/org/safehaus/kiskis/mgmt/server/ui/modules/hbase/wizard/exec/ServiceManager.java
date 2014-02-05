/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.hbase.wizard.exec;

import org.safehaus.kiskis.mgmt.server.ui.modules.hbase.commands.HBaseCommands;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManager;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import org.safehaus.kiskis.mgmt.server.ui.modules.hbase.management.HBaseCommandEnum;
import org.safehaus.kiskis.mgmt.shared.protocol.api.Command;

/**
 *
 * @author bahadyr
 */
public class ServiceManager {

    private final Queue<Task> tasks = new LinkedList<Task>();
    private Task currentTask;

    public ServiceManager() {
    }

    public void runCommand(Set<Agent> agents, HBaseCommandEnum cce) {
        Task startTask = RequestUtil.createTask("Run command");
        for (Agent agent : agents) {
            Command command = new HBaseCommands().getCommand(cce);
            command.getRequest().setUuid(agent.getUuid());
            command.getRequest().setTaskUuid(startTask.getUuid());
            command.getRequest().setRequestSequenceNumber(startTask.getIncrementedReqSeqNumber());
            startTask.addCommand(command);
        }
        tasks.add(startTask);
        start();
    }

    public void runCommand(Agent agent, HBaseCommandEnum cce) {
        Task startTask = RequestUtil.createTask("Run command");
        Command command = new HBaseCommands().getCommand(cce);
        command.getRequest().setUuid(agent.getUuid());
        command.getRequest().setTaskUuid(startTask.getUuid());
        command.getRequest().setRequestSequenceNumber(startTask.getIncrementedReqSeqNumber());
        startTask.addCommand(command);
        tasks.add(startTask);
        start();
    }

    public void start() {
        moveToNextTask();
        if (currentTask != null) {
            for (Command command : currentTask.getCommands()) {
                executeCommand(command);
            }
        }
    }

    public void moveToNextTask() {
        currentTask = tasks.poll();
    }

    public Task getCurrentTask() {
        return currentTask;
    }

    public void executeCommand(Command command) {
        ServiceLocator.getService(CommandManager.class).executeCommand(command);
    }

}
