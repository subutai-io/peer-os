/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


package org.safehaus.kiskis.mgmt.server.ui.modules.oozie.wizard.exec;

import org.safehaus.kiskis.mgmt.server.ui.modules.oozie.commands.OozieCommands;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManager;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import org.safehaus.kiskis.mgmt.server.ui.modules.oozie.management.OozieCommandEnum;
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

    public void runCommand(Set<Agent> agents, OozieCommandEnum cce) {
        Task startTask = RequestUtil.createTask("Run command");
        for (Agent agent : agents) {
            Command command = new OozieCommands().getCommand(cce);
            command.getRequest().setUuid(agent.getUuid());
            command.getRequest().setTaskUuid(startTask.getUuid());
            command.getRequest().setRequestSequenceNumber(startTask.getIncrementedReqSeqNumber());
            startTask.addCommand(command);
        }
        tasks.add(startTask);
        start();
    }

    public void runCommand(Agent agent, OozieCommandEnum cce) {
        Task startTask = RequestUtil.createTask("Run command");
        Command command = new OozieCommands().getCommand(cce);
        command.getRequest().setUuid(agent.getUuid());
        command.getRequest().setTaskUuid(startTask.getUuid());
        command.getRequest().setRequestSequenceNumber(startTask.getIncrementedReqSeqNumber());
        startTask.addCommand(command);
        tasks.add(startTask);
        start();
    }

//    public void purge(OozieConfig config) {
//        Task startTask = RequestUtil.createTask("Run command");
//        for (OozieCommandEnum ce : cce) {
//            Command command = new OozieCommands().getCommand(c);
//            command.getRequest().setUuid(agent.getUuid());
//            command.getRequest().setTaskUuid(startTask.getUuid());
//            command.getRequest().setRequestSequenceNumber(startTask.getIncrementedReqSeqNumber());
//            startTask.addCommand(command);
//            tasks.add(startTask);
//        }
//        start();
//    }

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
