/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizard.exec;

import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskCallback;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.CassandraModule;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.commands.CassandraCommands;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.management.CassandraCommandEnum;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.management.CassandraTable;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.management.NodesWindow;
import org.safehaus.kiskis.mgmt.shared.protocol.*;

import org.safehaus.kiskis.mgmt.api.taskrunner.TaskStatus;

/**
 *
 * @author bahadyr
 */
public class ServiceManager {

    private final Queue<Task> tasks = new LinkedList<Task>();
    private Task currentTask;
    private final CassandraTable cassandraTable;

    public ServiceManager(CassandraTable cassandraTable) {
        this.cassandraTable = cassandraTable;
    }

    public void runCommand(List<UUID> list, CassandraCommandEnum cce) {
        Task startTask = new Task("Run command");
        for (UUID uuid : list) {
            Request command = new CassandraCommands().getCommand(cce);
            command.setUuid(uuid);
            startTask.addRequest(command);
        }
        tasks.add(startTask);
        start();
    }

    public void runCommand(UUID agentUuid, CassandraCommandEnum cce) {
        Task startTask = new Task("Run command");
        Request command = new CassandraCommands().getCommand(cce);
        command.setUuid(agentUuid);
        startTask.addRequest(command);
        tasks.add(startTask);
        start();
    }

    public void start() {
        moveToNextTask();
        if (currentTask != null) {
            CassandraModule.getTaskRunner().executeTask(currentTask, new TaskCallback() {

                @Override
                public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                    if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                        if (cassandraTable.getNodesWindow() != null && cassandraTable.getNodesWindow().isVisible()) {
                            cassandraTable.getNodesWindow().updateUI(task);
                        }
                        cassandraTable.manageUI(task.getTaskStatus());
                    } else if (task.getTaskStatus() == TaskStatus.FAIL) {
                        if (cassandraTable.getNodesWindow() != null && cassandraTable.getNodesWindow().isVisible()) {
                            cassandraTable.getNodesWindow().updateUI(task);
                        }
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

    public void updateSeeds(List<UUID> nodes, String seeds) {
        Task setSeedsTask = new Task("Update seeds");
        for (UUID agent : nodes) {
            Request setSeedsCommand = CassandraCommands.getSetSeedsCommand(seeds);
            setSeedsCommand.setUuid(agent);
            setSeedsTask.addRequest(setSeedsCommand);
        }
        tasks.add(setSeedsTask);
        start();
    }

    public static AgentManager
            getAgentManager() {
        // get bundle instance via the OSGi Framework Util class
        BundleContext ctx = FrameworkUtil.getBundle(NodesWindow.class
        ).getBundleContext();
        if (ctx
                != null) {
            ServiceReference serviceReference = ctx.getServiceReference(AgentManager.class.getName());
            if (serviceReference != null) {
                return AgentManager.class.cast(ctx.getService(serviceReference));
            }
        }

        return null;
    }

}
