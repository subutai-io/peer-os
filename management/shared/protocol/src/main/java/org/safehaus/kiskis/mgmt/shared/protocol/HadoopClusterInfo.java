/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.shared.protocol;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * @author dilshat
 */
public class HadoopClusterInfo implements Serializable {

    public static final String UUID_LABEL = "UUID",
            CLUSTER_NAME_LABEL = "Cluster name",
            NAME_NODE_LABEL = "Name Node",
            SECONDARY_NAME_NODE_LABEL = "Secondary Name Node",
            JOB_TRACKER_LABEL = "Job Tracker",
            REPLICATION_FACTOR_LABEL = "Replication Factor",
            DATA_NODES_LABEL = "Data Nodes",
            TASK_TRACKERS_LABEL = "Task Trackers",
            IP_MASK_LABEL = "IP Mask";

    private UUID uuid;
    private String clusterName;
    private Agent nameNode;
    private Agent secondaryNameNode;
    private Agent jobTracker;
    private int replicationFactor;
    private List<Agent> dataNodes;
    private List<Agent> taskTrackers;
    private String ipMask;

    public HadoopClusterInfo() {
        this.uuid = java.util.UUID.fromString(new com.eaio.uuid.UUID().toString());
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uid) {
        this.uuid = uid;
    }

    public String getClusterName() {
        return clusterName;
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

    public Agent getSecondaryNameNode() {
        return secondaryNameNode;
    }

    public void setSecondaryNameNode(Agent secondaryNameNode) {
        this.secondaryNameNode = secondaryNameNode;
    }

    public Agent getJobTracker() {
        return jobTracker;
    }

    public void setJobTracker(Agent jobTracker) {
        this.jobTracker = jobTracker;
    }

    public int getReplicationFactor() {
        return replicationFactor;
    }

    public void setReplicationFactor(int replicationFactor) {
        this.replicationFactor = replicationFactor;
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

    public String getIpMask() {
        return ipMask;
    }

    public void setIpMask(String ipMask) {
        this.ipMask = ipMask;
    }

    @Override
    public String toString() {
        return "HadoopClusterInfo{" +
                "uuid=" + uuid +
                ", clusterName=" + clusterName +
                ", nameNode=" + nameNode +
                ", secondaryNameNode=" + secondaryNameNode +
                ", jobTracker=" + jobTracker +
                ", replicationFactor=" + replicationFactor +
                ", dataNodes=" + dataNodes +
                ", taskTrackers=" + taskTrackers +
                ", ipMask=" + ipMask + '}';
    }
}
