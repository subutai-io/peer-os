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
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.commands.CassandraCommands;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizard.CassandraConfig;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizard.CassandraWizard;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.ParseResult;
import org.safehaus.kiskis.mgmt.shared.protocol.RequestUtil;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ResponseListener;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

/**
 *
 * @author bahadyr
 */
public class Installer implements ResponseListener {

    private final CassandraConfig config;
    private final Queue<Task> tasks = new LinkedList<Task>();
    private final TextArea terminal;
    private Task currentTask;

    public Installer(CassandraConfig config, TextArea terminal) {
        this.config = config;
        this.terminal = terminal;

        Task installTask = RequestUtil.createTask(CassandraWizard.getCommandManager(), "Install Cassandra");
        for (Agent agent : config.getSelectedAgents()) {
            Command command = CassandraCommands.getInstallCommand();
            command.getRequest().setUuid(agent.getUuid());
            command.getRequest().setSource(CassandraWizard.SOURCE);
            command.getRequest().setUuid(agent.getUuid());
            command.getRequest().setTaskUuid(installTask.getUuid());
            command.getRequest().setRequestSequenceNumber(installTask.getIncrementedReqSeqNumber());
            installTask.addCommand(command);
        }

        Task setListenAddressTask = RequestUtil.createTask(CassandraWizard.getCommandManager(), "Set listen");
        for (Agent agent : config.getSelectedAgents()) {
            Command command = CassandraCommands.getSetListenAddressCommand();
            command.getRequest().setUuid(agent.getUuid());
            command.getRequest().setSource(CassandraWizard.SOURCE);
            command.getRequest().setUuid(agent.getUuid());
            command.getRequest().setTaskUuid(setListenAddressTask.getUuid());
            command.getRequest().setRequestSequenceNumber(setListenAddressTask.getIncrementedReqSeqNumber());
            setListenAddressTask.addCommand(command);
        }

        tasks.add(installTask);
        tasks.add(setListenAddressTask);
    }

    public void start() {
        //launch first task
        currentTask = tasks.poll();
        if (currentTask != null) {
            //execute
            for (Command command : currentTask.getCommands()) {
                CassandraWizard.getCommandManager().executeCommand(command);
            }
        }
    }

    private void moveToNextTask() {
        currentTask = tasks.poll();
    }

    @Override
    public void onResponse(Response response) {
        System.out.println("RESPONSE==============" + response);
        if (currentTask != null && response != null && response.getTaskUuid() != null
                && currentTask.getUuid().compareTo(response.getTaskUuid()) == 0) {

            Task task = CassandraWizard.getCommandManager().getTask(response.getTaskUuid());
            List<ParseResult> list = CassandraWizard.getCommandManager().parseTask(task, true);
            task = CassandraWizard.getCommandManager().getTask(response.getTaskUuid());
            if (!list.isEmpty() && terminal != null) {
                if (task.getTaskStatus().compareTo(TaskStatus.SUCCESS) == 0) {
                    terminal.setValue(terminal.getValue().toString() + "\nStep successfully finished.");
                    moveToNextTask();
                    if (currentTask != null) {
                        for (Command command : currentTask.getCommands()) {
                            CassandraWizard.getCommandManager().executeCommand(command);
                        }
                        terminal.setValue(terminal.getValue().toString() + "\nRunning next step...");
                    } else {
                        terminal.setValue(terminal.getValue().toString() + "\nInstallation finished");
                    }
                } else if (task.getTaskStatus().compareTo(TaskStatus.FAIL) == 0) {
                    terminal.setValue("\n" + task + " failed");
                }
            }

        }
    }

}
