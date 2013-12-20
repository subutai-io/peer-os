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

        hadoopInstallationTask = createTask("Setup Hadoop cluster");
        createInstallationRequest();
    }

    public void configureHadoop() {
        hadoopConfigureTask = createTask("Configure Hadoop cluster");
        createConfigureRequest();
    }

    public void configureSNameNode() {
        hadoopSNameNodeTask = createTask("Configure Hadoop secondary name node");
        createSNameNodeRequest();
    }

    public void setSlaveNameNode() {
        hadoopSlaveNameNode = createTask("Set Hadoop slave name nodes");
        createSetSlaveNameNodeRequest();
    }

    public void setSlaveJobTracker() {
        hadoopSlaveJobTracker = createTask("Set Hadoop slave job tracker");
        createSetSlaveJobTrackerRequest();
    }

    public void setSSH() {
        hadoopSetSSH = createTask("Set Hadoop configure SSH");
        createSSHRequest();
    }

    public void setSSHMaster() {
        hadoopSSHMaster = createTask("Set Hadoop SSH master");
        createSSHMasterRequest();
    }

    public void copySSHSlaves() {
        hadoopCopySSHSlaves = createTask("Copy Hadoop SSH key to slaves");
        createCopySSHRequest();
    }

    public void configSSHMaster() {
        hadoopConfigMasterSSH = createTask("Config SSH Masters");
        createConfigSSHMasterRequest();
    }

    public void formatMaster() {
        hadoopFormatMaster = createTask("Format name node");
        createFormatMasterRequest();
    }

    private Task createTask(String description) {
        Task clusterTask = new Task();
        clusterTask.setTaskStatus(TaskStatus.NEW);
        clusterTask.setDescription(description);
        commandManager.saveTask(clusterTask);

        return clusterTask;
    }


    private void createInstallationRequest() {
        for (Agent agent : allNodes) {
            if (agent != null) {
                createRequest(HadoopCommands.INSTALL_HADOOP, hadoopInstallationTask, agent, null, null);
            }
        }
    }

    private void createConfigureRequest() {
        for (Agent agent : allNodes) {
            if (agent != null) {
                createRequest(HadoopCommands.CONFIGURE_SLAVES, hadoopConfigureTask, agent, null, null);
            }
        }
    }

    private void createSNameNodeRequest() {
        createRequest(HadoopCommands.CLEAR_SECONDARY_NAME_NODE, hadoopSNameNodeTask, nameNode, null, null);
        createRequest(HadoopCommands.SET_SECONDARY_NAME_NODE, hadoopSNameNodeTask, nameNode, null, null);
    }

    private void createSetSlaveNameNodeRequest() {
        createRequest(HadoopCommands.CLEAR_SLAVES_NAME_NODE, hadoopSlaveNameNode, nameNode, null, null);
        for (Agent agent : allSlaveNodes) {
            if (agent != null) {
                createRequest(HadoopCommands.SET_SLAVES_NAME_NODE, hadoopSlaveNameNode, nameNode, agent, null);
            }
        }
    }

    private void createSetSlaveJobTrackerRequest() {
        createRequest(HadoopCommands.CLEAR_SLAVES_JOB_TRACKER, hadoopSlaveJobTracker, jobTracker, null, null);
        for (Agent agent : allSlaveNodes) {
            if (agent != null) {
                createRequest(HadoopCommands.SET_SLAVES_JOB_TRACKER, hadoopSlaveJobTracker, jobTracker, agent, null);
            }
        }
    }

    private void createSSHRequest() {
        for (Agent agent : allNodes) {
            if (agent != null) {
                createRequest(HadoopCommands.SET_SSH_MASTERS, hadoopSetSSH, agent, null, null);
            }
        }
    }

    private void createCopySSHRequest() {
        if (keys != null && !keys.isEmpty()) {
            for (Agent agent : allSlaveNodes) {
                if (agent != null) {
                    for (String key : keys) {
                        createRequest(HadoopCommands.COPY_SSH_SLAVES, hadoopCopySSHSlaves, agent, null, key);
                    }
                }
            }
        }
    }

    private void createConfigSSHMasterRequest() {
        createRequest(HadoopCommands.CONFIG_SSH_MASTER, hadoopConfigMasterSSH, nameNode, null, null);
        createRequest(HadoopCommands.CONFIG_SSH_MASTER, hadoopConfigMasterSSH, sNameNode, null, null);
        createRequest(HadoopCommands.CONFIG_SSH_MASTER, hadoopConfigMasterSSH, jobTracker, null, null);
    }

    private void createFormatMasterRequest() {
        createRequest(HadoopCommands.FORMAT_NAME_NODE, hadoopFormatMaster, nameNode, null, null);

        System.out.println(cluster);
        commandManager.saveHadoopClusterData(cluster);
    }

    private void createSSHMasterRequest() {
        createRequest(HadoopCommands.COPY_SSH_MASTERS, hadoopSSHMaster, nameNode, null, null);
        createRequest(HadoopCommands.COPY_SSH_MASTERS, hadoopSSHMaster, sNameNode, null, null);
        createRequest(HadoopCommands.COPY_SSH_MASTERS, hadoopSSHMaster, jobTracker, null, null);
    }

    private Request createRequest(final String command, Task task, Agent agent, Agent slave, String key) {
        String json = command;
        json = json.replaceAll(":taskUuid", task.getUuid().toString());
        json = json.replaceAll(":source", HadoopModule.MODULE_NAME);

        json = json.replaceAll(":uuid", agent.getUuid().toString());
        json = json.replaceAll(":requestSequenceNumber", task.getIncrementedReqSeqNumber().toString());

        json = json.replaceAll(":namenode", nameNode.getHostname());
        json = json.replaceAll(":jobtracker", jobTracker.getHostname());
        json = json.replaceAll(":replicationfactor", replicationFactor.toString());

        if (key != null) {
            json = json.replaceAll(":PUB_KEY", key);
        }

        if (slave != null) {
            json = json.replaceAll(":slave-hostname", slave.getHostname());
        }


        Request request = CommandJson.getRequest(json);
        if (commandManager != null) {
            commandManager.executeCommand(new Command(request));
        }

        return request;
    }

    public void onCommand(Response response, Step3 panel) {
        Task task = commandManager.getTask(response.getTaskUuid());
        List<ParseResult> list = commandManager.parseTask(task, true);
        task = commandManager.getTask(response.getTaskUuid());
        if (!list.isEmpty()) {
            if (task.getTaskStatus().compareTo(TaskStatus.SUCCESS) == 0) {
                panel.addOutput(task, " successfully finished.");
            } else if (task.getTaskStatus().compareTo(TaskStatus.FAIL) == 0) {
                panel.addOutput(task, " failed.\n" +
                        "Details: " + getResponseError(list));
            }

            if (hadoopSSHMaster != null && hadoopSSHMaster.equals(task)) {
                keys = new ArrayList<String>();
                for (ParseResult pr : list) {
                    keys.add(pr.getResponse().getStdOut());
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
