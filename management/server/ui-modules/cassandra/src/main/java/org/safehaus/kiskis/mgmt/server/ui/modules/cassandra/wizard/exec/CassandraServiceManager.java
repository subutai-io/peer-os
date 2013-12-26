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
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.CassandraModule;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.commands.CassandraCommands;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.ParseResult;
import org.safehaus.kiskis.mgmt.shared.protocol.RequestUtil;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

/**
 *
 * @author bahadyr
 */
public class CassandraServiceManager {

    private final Queue<Task> tasks = new LinkedList<Task>();
    private Task currentTask;
    private final TextArea terminal;

    public CassandraServiceManager(TextArea textArea) {
        this.terminal = textArea;
    }

    public void startCassandraServices(List<UUID> list) {
        Task startTask = RequestUtil.createTask(CassandraModule.getCommandManager(), "Start Cassandra");
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
        Task stopTask = RequestUtil.createTask(CassandraModule.getCommandManager(), "Stop Cassandra");
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

            Task task = CassandraModule.getCommandManager().getTask(response.getTaskUuid());
            List<ParseResult> list = CassandraModule.getCommandManager().parseTask(task, true);
            task = CassandraModule.getCommandManager().getTask(response.getTaskUuid());
            if (!list.isEmpty()) {
                if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                    terminal.setValue(terminal.getValue().toString() + "\n" + task.getDescription() + " successfully finished.");
                    moveToNextTask();
                    if (currentTask != null) {
                        terminal.setValue(terminal.getValue().toString() + "\nRunning next step " + currentTask.getDescription());
                        for (Command command : currentTask.getCommands()) {
                            executeCommand(command);
                        }
                    } else {
                        terminal.setValue(terminal.getValue().toString() + "\nInstallation finished.");
                    }
                } else if (task.getTaskStatus() == TaskStatus.FAIL) {
                    terminal.setValue("\n" + task.getDescription() + " failed");
                }
            }

        }
    }

    private void executeCommand(Command command) {
        terminal.setValue(terminal.getValue() + "\n" + command.getRequest().getProgram());
        CassandraModule.getCommandManager().executeCommand(command);
    }

}
