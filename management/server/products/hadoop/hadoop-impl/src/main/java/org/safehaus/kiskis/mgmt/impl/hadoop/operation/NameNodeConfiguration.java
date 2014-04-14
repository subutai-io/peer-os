package org.safehaus.kiskis.mgmt.impl.hadoop.operation;

import org.safehaus.kiskis.mgmt.api.hadoop.Config;
import org.safehaus.kiskis.mgmt.api.taskrunner.Result;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskCallback;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskStatus;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.hadoop.HadoopImpl;
import org.safehaus.kiskis.mgmt.impl.hadoop.Tasks;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.NodeState;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Created by daralbaev on 12.04.14.
 */
public class NameNodeConfiguration {
    private HadoopImpl parent;
    private Config config;
    private String globalStatus;

    public NameNodeConfiguration(HadoopImpl parent, Config config) {
        this.parent = parent;
        this.config = config;
    }

    public UUID startNameNode() {
        final ProductOperation po
                = parent.getTracker().createProductOperation(Config.PRODUCT_KEY,
                String.format("Starting cluster's %s NameNode", config.getClusterName()));

        parent.getExecutor().execute(new Runnable() {

            public void run() {
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", config.getClusterName()));
                    return;
                }

                Task task = Tasks.getNameNodeCommandTask(config.getNameNode(), "start");
                parent.getTaskRunner().executeTask(task);

                if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                    po.addLog(String.format("Task's operation %s finished", task.getDescription()));
                } else if (task.getTaskStatus() == TaskStatus.FAIL) {
                    po.addLogFailed(String.format("Task's operation %s failed", task.getDescription()));
                } else if (task.getTaskStatus() == TaskStatus.TIMEDOUT) {
                    po.addLogFailed(String.format("Task's operation %s timeout", task.getDescription()));
                }
            }
        });

        return po.getId();
    }

    public UUID stopNameNode() {

        final ProductOperation po
                = parent.getTracker().createProductOperation(Config.PRODUCT_KEY,
                String.format("Stopping cluster's %s NameNode", config.getClusterName()));

        parent.getExecutor().execute(new Runnable() {

            public void run() {
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", config.getClusterName()));
                    return;
                }

                Task task = Tasks.getNameNodeCommandTask(config.getNameNode(), "stop");
                parent.getTaskRunner().executeTask(task);

                if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                    po.addLog(String.format("Task's operation %s finished", task.getDescription()));
                } else if (task.getTaskStatus() == TaskStatus.FAIL) {
                    po.addLogFailed(String.format("Task's operation %s failed", task.getDescription()));
                } else if (task.getTaskStatus() == TaskStatus.TIMEDOUT) {
                    po.addLogFailed(String.format("Task's operation %s timeout", task.getDescription()));
                }
            }
        });

        return po.getId();
    }

    public UUID restartNameNode() {
        final ProductOperation po
                = parent.getTracker().createProductOperation(Config.PRODUCT_KEY,
                String.format("Restarting cluster's %s NameNode", config.getClusterName()));

        parent.getExecutor().execute(new Runnable() {

            public void run() {
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", config.getClusterName()));
                    return;
                }

                Task task = Tasks.getNameNodeCommandTask(config.getNameNode(), "restart");
                parent.getTaskRunner().executeTask(task);

                if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                    po.addLog(String.format("Task's operation %s finished", task.getDescription()));
                } else if (task.getTaskStatus() == TaskStatus.FAIL) {
                    po.addLogFailed(String.format("Task's operation %s failed", task.getDescription()));
                } else if (task.getTaskStatus() == TaskStatus.TIMEDOUT) {
                    po.addLogFailed(String.format("Task's operation %s timeout", task.getDescription()));
                }
            }
        });

        return po.getId();
    }

    public UUID statusNameNode() {

        final ProductOperation po
                = parent.getTracker().createProductOperation(Config.PRODUCT_KEY,
                String.format("Restarting cluster's %s NameNode", config.getClusterName()));

        parent.getExecutor().execute(new Runnable() {

            public void run() {
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", config.getClusterName()));
                    return;
                }
                Task task = Tasks.getNameNodeCommandTask(config.getNameNode(), "status");

                parent.getTaskRunner().executeTask(task);

                NodeState nodeState = NodeState.UNKNOWN;
                if (task.isCompleted()) {
                    Result result = task.getResults().entrySet().iterator().next().getValue();
                    if (result.getStdOut().contains("NameNode")) {
                        String[] array = result.getStdOut().split("\n");
                        System.out.println(result.getStdOut());

                        for (String status : array) {
                            if (status.contains("NameNode")) {
                                String temp = status.
                                        replaceAll(Pattern.quote("!(SecondaryNameNode is not running on this machine)"), "").
                                        replaceAll("NameNode is ", "");
                                if (temp.toLowerCase().contains("not")) {
                                    nodeState = NodeState.STOPPED;
                                } else {
                                    nodeState = NodeState.RUNNING;
                                }
                            }
                        }
                    }
                }

                if (NodeState.UNKNOWN.equals(nodeState)) {
                    po.addLogFailed(String.format("Failed to check status of %s, %s",
                            config.getClusterName(),
                            config.getNameNode().getHostname()
                    ));
                } else {
                    po.addLogDone(String.format("NameNode of %s is %s",
                            config.getNameNode().getHostname(),
                            nodeState
                    ));
                }
            }
        });

        return po.getId();

    }

    public boolean statusSecondaryNameNode() {
        Task task = Tasks.getNameNodeCommandTask(config.getSecondaryNameNode(), "status");
        final String[] gStatus = new String[1];

        parent.getTaskRunner().executeTaskNWait(task, new TaskCallback() {
            @Override
            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                    String[] array = response.getStdOut().split("\n");

                    for (String status : array) {
                        if (status.contains("SecondaryNameNode")) {
                            gStatus[0] = status.
                                    replaceAll("SecondaryNameNode is ", "");
                        }
                    }
                }

                return null;
            }
        });

        return !gStatus[0].toLowerCase().contains("not");
    }

    public boolean statusDataNode(Agent agent) {
        Task task = Tasks.getNameNodeCommandTask(agent, "status");
        final String[] gStatus = new String[1];

        parent.getTaskRunner().executeTaskNWait(task, new TaskCallback() {
            @Override
            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                    String[] array = response.getStdOut().split("\n");

                    for (String status : array) {
                        if (status.contains("DataNode")) {
                            gStatus[0] = status.
                                    replaceAll("DataNode is ", "");
                        }
                    }
                }

                return null;
            }
        });

        return !gStatus[0].toLowerCase().contains("not");
    }

    public boolean startJobTracker() {
        Task task = Tasks.getJobTrackerCommand(config.getJobTracker(), "start");
        parent.getTaskRunner().executeTask(task);

        return task.getTaskStatus() == TaskStatus.SUCCESS;
    }

    public boolean stopJobTracker() {
        Task task = Tasks.getJobTrackerCommand(config.getJobTracker(), "stop");
        parent.getTaskRunner().executeTask(task);

        return task.getTaskStatus() == TaskStatus.SUCCESS;
    }

    public boolean restartJobTracker() {
        Task task = Tasks.getJobTrackerCommand(config.getJobTracker(), "restart");
        parent.getTaskRunner().executeTask(task);

        return task.getTaskStatus() == TaskStatus.SUCCESS;
    }

    public boolean statusJobTracker() {
        Task task = Tasks.getJobTrackerCommand(config.getJobTracker(), "status");
        final String[] gStatus = new String[1];

        parent.getTaskRunner().executeTaskNWait(task, new TaskCallback() {
            @Override
            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                    String[] array = response.getStdOut().split("\n");

                    for (String status : array) {
                        if (status.contains("JobTracker")) {
                            gStatus[0] = status.
                                    replaceAll("JobTracker is ", "");
                        }
                    }
                }

                return null;
            }
        });

        return !gStatus[0].toLowerCase().contains("not");
    }

    public boolean statusTaskTracker(Agent agent) {
        Task task = Tasks.getJobTrackerCommand(agent, "status");
        final String[] gStatus = new String[1];

        parent.getTaskRunner().executeTaskNWait(task, new TaskCallback() {
            @Override
            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                    String[] array = response.getStdOut().split("\n");

                    for (String status : array) {
                        if (status.contains("TaskTracker")) {
                            gStatus[0] = status.
                                    replaceAll("TaskTracker is ", "");
                        }
                    }
                }

                return null;
            }
        });

        return !gStatus[0].toLowerCase().contains("not");
    }
}
