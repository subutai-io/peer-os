package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.common;

import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopClusterInfo;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopModule;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 1/31/14 Time: 8:24 PM
 */
public class Tasks {

    public static Task removeClusterTask(HadoopClusterInfo cluster) {
        Task task = new Task("Remove hadoop deb packages");

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", cluster.getNameNode().getUuid().toString());
        Request request = TaskUtil.createRequest(Commands.PURGE_DEB, task, map);
        task.addRequest(request);

        map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", cluster.getSecondaryNameNode().getUuid().toString());
        request = TaskUtil.createRequest(Commands.PURGE_DEB, task, map);
        task.addRequest(request);

        map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", cluster.getJobTracker().getUuid().toString());
        request = TaskUtil.createRequest(Commands.PURGE_DEB, task, map);
        task.addRequest(request);

        for (Agent agent : cluster.getDataNodes()) {
            if (agent != null) {
                map = new HashMap<String, String>();
                map.put(":source", HadoopModule.MODULE_NAME);
                map.put(":uuid", agent.getUuid().toString());

                request = TaskUtil.createRequest(Commands.PURGE_DEB, task, map);
                task.addRequest(request);
            }
        }

        for (Agent agent : cluster.getTaskTrackers()) {
            if (agent != null) {
                map = new HashMap<String, String>();
                map.put(":source", HadoopModule.MODULE_NAME);
                map.put(":uuid", agent.getUuid().toString());

                request = TaskUtil.createRequest(Commands.PURGE_DEB, task, map);
                task.addRequest(request);
            }
        }

        return task;
    }

    public static Task getInstallTask(List<Agent> agents) {
        Task task = new Task("Setup hadoop deb packages");

        for (Agent agent : agents) {
            if (agent != null) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(":source", HadoopModule.MODULE_NAME);
                map.put(":uuid", agent.getUuid().toString());

                Request request = TaskUtil.createRequest(Commands.INSTALL_DEB, task, map);
                task.addRequest(request);
            }
        }

