/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.hbase.wizard.exec;

import org.safehaus.kiskis.mgmt.api.taskrunner.TaskCallback;
import org.safehaus.kiskis.mgmt.server.ui.modules.hbase.HBaseModule;
import org.safehaus.kiskis.mgmt.server.ui.modules.hbase.commands.HBaseCommands;
import org.safehaus.kiskis.mgmt.server.ui.modules.hbase.management.HBaseCommandEnum;
import org.safehaus.kiskis.mgmt.server.ui.modules.hbase.management.HBaseTable;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 *
 * @author bahadyr
 */
public class ServiceManager {

    private final Queue<Task> tasks = new LinkedList<Task>();
    private Task currentTask;
    private final HBaseTable hBaseTable;

    public ServiceManager(HBaseTable hBaseTable) {
        this.hBaseTable = hBaseTable;
    }

    public void runCommand(Set<Agent> agents, HBaseCommandEnum cce) {
        Task startTask = new Task("Run command");
        for (Agent agent : agents) {
            Request command = new HBaseCommands().getCommand(cce);
            command.setUuid(agent.getUuid());

            startTask.addRequest(command);
        }
        tasks.add(startTask);
        start();
    }

    public void runCommand(Agent agent, HBaseCommandEnum cce) {
        Task startTask = new Task("Run command");
        Request command = new HBaseCommands().getCommand(cce);
        command.setUuid(agent.getUuid());

        startTask.addRequest(command);
        tasks.add(startTask);
        start();
    }

    public void start() {
        moveToNextTask();
        if (currentTask != null) {
//            for (Request command : currentTask.getCommands()) {
//                executeCommand(command);
//            }

            HBaseModule.getTaskRunner().executeTask(currentTask, new TaskCallback() {

                @Override
                public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                    if (task.isCompleted()) {
                        if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                            moveToNextTask();
                            if (currentTask != null) {
                                return currentTask;
                            }
                        } else if (task.getTaskStatus() == TaskStatus.FAIL) {
                        }
                        hBaseTable.manageUI(task);
                    }
                    return null;
                }
            });
        }
    }

    public void moveToNextTask() {
        currentTask = tasks.poll();
    }

    public Task getCurrentTask() {
        return currentTask;
    }

}
