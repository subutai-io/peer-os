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
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.server.ui.modules.hbase.management.HBaseCommandEnum;
//import org.safehaus.kiskis.mgmt.server.ui.modules.hbase.management.NodesWindow;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManager;
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

    public void runCommand(List<UUID> list, HBaseCommandEnum cce) {
        Task startTask = RequestUtil.createTask("Run command");
        for (UUID uuid : list) {
            Command command = new HBaseCommands().getCommand(cce);
            command.getRequest().setUuid(uuid);
            command.getRequest().setTaskUuid(startTask.getUuid());
            command.getRequest().setRequestSequenceNumber(startTask.getIncrementedReqSeqNumber());
            startTask.addCommand(command);
        }
        tasks.add(startTask);
        start();
    }

    public void runCommand(UUID agentUuid, HBaseCommandEnum cce) {
        Task startTask = RequestUtil.createTask("Run command");
        Command command = new HBaseCommands().getCommand(cce);
        command.getRequest().setUuid(agentUuid);
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

    public void updateSeeds(List<UUID> nodes, String seeds) {
        Task setSeedsTask = RequestUtil.createTask("Update seeds");
        for (UUID agent : nodes) {
//            Command setSeedsCommand = HBaseCommands.getSetSeedsCommand(seeds);
//            setSeedsCommand.getRequest().setUuid(agent);
//            setSeedsCommand.getRequest().setTaskUuid(setSeedsTask.getUuid());
//            setSeedsCommand.getRequest().setRequestSequenceNumber(setSeedsTask.getIncrementedReqSeqNumber());
//            setSeedsTask.addCommand(setSeedsCommand);
        }
        tasks.add(setSeedsTask);
        start();
    }

    public static AgentManager getAgentManager() {
        // get bundle instance via the OSGi Framework Util class
//        BundleContext ctx = FrameworkUtil.getBundle(NodesWindow.class).getBundleContext();
//        if (ctx != null) {
//            ServiceReference serviceReference = ctx.getServiceReference(AgentManager.class.getName());
//            if (serviceReference != null) {
//                return AgentManager.class.cast(ctx.getService(serviceReference));
//            }
//        }

        return null;
    }

}
