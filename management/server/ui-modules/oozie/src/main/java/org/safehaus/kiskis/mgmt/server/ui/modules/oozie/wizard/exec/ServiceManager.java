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

import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskCallback;
import org.safehaus.kiskis.mgmt.server.ui.modules.oozie.OozieConfig;
import org.safehaus.kiskis.mgmt.server.ui.modules.oozie.OozieDAO;
import org.safehaus.kiskis.mgmt.server.ui.modules.oozie.OozieModule;
import org.safehaus.kiskis.mgmt.server.ui.modules.oozie.commands.OozieCommands;
import org.safehaus.kiskis.mgmt.server.ui.modules.oozie.management.OozieCommandEnum;
import org.safehaus.kiskis.mgmt.server.ui.modules.oozie.management.OozieTable;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskStatus;

/**
 *
 * @author bahadyr
 */
public class ServiceManager {

    private final Queue<Task> tasks = new LinkedList<Task>();
    private Task currentTask;
    private final OozieTable oozieTable;

    public ServiceManager(OozieTable oozieTable) {
        this.oozieTable = oozieTable;
    }

    public void runCommand(Set<Agent> agents, OozieCommandEnum cce) {
        Task startTask = new Task("Run command");
        for (Agent agent : agents) {
            Request command = new OozieCommands().getCommand(cce);
            command.setTimeout(cce.getTimeout());
            command.setUuid(agent.getUuid());
            startTask.addRequest(command);
        }
        tasks.add(startTask);
        start();
    }

    public void runCommand(Agent agent, OozieCommandEnum cce) {
        Task startTask = new Task("Run command");
        Request command = new OozieCommands().getCommand(cce);
        command.setTimeout(cce.get);
        command.setUuid(agent.getUuid());
        startTask.addRequest(command);
        tasks.add(startTask);
        start();
    }

    public void start() {
        moveToNextTask();
        if (currentTask != null) {
            OozieModule.getTaskRunner().executeTask(currentTask, new TaskCallback() {
                @Override
                public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                    if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                        moveToNextTask();
                        if (currentTask != null) {
                            return currentTask;
                        } else {
                            oozieTable.manageUI(task, stdOut, stdErr);
                        }
                    } else {
                        oozieTable.manageUI(task, stdOut, stdErr);
                    }
                    return null;
                }
            });
        }
    }

    public void startPurge() {
        moveToNextTask();
        if (currentTask != null) {
            OozieModule.getTaskRunner().executeTask(currentTask, new TaskCallback() {
                @Override
                public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                    if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                        moveToNextTask();
                        if (currentTask != null) {
                            return currentTask;
                        } else {
                            oozieTable.manageUI(task, stdOut, stdErr);

                        }
                    }
                    return null;
                }
            });

        }
        deleteInfo();
    }

    public void moveToNextTask() {
        currentTask = tasks.poll();
    }

    public Task getCurrentTask() {
        return currentTask;
    }
    OozieConfig config;

    public void purge(OozieConfig config) {
        this.config = config;
        OozieCommands oc = new OozieCommands();

        Task purgeServer = new Task("Purge Oozie Server");
        Request commandServerPurge = oc.getCommand(OozieCommandEnum.PURGE_SERVER);
        commandServerPurge.setUuid(config.getServer().getUuid());
        purgeServer.addRequest(commandServerPurge);
        tasks.add(purgeServer);

        Task purgeClient = new Task("Purge Client command");
        for (Agent agent : config.getClients()) {
            Request command = oc.getCommand(OozieCommandEnum.PURGE_CLIENT);
            command.setUuid(agent.getUuid());
            purgeClient.addRequest(command);
        }
        tasks.add(purgeClient);
        startPurge();
    }

    private void deleteInfo() {
        OozieDAO.deleteClusterInfo(config.getUuid());
    }

}
