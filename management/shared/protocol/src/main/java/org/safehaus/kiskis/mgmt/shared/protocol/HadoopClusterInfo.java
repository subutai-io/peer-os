/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.shared.protocol;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author dilshat
 */
public class HadoopClusterInfo {

    public static final String UUID_LABEL = "UUID",
            CLUSTER_NAME_LABEL = "Cluster name",
            NAME_NODE_LABEL = "Name Node",
            SECONDARY_NAME_NODE_LABEL = "Secondary Name Node",
            JOB_TRACKER_LABEL = "Job Tracker",
            REPLICATION_FACTOR_LABEL = "Replication Factor",
            DATA_NODES_LABEL = "Data Nodes",
            TASK_TRACKERS_LABEL = "Task Trackers",
            IP_MASK_LABEL = "IP Mask";
    private UUID uid;
    private String clusterName;
    private UUID nameNode;
    private UUID secondaryNameNode;
    private UUID jobTracker;
    private int replicationFactor;
    private List<UUID> dataNodes;
    private List<UUID> taskTrackers;
    private String ipMask;

    public HadoopClusterInfo() {
        this.uid = java.util.UUID.fromString(new com.eaio.uuid.UUID().toString());
    }

    public UUID getUid() {
        return uid;
    }

    public void setUid(UUID uid) {
        this.uid = uid;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public UUID getNameNode() {
        return nameNode;
    }

    public void setNameNode(UUID nameNode) {
        this.nameNode = nameNode;
    }

    public UUID getSecondaryNameNode() {
        return secondaryNameNode;
    }

    public void setSecondaryNameNode(UUID secondaryNameNode) {
        this.secondaryNameNode = secondaryNameNode;
    }

    public UUID getJobTracker() {
        return jobTracker;
    }

    public void setJobTracker(UUID jobTracker) {
        this.jobTracker = jobTracker;
    }

    public int getReplicationFactor() {
        return replicationFactor;
    }

    public void setReplicationFactor(int replicationFactor) {
        this.replicationFactor = replicationFactor;
    }

    public List<UUID> getDataNodes() {
        return dataNodes;
    }

    public void setDataNodes(List<UUID> dataNodes) {
        this.dataNodes = dataNodes;
    }

    public List<UUID> getTaskTrackers() {
        return taskTrackers;
    }

    public void setTaskTrackers(List<UUID> taskTrackers) {
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
        return "HadoopClusterInfo{" + "uid=" + uid + ", clusterName=" + clusterName + ", nameNode=" + nameNode + ", secondaryNameNode=" + secondaryNameNode + ", jobTracker=" + jobTracker + ", replicationFactor=" + replicationFactor + ", dataNodes=" + dataNodes + ", taskTrackers=" + taskTrackers + ", ipMask=" + ipMask + '}';
    }
}
