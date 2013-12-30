/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizard.exec;

import com.vaadin.ui.TextArea;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.commands.CassandraCommands;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

/**
 *
 * @author bahadyr
 */
public class ServiceManager {

    private final Queue<Task> tasks = new LinkedList<Task>();
    private Task currentTask;
    private final TextArea terminal;

    public ServiceManager(TextArea textArea) {
        this.terminal = textArea;
    }

    public void startCassandraServices(List<UUID> list) {
        Task startTask = RequestUtil.createTask(ServiceLocator.getService(CommandManagerInterface.class), "Start Cassandra");
        for (UUID uuid : list) {
            Command command = CassandraCommands.getServiceCassandraStartCommand();
            command.getRequest().setUuid(uuid);
            command.getRequest().setTaskUuid(startTask.getUuid());
            command.getRequest().setRequestSequenceNumber(startTask.getIncrementedReqSeqNumber());
            startTask.addCommand(command);
        }
        tasks.add(startTask);
        start();
    }

    public void stopCassandraServices(List<UUID> list) {
        Task stopTask = RequestUtil.createTask(ServiceLocator.getService(CommandManagerInterface.class), "Stop Cassandra");
        for (UUID uuid : list) {
            Command command = CassandraCommands.getServiceCassandraStopCommand();
            command.getRequest().setUuid(uuid);
            command.getRequest().setTaskUuid(stopTask.getUuid());
            command.getRequest().setRequestSequenceNumber(stopTask.getIncrementedReqSeqNumber());
            stopTask.addCommand(command);
        }
        tasks.add(stopTask);
        start();
    }

    public void statusCassandraServices(List<UUID> list) {
        Task statusTask = RequestUtil.createTask(ServiceLocator.getService(CommandManagerInterface.class), "Cassandra service status check");
        for (UUID uuid : list) {
            Command command = CassandraCommands.getServiceCassandraStatusCommand();
            command.getRequest().setUuid(uuid);
            command.getRequest().setTaskUuid(statusTask.getUuid());
            command.getRequest().setRequestSequenceNumber(statusTask.getIncrementedReqSeqNumber());
            statusTask.addCommand(command);
        }
        tasks.add(statusTask);
        start();
    }

    public void purgeCassandraServices(List<UUID> list) {
        Task purgeTask = RequestUtil.createTask(ServiceLocator.getService(CommandManagerInterface.class), "Purge Cassandra");
        for (UUID uuid : list) {
            Command command = CassandraCommands.getUninstallCommand();
            command.getRequest().setUuid(uuid);
            command.getRequest().setTaskUuid(purgeTask.getUuid());
            command.getRequest().setRequestSequenceNumber(purgeTask.getIncrementedReqSeqNumber());
            purgeTask.addCommand(command);
        }
        tasks.add(purgeTask);
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

    private void moveToNextTask() {
        currentTask = tasks.poll();
    }

    public void onResponse(Response response) {
        if (currentTask != null && response.getTaskUuid() != null
                && currentTask.getUuid().compareTo(response.getTaskUuid()) == 0) {
            List<ParseResult> list = ServiceLocator.getService(CommandManagerInterface.class).parseTask(response.getTaskUuid(), true);
            Task task = ServiceLocator.getService(CommandManagerInterface.class).getTask(response.getTaskUuid());
            if (!list.isEmpty()) {
                if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                    for (ParseResult pr : ServiceLocator.getService(CommandManagerInterface.class).parseTask(task.getUuid(), true)) {
                        terminal.setValue(terminal.getValue().toString() + pr.getResponse().getStdOut());
                    }
                    terminal.setValue(terminal.getValue().toString() + "\n" + task.getDescription() + " successfully finished.");
                    moveToNextTask();
                    if (currentTask != null) {
                        terminal.setValue(terminal.getValue().toString() + "\nRunning next step " + currentTask.getDescription());
                        for (Command command : currentTask.getCommands()) {
                            executeCommand(command);
                        }
                    } else {
                        terminal.setValue(terminal.getValue().toString() + "\nTasks complete.\n");
                    }
                } else if (task.getTaskStatus() == TaskStatus.FAIL) {
                    terminal.setValue("\n" + task.getDescription() + " failed");
                }
            }

            terminal.setCursorPosition(terminal.getValue().toString().length() - 1);
        }
    }

    private void executeCommand(Command command) {
//        terminal.setValue(terminal.getValue() + "\n" + command.getRequest().getProgram());
        ServiceLocator.getService(CommandManagerInterface.class).executeCommand(command);
    }

    public void startCassandraService(Agent agent) {
        Task startTask = RequestUtil.createTask(ServiceLocator.getService(CommandManagerInterface.class), "Start Cassandra Service");
        Command command = CassandraCommands.getServiceCassandraStartCommand();
        command.getRequest().setUuid(agent.getUuid());
        command.getRequest().setTaskUuid(startTask.getUuid());
        command.getRequest().setRequestSequenceNumber(startTask.getIncrementedReqSeqNumber());
        startTask.addCommand(command);
        tasks.add(startTask);
        start();
    }

    public void stopCassandraService(Agent agent) {
        Task startTask = RequestUtil.createTask(ServiceLocator.getService(CommandManagerInterface.class), "Stop Cassandra Service");
        Command command = CassandraCommands.getServiceCassandraStopCommand();
        command.getRequest().setUuid(agent.getUuid());
        command.getRequest().setTaskUuid(startTask.getUuid());
        command.getRequest().setRequestSequenceNumber(startTask.getIncrementedReqSeqNumber());
        startTask.addCommand(command);
        tasks.add(startTask);
        start();
    }

    public void statusCassandraService(UUID uuid) {
        Task startTask = RequestUtil.createTask(ServiceLocator.getService(CommandManagerInterface.class), "Status of Cassandra Service");
        Command command = CassandraCommands.getServiceCassandraStatusCommand();
        command.getRequest().setUuid(uuid);
        command.getRequest().setTaskUuid(startTask.getUuid());
        command.getRequest().setRequestSequenceNumber(startTask.getIncrementedReqSeqNumber());
        startTask.addCommand(command);
        tasks.add(startTask);
        start();
    }

    public void uninstallCassandraService(Agent agent) {
        Task startTask = RequestUtil.createTask(ServiceLocator.getService(CommandManagerInterface.class), "Unistall Cassandra Service");
        Command command = CassandraCommands.getUninstallCommand();
        command.getRequest().setUuid(agent.getUuid());
        command.getRequest().setTaskUuid(startTask.getUuid());
        command.getRequest().setRequestSequenceNumber(startTask.getIncrementedReqSeqNumber());
        startTask.addCommand(command);
        tasks.add(startTask);
        start();
    }

}
