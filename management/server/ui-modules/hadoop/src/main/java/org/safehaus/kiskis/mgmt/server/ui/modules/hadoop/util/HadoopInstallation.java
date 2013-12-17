package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.util;

import com.google.common.base.Strings;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopModule;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.wizard.Step3;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: daralbaev
 * Date: 12/7/13
 * Time: 5:55 PM
 */
public class HadoopInstallation {
    private Task hadoopInstallationTask;
    private Task hadoopConfigureTask;

    private String clusterName;
    private Agent nameNode, jobTracker, sNameNode;
    private List<Agent> dataNodes, taskTrackers;
    private Integer replicationFactor;
    private List<Agent> allNodes;
    private List<Agent> allSlaveNodes;
    private CommandManagerInterface commandManager;

    public HadoopInstallation(CommandManagerInterface commandManagerInterface) {
        this.commandManager = commandManagerInterface;
    }

    public void installHadoop() {
        removeDuplicateAgents();

        hadoopInstallationTask = createTask("Setup Hadoop cluster");
        createInstallationRequest();
    }

    public void configureHadoop() {
        System.out.println("Hadoop configuration");
        hadoopConfigureTask = createTask("Configure Hadoop cluster");
        createConfigureRequest();
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
                createRequest(HadoopCommands.INSTALL_HADOOP, hadoopInstallationTask, agent);
            }
        }
    }

    private void createConfigureRequest() {
        for (Agent agent : allNodes) {
            if (agent != null) {
                createRequest(HadoopCommands.CONFIGURE_SLAVES, hadoopConfigureTask, agent);
            }
        }
    }

    private Request createRequest(final String command, Task task, Agent agent) {
        String json = command;
        json = json.replaceAll(":taskUuid", task.getUuid().toString());
        json = json.replaceAll(":source", HadoopModule.MODULE_NAME);

        json = json.replaceAll(":uuid", agent.getUuid().toString());
        json = json.replaceAll(":requestSequenceNumber", task.getIncrementedReqSeqNumber().toString());

        json = json.replaceAll(":namenode", nameNode.getHostname());
        json = json.replaceAll(":jobtracker", jobTracker.getHostname());
        json = json.replaceAll(":replicationfactor", replicationFactor.toString());

        Request request = CommandJson.getRequest(json);
        if (commandManager != null) {
            commandManager.executeCommand(new Command(request));
        }

        return request;
    }

    public void onCommand(Response response, Step3 panel) {
        if (response.getTaskUuid().compareTo(hadoopInstallationTask.getUuid()) == 0) {
            List<ParseResult> resultList = commandManager.parseTask(hadoopInstallationTask, true);
            hadoopInstallationTask = commandManager.getTask(hadoopInstallationTask.getUuid());

            if (resultList.size() > 0 && hadoopInstallationTask.getTaskStatus().compareTo(TaskStatus.SUCCESS) == 0) {
                panel.addOutput(hadoopInstallationTask, " successfully finished.");
                if (hadoopConfigureTask == null) {
                    configureHadoop();
                }
            } else if (hadoopInstallationTask.getTaskStatus().compareTo(TaskStatus.FAIL) == 0) {
                StringBuilder stringBuilder = new StringBuilder();
                for (ParseResult pr : resultList) {
                    if (!Strings.isNullOrEmpty(pr.getResponse().getStdErr())) {
                        stringBuilder.append("\n");
                        stringBuilder.append(pr.getResponse().getStdErr());
                    }
                }
                panel.addOutput(hadoopInstallationTask, " failed.\nDetails: " + stringBuilder);
            }
        } else if (response.getTaskUuid().compareTo(hadoopConfigureTask.getUuid()) == 0) {
            List<ParseResult> resultList = commandManager.parseTask(hadoopConfigureTask, true);
            hadoopConfigureTask = commandManager.getTask(hadoopConfigureTask.getUuid());

            if (resultList.size() > 0 && hadoopConfigureTask.getTaskStatus().compareTo(TaskStatus.SUCCESS) == 0) {
                panel.addOutput(hadoopConfigureTask, " successfully finished.");
            } else if (hadoopConfigureTask.getTaskStatus().compareTo(TaskStatus.FAIL) == 0) {
                StringBuilder stringBuilder = new StringBuilder();
                for (ParseResult pr : resultList) {
                    if (!Strings.isNullOrEmpty(pr.getResponse().getStdErr())) {
                        stringBuilder.append("\n");
                        stringBuilder.append(pr.getResponse().getStdErr());
                    }
                }
                panel.addOutput(hadoopConfigureTask, " failed.\nDetails: " + stringBuilder);
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

    public Task getHadoopInstallationTask() {
        return hadoopInstallationTask;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public void setHadoopInstallationTask(Task hadoopInstallationTask) {
        this.hadoopInstallationTask = hadoopInstallationTask;
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

    public Integer getReplicationFactor() {
        return replicationFactor;
    }

    public void setReplicationFactor(Integer replicationFactor) {
        this.replicationFactor = replicationFactor;
    }
}
