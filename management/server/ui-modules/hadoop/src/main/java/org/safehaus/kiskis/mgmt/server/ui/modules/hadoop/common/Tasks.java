package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.common;

import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopModule;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.install.Commands;
import org.safehaus.kiskis.mgmt.shared.protocol.*;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: daralbaev
 * Date: 1/31/14
 * Time: 8:24 PM
 */
public class Tasks {
    public static Task getInstallTask(List<Agent> agents) {
        Task task = new Task("Setup hadoop deb packages");

        for (Agent agent : agents) {
            if (agent != null) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(":source", HadoopModule.MODULE_NAME);
                map.put(":uuid", agent.getUuid().toString());

                Request request = RequestUtil.getRequest(Commands.INSTALL_DEB, task, map);
                task.addCommand(new CommandImpl(request));
            }
        }

        return task;
    }

    public static Task getSetMastersTask(List<Agent> agents, Agent nameNode, Agent jobTracker, Integer replicationFactor){
        Task task = new Task("Configure hadoop master nodes");

        for (Agent agent : agents) {
            if (agent != null) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(":source", HadoopModule.MODULE_NAME);
                map.put(":uuid", agent.getUuid().toString());

                map.put(":namenode", nameNode.getHostname());
                map.put(":jobtracker", jobTracker.getHostname());
                map.put(":replicationfactor", replicationFactor.toString());

                Request request = RequestUtil.getRequest(Commands.SET_MASTERS, task, map);
                task.addCommand(new CommandImpl(request));
            }
        }

        return task;
    }

    public static Task getClearSecondaryNameNodeTask(Agent nameNode) {
        Task task = new Task("Clear SecondaryNameNode on NameNode");

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", nameNode.getUuid().toString());

        Request request = RequestUtil.getRequest(Commands.CLEAR_MASTER, task, map);
        task.addCommand(new CommandImpl(request));

        return task;
    }

    public static Task getSetSecondaryNameNodeTask(Agent nameNode, Agent secondaryNameNode) {
        Task task = new Task("Set SecondaryNameNode on NameNode");

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", nameNode.getUuid().toString());

        map.put(":secondarynamenode", secondaryNameNode.getUuid().toString());

        Request request = RequestUtil.getRequest(Commands.SET_SECONDARY_NAME_NODE, task, map);
        task.addCommand(new CommandImpl(request));

        return task;
    }

    public static Task getClearDataNodesTask(Agent nameNode) {
        Task task = new Task("Clear DataNodes on NameNode");

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", nameNode.getUuid().toString());

        Request request = RequestUtil.getRequest(Commands.CLEAR_SLAVES, task, map);
        task.addCommand(new CommandImpl(request));

        return task;
    }

    public static Task getSetDataNodesTask(Agent nameNode, Agent agent) {
        Task task = new Task("Set DataNodes on NameNode");

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", nameNode.getUuid().toString());

        map.put(":slave-hostname", agent.getHostname());

        Request request = RequestUtil.getRequest(Commands.SET_SLAVES, task, map);
        task.addCommand(new CommandImpl(request));

        return task;
    }

    public static Task getClearTaskTrackersTask(Agent jobTracker) {
        Task task = new Task("Clear TaskTrackers on JobTracker");

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", jobTracker.getUuid().toString());

        Request request = RequestUtil.getRequest(Commands.CLEAR_SLAVES, task, map);
        task.addCommand(new CommandImpl(request));

        return task;
    }

    public static Task getSetTaskTrackersTask(Agent jobTracker, Agent agent) {
        Task task = new Task("Set TaskTrackers on JobTracker");

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", jobTracker.getUuid().toString());

        map.put(":slave-hostname", agent.getHostname());

        Request request = RequestUtil.getRequest(Commands.SET_SLAVES, task, map);
        task.addCommand(new CommandImpl(request));

        return task;
    }

    public static Task getSetSSHTask(List<Agent> agents) {
        Task task = new Task("Setup ssh on all nodes");

        for (Agent agent : agents) {
            if (agent != null) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(":source", HadoopModule.MODULE_NAME);
                map.put(":uuid", agent.getUuid().toString());

                Request request = RequestUtil.getRequest(Commands.SET_MASTER_KEY, task, map);
                task.addCommand(new CommandImpl(request));
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

                Request request = RequestUtil.getRequest(Commands.COPY_MASTER_KEY, task, map);
                task.addCommand(new CommandImpl(request));
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

                        Request request = RequestUtil.getRequest(Commands.PASTE_MASTER_KEY, task, map);
                        task.addCommand(new CommandImpl(request));
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

                Request request = RequestUtil.getRequest(Commands.SET_MASTER_CONFIG, task, map);
                task.addCommand(new CommandImpl(request));
            }
        }

        return task;
    }

    public static Task getFormatMasterTask(Agent nameNode) {
        Task task = new Task("Format NameNode");

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", nameNode.getUuid().toString());

        Request request = RequestUtil.getRequest(Commands.FORMAT_NAME_NODE, task, map);
        task.addCommand(new CommandImpl(request));

        return task;
    }

    public static Task getCopyHostsTask(List<Agent> agents) {
        Task task = new Task("Observing environment");

        for (Agent agent : agents) {
            if (agent != null) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(":source", HadoopModule.MODULE_NAME);
                map.put(":uuid", agent.getUuid().toString());

                Request request = RequestUtil.getRequest(Commands.READ_HOSTNAME, task, map);
                task.addCommand(new CommandImpl(request));
            }
        }

        return task;
    }

    public static Task getPasteHostsTask(HashMap<UUID, String> hosts){
        Task task = new Task("Configuring environment");

        for(UUID id : hosts.keySet()){
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(":source", HadoopModule.MODULE_NAME);
            map.put(":uuid", id.toString());
            map.put(":hosts", hosts.get(id));

            Request request = RequestUtil.getRequest(Commands.WRITE_HOSTNAME, task, map);
            task.addCommand(new CommandImpl(request));
        }

        return task;
    }
}
