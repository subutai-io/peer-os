/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizard.exec;

import com.vaadin.ui.TextArea;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.commands.CassandraCommands;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.ParseResult;
import org.safehaus.kiskis.mgmt.shared.protocol.RequestUtil;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.ServiceLocator;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

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
            Task task = ServiceLocator.getService(CommandManagerInterface.class).getTask(response.getTaskUuid());
            List<ParseResult> list = ServiceLocator.getService(CommandManagerInterface.class).parseTask(task, true);
            task = ServiceLocator.getService(CommandManagerInterface.class).getTask(response.getTaskUuid());
            if (!list.isEmpty()) {
                if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                    for (ParseResult pr : ServiceLocator.getService(CommandManagerInterface.class).parseTask(task, true)) {
                        terminal.setValue(terminal.getValue().toString() + "\n" + pr.getResponse().getStdOut());
                    }
                    terminal.setValue(terminal.getValue().toString() + "\n" + task.getDescription() + " successfully finished.");
                    moveToNextTask();
                    if (currentTask != null) {
                        terminal.setValue(terminal.getValue().toString() + "\nRunning next step " + currentTask.getDescription());
                        for (Command command : currentTask.getCommands()) {
                            executeCommand(command);
                        }
                    } else {
                        terminal.setValue(terminal.getValue().toString() + "\nTasks complete.");
                    }
                } else if (task.getTaskStatus() == TaskStatus.FAIL) {
                    terminal.setValue("\n" + task.getDescription() + " failed");
                }
            }

        }
    }

    private void executeCommand(Command command) {
        terminal.setValue(terminal.getValue() + "\n" + command.getRequest().getProgram());
        ServiceLocator.getService(CommandManagerInterface.class).executeCommand(command);
    }

}
