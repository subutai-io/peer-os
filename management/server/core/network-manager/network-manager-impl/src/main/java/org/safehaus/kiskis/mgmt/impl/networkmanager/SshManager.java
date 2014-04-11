package org.safehaus.kiskis.mgmt.impl.networkmanager;

import com.google.common.base.Strings;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskCallback;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskRunner;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskStatus;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

import java.util.List;

/**
 * Created by daralbaev on 04.04.14.
 */
public class SshManager {
    private TaskRunner taskRunner;
    private List<Agent> agentList;
    private String keys;

    public SshManager(TaskRunner taskRunner, List<Agent> agentList) {
        this.taskRunner = taskRunner;
        this.agentList = agentList;
    }

    public boolean execute() {
        if (agentList != null && !agentList.isEmpty()) {
            if (create()) {
                if (read()) {
                    if (write()) {
                        return config();
                    }
                }
            }
        }

        return false;
    }

    private boolean read() {
        Task task = Tasks.getReadSshTask(agentList);

        taskRunner.executeTask(task, new TaskCallback() {
            final StringBuilder value = new StringBuilder();

            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                if (!Util.isStringEmpty(response.getStdOut())) {
                    value.append(response.getStdOut());
                }

                if (task.isCompleted()) {
                    synchronized (task) {
                        keys = value.toString();
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

        if (!Strings.isNullOrEmpty(keys)) {
            return task.getTaskStatus() == TaskStatus.SUCCESS;
        } else {
            return false;
        }
    }

    private boolean write() {
        Task task = Tasks.getWriteSshTask(agentList, keys);

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

    private boolean create() {
        Task task = Tasks.getCreateSshTask(agentList);

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

    private boolean config() {
        Task task = Tasks.getConfigSshTask(agentList);

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
}
