/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizard.exec;

import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.commands.CassandraCommands;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.management.CassandraCommandEnum;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.management.NodesWindow;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManagerInterface;

/**
 *
 * @author bahadyr
 */
public class ServiceManager {

    private final Queue<Task> tasks = new LinkedList<Task>();
    private Task currentTask;
    private final TextArea terminal;

    public ServiceManager() {
        this.terminal = null;
    }

    public ServiceManager(TextArea textArea) {
        this.terminal = textArea;
    }

    public void runCommand(List<UUID> list, CassandraCommandEnum cce) {
        Task startTask = RequestUtil.createTask(ServiceLocator.getService(CommandManagerInterface.class), "Start Cassandra");
        for (UUID uuid : list) {
            Command command = new CassandraCommands().getCommand(cce);
            command.getRequest().setUuid(uuid);
            command.getRequest().setTaskUuid(startTask.getUuid());
            command.getRequest().setRequestSequenceNumber(startTask.getIncrementedReqSeqNumber());
            startTask.addCommand(command);
        }
        tasks.add(startTask);
        start();
    }

    public void runCommand(UUID agentUuid, CassandraCommandEnum cce) {
        Task startTask = RequestUtil.createTask(ServiceLocator.getService(CommandManagerInterface.class), "Start Cassandra");
        Command command = new CassandraCommands().getCommand(cce);
        command.getRequest().setUuid(agentUuid);
        command.getRequest().setTaskUuid(startTask.getUuid());
        command.getRequest().setRequestSequenceNumber(startTask.getIncrementedReqSeqNumber());
        startTask.addCommand(command);
        tasks.add(startTask);
        start();
    }

    public void start() {
//        terminal.setValue("");
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

    public void onResponse(Response response) {
        if (currentTask != null && response.getTaskUuid() != null
                && currentTask.getUuid().compareTo(response.getTaskUuid()) == 0) {
            List<ParseResult> list = ServiceLocator.getService(CommandManagerInterface.class).parseTask(response.getTaskUuid(), true);
            Task task = ServiceLocator.getService(CommandManagerInterface.class).getTask(response.getTaskUuid());
            if (!list.isEmpty() && terminal != null) {
                if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                    terminal.setValue(terminal.getValue().toString() + task.getDescription() + " successfully finished.\n");
                    moveToNextTask();
                    if (currentTask != null) {
                        terminal.setValue(terminal.getValue().toString() + "Running next step " + currentTask.getDescription() + "\n");
                        for (Command command : currentTask.getCommands()) {
                            executeCommand(command);
                        }
                    } else {
                        terminal.setValue(terminal.getValue().toString() + "Tasks complete.\n");
                    }
                } else if (task.getTaskStatus() == TaskStatus.FAIL) {
                    terminal.setValue(terminal.getValue().toString() + task.getDescription() + " failed\n");
                }
            }
            terminal.setCursorPosition(terminal.getValue().toString().length());
        }
    }

    public void executeCommand(Command command) {
//        terminal.setValue(terminal.getValue() + "\n" + command.getRequest().getProgram());
        ServiceLocator.getService(CommandManagerInterface.class).executeCommand(command);
    }

    public void updateSeeds(List<UUID> nodes, String seeds) {
        Task setSeedsTask = RequestUtil.createTask(ServiceLocator.getService(CommandManagerInterface.class), "Set seeds addresses");
        for (UUID agent : nodes) {
            Command setSeedsCommand = CassandraCommands.getSetSeedsCommand(seeds);
            setSeedsCommand.getRequest().setUuid(agent);
            setSeedsCommand.getRequest().setTaskUuid(setSeedsTask.getUuid());
            setSeedsCommand.getRequest().setRequestSequenceNumber(setSeedsTask.getIncrementedReqSeqNumber());
            setSeedsTask.addCommand(setSeedsCommand);
        }
        tasks.add(setSeedsTask);
        start();
    }

    public static AgentManagerInterface getAgentManager() {
        // get bundle instance via the OSGi Framework Util class
        BundleContext ctx = FrameworkUtil.getBundle(NodesWindow.class).getBundleContext();
        if (ctx != null) {
            ServiceReference serviceReference = ctx.getServiceReference(AgentManagerInterface.class.getName());
            if (serviceReference != null) {
                return AgentManagerInterface.class.cast(ctx.getService(serviceReference));
            }
        }

        return null;
    }

}
