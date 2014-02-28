package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.common;

import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.HadoopClusterInfo;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: daralbaev
 * Date: 2/3/14
 * Time: 3:07 PM
 */
public class HadoopConfig {
    private HadoopClusterInfo cluster;
    private String clusterName, domainName;
    private Agent nameNode, jobTracker, sNameNode;
    private List<Agent> dataNodes, taskTrackers;
    private Integer replicationFactor;
    private List<Agent> allNodes;
    private List<Agent> allSlaveNodes;
    private List<String> keys;

    public void removeDuplicateAgents() {
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

    public void setCluster() {
        removeDuplicateAgents();
        cluster = new HadoopClusterInfo();

        cluster.setClusterName(clusterName);
        cluster.setIpMask(domainName);
        cluster.setReplicationFactor(replicationFactor);

        cluster.setNameNode(nameNode);
        cluster.setSecondaryNameNode(sNameNode);
        cluster.setJobTracker(jobTracker);

        List<Agent> list = new ArrayList<Agent>();
        for (Agent agent : dataNodes) {
            list.add(agent);
        }
        cluster.setDataNodes(list);

        list = new ArrayList<Agent>();
        for (Agent agent : taskTrackers) {
            list.add(agent);
        }
        cluster.setTaskTrackers(list);
    }

    public HadoopClusterInfo getCluster() {
        return cluster;
    }

    public void setCluster(HadoopClusterInfo cluster) {
        this.cluster = cluster;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
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

    public List<Agent> getAllNodes() {
        return allNodes;
    }

    public void setAllNodes(List<Agent> allNodes) {
        this.allNodes = allNodes;
    }

    public List<Agent> getAllSlaveNodes() {
        return allSlaveNodes;
    }

    public void setAllSlaveNodes(List<Agent> allSlaveNodes) {
        this.allSlaveNodes = allSlaveNodes;
    }

    public List<String> getKeys() {
        return keys;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }
}
