package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.util;

import com.google.common.base.Strings;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopModule;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.wizard.Step3;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: daralbaev
 * Date: 12/7/13
 * Time: 5:55 PM
 */
public class HadoopInstallation {
    private Task hadoopInstallationTask;
    private Task hadoopConfigureTask;
    private Task hadoopSNameNodeTask;
    private Task hadoopSlaveNameNode;
    private Task hadoopSlaveJobTracker;
    private Task hadoopSetSSH;
    private Task hadoopSSHMaster;
    private Task hadoopCopySSHSlaves;
    private Task hadoopConfigMasterSSH;
    private Task hadoopFormatMaster;

    private HadoopClusterInfo cluster;
    private String clusterName;
    private Agent nameNode, jobTracker, sNameNode;
    private List<Agent> dataNodes, taskTrackers;
    private Integer replicationFactor;
    private List<Agent> allNodes;
    private List<Agent> allSlaveNodes;
    private List<String> keys;

    private CommandManagerInterface commandManager;

    public HadoopInstallation(CommandManagerInterface commandManagerInterface) {
        this.commandManager = commandManagerInterface;
    }

    public void installHadoop() {
        setCluster();
        hadoopInstallationTask = RequestUtil.createTask(commandManager, "Setup Hadoop cluster");

        for (Agent agent : allNodes) {
            if (agent != null) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(":source", HadoopModule.MODULE_NAME);
                map.put(":uuid", agent.getUuid().toString());

                RequestUtil.createRequest(commandManager, HadoopCommands.INSTALL_HADOOP, hadoopInstallationTask, map);
            }
        }
    }

    public void configureHadoop() {
        hadoopConfigureTask = RequestUtil.createTask(commandManager, "Configure Hadoop cluster");

        for (Agent agent : allNodes) {
            if (agent != null) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(":source", HadoopModule.MODULE_NAME);
                map.put(":uuid", agent.getUuid().toString());

                map.put(":namenode", nameNode.getListIP().get(0));
                map.put(":jobtracker", jobTracker.getListIP().get(0));
                map.put(":replicationfactor", replicationFactor.toString());

                RequestUtil.createRequest(commandManager, HadoopCommands.CONFIGURE_SLAVES, hadoopConfigureTask, map);
            }
        }
    }

    public void configureSNameNode() {
        hadoopSNameNodeTask = RequestUtil.createTask(commandManager, "Configure Hadoop secondary name node");

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", nameNode.getUuid().toString());

        RequestUtil.createRequest(commandManager, HadoopCommands.CLEAR_SECONDARY_NAME_NODE, hadoopSNameNodeTask, map);

        map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", nameNode.getUuid().toString());

        map.put(":secondarynamenode", sNameNode.getListIP().get(0));

        RequestUtil.createRequest(commandManager, HadoopCommands.SET_SECONDARY_NAME_NODE, hadoopSNameNodeTask, map);
    }

    public void setSlaveNameNode() {
        hadoopSlaveNameNode = RequestUtil.createTask(commandManager, "Set Hadoop slave name nodes");

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", nameNode.getUuid().toString());

        RequestUtil.createRequest(commandManager, HadoopCommands.CLEAR_SLAVES_NAME_NODE, hadoopSlaveNameNode, map);

        for (Agent agent : allSlaveNodes) {
            if (agent != null) {
                map = new HashMap<String, String>();
                map.put(":source", HadoopModule.MODULE_NAME);
                map.put(":uuid", nameNode.getUuid().toString());

                map.put(":slave-hostname", agent.getListIP().get(0));

                RequestUtil.createRequest(commandManager, HadoopCommands.SET_SLAVES_NAME_NODE, hadoopSlaveNameNode, map);
            }
        }
    }

    public void setSlaveJobTracker() {
        hadoopSlaveJobTracker = RequestUtil.createTask(commandManager, "Set Hadoop slave job tracker");

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", jobTracker.getUuid().toString());

        RequestUtil.createRequest(commandManager, HadoopCommands.CLEAR_SLAVES_JOB_TRACKER, hadoopSlaveJobTracker, map);

        for (Agent agent : allSlaveNodes) {
            if (agent != null) {

                map = new HashMap<String, String>();
                map.put(":source", HadoopModule.MODULE_NAME);
                map.put(":uuid", jobTracker.getUuid().toString());

                map.put(":slave-hostname", agent.getListIP().get(0));

                RequestUtil.createRequest(commandManager, HadoopCommands.SET_SLAVES_JOB_TRACKER, hadoopSlaveJobTracker, map);
            }
        }
    }

    public void setSSH() {
        hadoopSetSSH = RequestUtil.createTask(commandManager, "Set Hadoop configure SSH");

        for (Agent agent : allNodes) {
            if (agent != null) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(":source", HadoopModule.MODULE_NAME);
                map.put(":uuid", agent.getUuid().toString());

                RequestUtil.createRequest(commandManager, HadoopCommands.SET_SSH_MASTERS, hadoopSetSSH, map);
            }
        }
    }

    public void setSSHMaster() {
        hadoopSSHMaster = RequestUtil.createTask(commandManager, "Set Hadoop SSH master");

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", nameNode.getUuid().toString());
        RequestUtil.createRequest(commandManager, HadoopCommands.COPY_SSH_MASTERS, hadoopSSHMaster, map);

        map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", sNameNode.getUuid().toString());
        RequestUtil.createRequest(commandManager, HadoopCommands.COPY_SSH_MASTERS, hadoopSSHMaster, map);

        map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", jobTracker.getUuid().toString());
        RequestUtil.createRequest(commandManager, HadoopCommands.COPY_SSH_MASTERS, hadoopSSHMaster, map);
    }

    public void copySSHSlaves() {
        hadoopCopySSHSlaves = RequestUtil.createTask(commandManager, "Copy Hadoop SSH key to slaves");

        if (keys != null && !keys.isEmpty()) {
            for (Agent agent : allNodes) {
                if (agent != null) {
                    for (String key : keys) {
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(":source", HadoopModule.MODULE_NAME);
                        map.put(":uuid", agent.getUuid().toString());

                        map.put(":PUB_KEY", key);

                        RequestUtil.createRequest(commandManager, HadoopCommands.COPY_SSH_SLAVES, hadoopCopySSHSlaves, map);
                    }
                }
            }
        }
    }

    public void configSSHMaster() {
        hadoopConfigMasterSSH = RequestUtil.createTask(commandManager, "Config SSH Masters");

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", nameNode.getUuid().toString());
        RequestUtil.createRequest(commandManager, HadoopCommands.CONFIG_SSH_MASTER, hadoopConfigMasterSSH, map);

        map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", sNameNode.getUuid().toString());
        RequestUtil.createRequest(commandManager, HadoopCommands.CONFIG_SSH_MASTER, hadoopConfigMasterSSH, map);

        map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", jobTracker.getUuid().toString());
        RequestUtil.createRequest(commandManager, HadoopCommands.CONFIG_SSH_MASTER, hadoopConfigMasterSSH, map);
    }

    public void formatMaster() {
        hadoopFormatMaster = RequestUtil.createTask(commandManager, "Format name node");

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", nameNode.getUuid().toString());
        RequestUtil.createRequest(commandManager, HadoopCommands.FORMAT_NAME_NODE, hadoopFormatMaster, map);

        System.out.println(cluster);
        commandManager.saveHadoopClusterData(cluster);
    }

    public void onCommand(Response response, Step3 panel) {
        Task task = commandManager.getTask(response.getTaskUuid());
        List<ParseResult> list = commandManager.parseTask(task, true);
        task = commandManager.getTask(response.getTaskUuid());
        if (!list.isEmpty() && panel != null) {
            if (task.getTaskStatus().compareTo(TaskStatus.SUCCESS) == 0) {
                panel.addOutput(task, " successfully finished.");
            } else if (task.getTaskStatus().compareTo(TaskStatus.FAIL) == 0) {
                panel.addOutput(task, " failed.\n" +
                        "Details: " + getResponseError(list));
            }

            if (hadoopSSHMaster != null && hadoopSSHMaster.equals(task)) {
                keys = new ArrayList<String>();
                for (ParseResult pr : list) {
                    keys.add(pr.getResponse().getStdOut().trim());
                }
            }
        }
    }

    private void removeDuplicateAgents() {
        Set<Agent> allAgents = new HashSet<Agent>();
        if (dataNodes != null) {
            allAgents.addAll(dataNodes);
        }
        if (taskTrackers != null) {
            allAgents.addAll(taskTrackers);
        }

        this.allSlaveNodes = new ArrayList<Agent>();
        this.allSlaveNodes.addAll(allAgents);

        if (nameNode != null) {
            allAgents.add(nameNode);
        }
        if (jobTracker != null) {
            allAgents.add(jobTracker);
        }
        if (sNameNode != null) {
            allAgents.add(sNameNode);
        }

        this.allNodes = new ArrayList<Agent>();
        this.allNodes.addAll(allAgents);
    }

    private String getResponseError(List<ParseResult> list) {
        StringBuilder stringBuilder = new StringBuilder();
        for (ParseResult pr : list) {
            if (!Strings.isNullOrEmpty(pr.getResponse().getStdErr())) {
                stringBuilder.append(pr.getRequest());
                stringBuilder.append("\n");
                stringBuilder.append(pr.getResponse());
                stringBuilder.append("\n");
            }
        }

        return stringBuilder.toString();
    }

    private void setCluster() {
        removeDuplicateAgents();
        cluster = new HadoopClusterInfo();

        cluster.setClusterName(clusterName);
        cluster.setReplicationFactor(replicationFactor);

        cluster.setNameNode(nameNode.getUuid());
        cluster.setSecondaryNameNode(sNameNode.getUuid());
        cluster.setJobTracker(jobTracker.getUuid());

        List<UUID> list = new ArrayList<UUID>();
        for (Agent agent : dataNodes) {
            list.add(agent.getUuid());
        }
        cluster.setDataNodes(list);

        list = new ArrayList<UUID>();
        for (Agent agent : taskTrackers) {
            list.add(agent.getUuid());
        }
        cluster.setTaskTrackers(list);
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Agent getNameNode() {
        return nameNode;
    }

    public void setNameNode(Agent nameNode) {
        this.nameNode = nameNode;
    }

    public Agent getJobTracker() {
        return jobTracker;
    }

    public void setJobTracker(Agent jobTracker) {
        this.jobTracker = jobTracker;
    }

    public Agent getsNameNode() {
        return sNameNode;
    }

    public void setsNameNode(Agent sNameNode) {
        this.sNameNode = sNameNode;
    }

    public List<Agent> getDataNodes() {
        return dataNodes;
    }

    public void setDataNodes(List<Agent> dataNodes) {
        this.dataNodes = dataNodes;
    }

    public List<Agent> getTaskTrackers() {
        return taskTrackers;
    }

    public void setTaskTrackers(List<Agent> taskTrackers) {
        this.taskTrackers = taskTrackers;
    }

    public void setReplicationFactor(Integer replicationFactor) {
        this.replicationFactor = replicationFactor;
    }
}
