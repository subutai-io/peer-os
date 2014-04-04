package org.safehaus.kiskis.mgmt.impl.networkmanager;

import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskCallback;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskRunner;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskStatus;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

import java.util.List;

/**
 * Created by daralbaev on 04.04.14.
 */
public class HostManager {
    private TaskRunner taskRunner;
    private List<Agent> agentList;
    private String domainName;

    public HostManager(TaskRunner taskRunner, List<Agent> agentList, String domainName) {
        this.taskRunner = taskRunner;
        this.agentList = agentList;
        this.domainName = domainName;
    }

    public boolean execute() {
        if (agentList != null && !agentList.isEmpty()) {
            return write();
        }

        return false;
    }

    private boolean write() {
        Task task = Tasks.getWriteHostTask(agentList, prepareHost());

        taskRunner.executeTask(task, new TaskCallback() {

            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                if (task.isCompleted()) {
                    synchronized (task) {
                        task.notifyAll();
                    }
                }

                return null;
            }
        });

        synchronized (task) {
            try {
                task.wait(task.getAvgTimeout() * 1000 + 1000);
            } catch (InterruptedException ex) {
                return false;
            }
        }

        return task.getTaskStatus() == TaskStatus.SUCCESS;
    }

    private String prepareHost() {
        StringBuilder value = new StringBuilder();

        for (Agent agent : agentList) {
            value.append(agent.getListIP().get(0));
            value.append("\t");
            value.append(agent.getHostname());
            value.append(".");
            value.append(domainName);
            value.append("\t");
            value.append(agent.getHostname());
            value.append("\n");
        }
        value.append("127.0.0.1\tlocalhost");

        return value.toString();
    }
}
