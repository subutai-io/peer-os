/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.hbase;

import org.doomdark.uuid.UUIDGenerator;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.ConfigBase;

import java.util.Set;
import java.util.UUID;

/**
 * @author dilshat
 */
public class Config implements ConfigBase {

    public static final String PRODUCT_KEY = "HBase";
    private int numberOfNodes = 4;
    private UUID uuid;
    private Agent master;
    private Set<Agent> region;
    private Set<Agent> quorum;
    private Agent backupMasters;
    private String domainInfo;
    private Set<Agent> nodes;
    private String clusterName = "";
    private Agent hadoopNameNode;

    public Config() {
        this.uuid = UUID.fromString(UUIDGenerator.getInstance().generateTimeBasedUUID().toString());
    }

    public static String getProductKey() {
        return PRODUCT_KEY;
    }

    public Agent getHadoopNameNode() {
        return hadoopNameNode;
    }

    public void setHadoopNameNode(Agent hadoopNameNode) {
        this.hadoopNameNode = hadoopNameNode;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void reset() {
        this.master = null;
        this.region = null;
        this.quorum = null;
        this.backupMasters = null;
        this.domainInfo = "";
        this.clusterName = "";
    }

    public Agent getMaster() {
        return master;
    }

    public void setMaster(Agent master) {
        this.master = master;
    }

    public Set<Agent> getRegion() {
        return region;
    }

    public void setRegion(Set<Agent> region) {
        this.region = region;
    }

    public Set<Agent> getQuorum() {
        return quorum;
    }

    public void setQuorum(Set<Agent> quorum) {
        this.quorum = quorum;
    }

    public Agent getBackupMasters() {
        return backupMasters;
    }

    public void setBackupMasters(Agent backupMasters) {
        this.backupMasters = backupMasters;
    }

    public String getDomainInfo() {
        return domainInfo;
    }

    public void setDomainInfo(String domainInfo) {
        this.domainInfo = domainInfo;
    }

    public Set<Agent> getNodes() {
        return nodes;
    }

    public void setNodes(Set<Agent> nodes) {
        this.nodes = nodes;
    }

    public int getNumberOfNodes() {
        return numberOfNodes;
    }

    public void setNumberOfNodes(int numberOfNodes) {
        this.numberOfNodes = numberOfNodes;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    @Override
    public String getProductName() {
        return PRODUCT_KEY;
    }
}
