/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop;

import org.doomdark.uuid.UUIDGenerator;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import java.io.Serializable;
import java.util.*;

/**
 * @author dilshat
 */
public class HadoopClusterInfo implements Serializable {

    public static final String SOURCE = "Hadoop",
            UUID_LABEL = "UUID",
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
        this.uuid = java.util.UUID.fromString(UUIDGenerator.getInstance().generateTimeBasedUUID().toString());
    }

    public List<Agent> getAllAgents() {
        Set<Agent> allAgents = new HashSet<Agent>();
        if (dataNodes != null) {
            allAgents.addAll(dataNodes);
        }
        if (taskTrackers != null) {
            allAgents.addAll(taskTrackers);
        }

        if (nameNode != null) {
            allAgents.add(nameNode);
        }
        if (jobTracker != null) {
            allAgents.add(jobTracker);
        }
        if (secondaryNameNode != null) {
            allAgents.add(secondaryNameNode);
        }

        return new ArrayList<Agent>(allAgents);
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
        return "HadoopClusterInfo{"
                + "uuid=" + uuid
                + ", clusterName=" + clusterName
                + ", nameNode=" + nameNode
                + ", secondaryNameNode=" + secondaryNameNode
                + ", jobTracker=" + jobTracker
                + ", replicationFactor=" + replicationFactor
                + ", dataNodes=" + dataNodes
                + ", taskTrackers=" + taskTrackers
                + ", ipMask=" + ipMask + '}';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final HadoopClusterInfo other = (HadoopClusterInfo) obj;
        if (this.uuid != other.uuid && (this.uuid == null || !this.uuid.equals(other.uuid))) {
            return false;
        }
        return true;
    }

}