        return task;
    }

    public static Task getUninstallTask(Agent agent) {
        Task task = new Task("Purge hadoop deb packages");

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", agent.getUuid().toString());

        Request request = TaskUtil.createRequest(Commands.PURGE_DEB, task, map);
        task.addRequest(request);


        return task;
    }

    public static Task getSetMastersTask(List<Agent> agents, Agent nameNode, Agent jobTracker, Integer replicationFactor) {
        Task task = new Task("Configure hadoop master nodes");

        for (Agent agent : agents) {
            if (agent != null) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(":source", HadoopModule.MODULE_NAME);
                map.put(":uuid", agent.getUuid().toString());

                map.put(":namenode", nameNode.getHostname());
                map.put(":jobtracker", jobTracker.getHostname());
                map.put(":replicationfactor", replicationFactor.toString());

                Request request = TaskUtil.createRequest(Commands.SET_MASTERS, task, map);
                task.addRequest(request);
            }
        }

        return task;
    }

    public static Task getClearSecondaryNameNodeTask(Agent nameNode) {
        Task task = new Task("Clear SecondaryNameNode on NameNode");

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", nameNode.getUuid().toString());

        Request request = TaskUtil.createRequest(Commands.CLEAR_MASTER, task, map);
        task.addRequest(request);

        return task;
    }

    public static Task getSetSecondaryNameNodeTask(Agent nameNode, Agent secondaryNameNode) {
        Task task = new Task("Set SecondaryNameNode on NameNode");

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", nameNode.getUuid().toString());

        map.put(":secondarynamenode", secondaryNameNode.getHostname());

        Request request = TaskUtil.createRequest(Commands.SET_SECONDARY_NAME_NODE, task, map);
        task.addRequest(request);

        return task;
    }

    public static Task getClearDataNodesTask(Agent nameNode) {
        Task task = new Task("Clear DataNodes on NameNode");

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", nameNode.getUuid().toString());

        Request request = TaskUtil.createRequest(Commands.CLEAR_SLAVES, task, map);
        task.addRequest(request);

        return task;
    }

    public static Task getSetDataNodesTask(Agent nameNode, Agent agent) {
        Task task = new Task("Set DataNodes on NameNode");

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", nameNode.getUuid().toString());

        map.put(":slave-hostname", agent.getHostname());

        Request request = TaskUtil.createRequest(Commands.SET_SLAVES, task, map);
        task.addRequest(request);

        return task;
    }

    public static Task getClearTaskTrackersTask(Agent jobTracker) {
        Task task = new Task("Clear TaskTrackers on JobTracker");

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", jobTracker.getUuid().toString());

        Request request = TaskUtil.createRequest(Commands.CLEAR_SLAVES, task, map);
        task.addRequest(request);

        return task;
    }

    public static Task getSetTaskTrackersTask(Agent jobTracker, Agent agent) {
        Task task = new Task("Set TaskTrackers on JobTracker");

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", jobTracker.getUuid().toString());

        map.put(":slave-hostname", agent.getHostname());

        Request request = TaskUtil.createRequest(Commands.SET_SLAVES, task, map);
        task.addRequest(request);

        return task;
    }

    public static Task getSetSSHTask(List<Agent> agents) {
        Task task = new Task("Setup ssh on all nodes");

        for (Agent agent : agents) {
            if (agent != null) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(":source", HadoopModule.MODULE_NAME);
                map.put(":uuid", agent.getUuid().toString());

                Request request = TaskUtil.createRequest(Commands.SET_MASTER_KEY, task, map);
                task.addRequest(request);
            }
        }

        return task;
    }

    public static Task getCopySSHKeyTask(List<Agent> agents) {
        Task task = new Task("Copy ssh keys");

        for (Agent agent : agents) {
            if (agent != null) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(":source", HadoopModule.MODULE_NAME);
                map.put(":uuid", agent.getUuid().toString());

                Request request = TaskUtil.createRequest(Commands.COPY_MASTER_KEY, task, map);
                task.addRequest(request);
            }
        }

        return task;
    }

    public static Task getPasteSSHKeyTask(List<Agent> agents, List<String> keys) {
        Task task = new Task("Paste ssh keys");

        if (keys != null && !keys.isEmpty()) {
            for (Agent agent : agents) {
                if (agent != null) {
                    for (String key : keys) {
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(":source", HadoopModule.MODULE_NAME);
                        map.put(":uuid", agent.getUuid().toString());

                        map.put(":PUB_KEY", key);

                        Request request = TaskUtil.createRequest(Commands.PASTE_MASTER_KEY, task, map);
                        task.addRequest(request);
                    }
                }
            }
        }

        return task;
    }

    public static Task getConfigSSHFolderTask(List<Agent> agents) {
        Task task = new Task("Config ssh keys");

        for (Agent agent : agents) {
            if (agent != null) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(":source", HadoopModule.MODULE_NAME);
                map.put(":uuid", agent.getUuid().toString());

                Request request = TaskUtil.createRequest(Commands.SET_MASTER_CONFIG, task, map);
                task.addRequest(request);
            }
        }

        return task;
    }

    public static Task getFormatMasterTask(Agent nameNode) {
        Task task = new Task("Format NameNode");

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", nameNode.getUuid().toString());

        Request request = TaskUtil.createRequest(Commands.FORMAT_NAME_NODE, task, map);
        task.addRequest(request);

        return task;
    }

    public static Task getCopyHostsTask(List<Agent> agents) {
        Task task = new Task("Observing environment");

        for (Agent agent : agents) {
            if (agent != null) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(":source", HadoopModule.MODULE_NAME);
                map.put(":uuid", agent.getUuid().toString());

                Request request = TaskUtil.createRequest(Commands.READ_HOSTNAME, task, map);
                task.addRequest(request);
            }
        }

        return task;
    }

    public static Task getPasteHostsTask(HashMap<UUID, String> hosts) {
        Task task = new Task("Configuring environment");

        for (UUID id : hosts.keySet()) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(":source", HadoopModule.MODULE_NAME);
            map.put(":uuid", id.toString());
            map.put(":hosts", hosts.get(id));

            Request request = TaskUtil.createRequest(Commands.WRITE_HOSTNAME, task, map);
            task.addRequest(request);
        }

        return task;
    }

    public static Task getJobTrackerCommand(HadoopClusterInfo cluster, String command) {
        Task task = new Task(command + "for Hadoop Job Tracker");

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", cluster.getJobTracker().getUuid().toString());
        map.put(":command", command);

        Request request = TaskUtil.createRequest(Commands.COMMAND_JOB_TRACKER, task, map);
        task.addRequest(request);

        return task;
    }

    public static Task getNameNodeCommand(HadoopClusterInfo cluster, String command) {
        Task task = new Task(command + "for Hadoop Name Node");

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", cluster.getNameNode().getUuid().toString());
        map.put(":command", command);

        Request request = TaskUtil.createRequest(Commands.COMMAND_NAME_NODE, task, map);
        task.addRequest(request);

        return task;
    }

    public static Task getRemoveNodeCommand(HadoopClusterInfo cluster, Agent agent) {
        Task task = new Task("Remove node from cluster");

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", agent.getUuid().toString());
        map.put(":command", "stop");

        Request request = TaskUtil.createRequest(Commands.COMMAND_NAME_NODE, task, map);
        task.addRequest(request);

        map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", cluster.getNameNode().getUuid().toString());
        map.put(":slave-hostname", agent.getHostname());

        request = TaskUtil.createRequest(Commands.REMOVE_NODE_TRACKER, task, map);
        task.addRequest(request);

        return task;
    }

    public static Task getRemoveTrackerCommand(HadoopClusterInfo cluster, Agent agent) {
        Task task = new Task("Remove tracker from cluster");

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", agent.getUuid().toString());
        map.put(":command", "stop");

        Request request = TaskUtil.createRequest(Commands.COMMAND_JOB_TRACKER, task, map);
        task.addRequest(request);

        map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", cluster.getJobTracker().getUuid().toString());
        map.put(":slave-hostname", agent.getHostname());

        request = TaskUtil.createRequest(Commands.REMOVE_NODE_TRACKER, task, map);
        task.addRequest(request);

        return task;
    }
}
