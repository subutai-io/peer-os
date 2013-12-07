package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.util;

import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopModule;
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
    private Task hadoopTask;

    private String clusterName;
    private Agent nameNode, jobTracker, sNameNode;
    private List<Agent> dataNodes, taskTrackers;
    private Integer replicationFactor;
    private List<Agent> allNodes;
    private List<Agent> allSlaveNodes;
    private CommandManagerInterface commandManager;

    public HadoopInstallation(CommandManagerInterface commandManagerInterface){
        this.commandManager = commandManagerInterface;
    }

    public void createTask() {
        Task clusterTask = new Task();
        clusterTask.setTaskStatus(TaskStatus.NEW);
        clusterTask.setDescription("Setup Hadoop cluster");

        setHadoopTask(clusterTask);
        commandManager.saveTask(hadoopTask);

        removeDuplicateAgents();
    }

    private void removeDuplicateAgents() {
        Set<Agent> allAgents = new HashSet<Agent>();
        allAgents.addAll(dataNodes);
        allAgents.addAll(taskTrackers);

        this.allSlaveNodes = new ArrayList<Agent>();
        this.allSlaveNodes.addAll(allAgents);

        allAgents.add(nameNode);
        allAgents.add(jobTracker);
        allAgents.add(sNameNode);

        this.allNodes = new ArrayList<Agent>();
        this.allNodes.addAll(allAgents);
    }

    public void createInstallationRequest() {
        for(Agent agent : allNodes){
            if(agent != null){
                createRequest(HadoopCommands.INSTALL_HADOOP, hadoopTask, agent);
            }
        }
    }

    private void createRequest(final String command, Task task, Agent agent) {
        String json = command;
        json = json.replaceAll(":task", task.getUuid().toString());
        json = json.replaceAll(":source", HadoopModule.MODULE_NAME);

        json = json.replaceAll(":uuid", agent.getUuid().toString());
        json = json.replaceAll(":requestSequenceNumber", task.getIncrementedReqSeqNumber().toString());

        /*Request request = CommandJson.getRequest(json);
        if (commandManager != null) {
            commandManager.executeCommand(new Command(request));
        }*/

        System.out.println(json);
    }

    public Task getHadoopTask() {
        return hadoopTask;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public void setHadoopTask(Task hadoopTask) {
        this.hadoopTask = hadoopTask;
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
