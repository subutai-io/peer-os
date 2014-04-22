/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.cassandra;

import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import java.util.Set;

/**
 * @author dilshat
 */
public class Config {

    public static final String PRODUCT_KEY = "Cassandra";
    private String clusterName = "";
    private String domainName = "";
    private Agent listedAddressNode;
    private Agent rpcAddressNode;
    private int numberOfSeeds;
    private Set<Agent> seedNodes;
    private String dataDirectory = "/var/lib/cassandra/data";
    private String commitLogDirectory = "/var/lib/cassandra/commitlog";
    private String savedCachesDirectory = "/var/lib/cassandra/saved_caches";

    public static String getProductKey() {
        return PRODUCT_KEY;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Agent getListedAddressNode() {
        return listedAddressNode;
    }

    public void setListedAddressNode(Agent listedAddressNode) {
        this.listedAddressNode = listedAddressNode;
    }

    public Agent getRpcAddressNode() {
        return rpcAddressNode;
    }

    public void setRpcAddressNode(Agent rpcAddressNode) {
        this.rpcAddressNode = rpcAddressNode;
    }

    public String getDataDirectory() {
        return dataDirectory;
    }

    public void setDataDirectory(String dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    public String getCommitLogDirectory() {
        return commitLogDirectory;
    }

    public void setCommitLogDirectory(String commitLogDirectory) {
        this.commitLogDirectory = commitLogDirectory;
    }

    public String getSavedCachesDirectory() {
        return savedCachesDirectory;
    }

    public void setSavedCachesDirectory(String savedCachesDirectory) {
        this.savedCachesDirectory = savedCachesDirectory;
    }

    public int getNumberOfSeeds() {
        return numberOfSeeds;
    }

    public void setNumberOfSeeds(int numberOfSeeds) {
        this.numberOfSeeds = numberOfSeeds;
    }

    public Set<Agent> getSeedNodes() {
        return seedNodes;
    }

    public void setSeedNodes(Set<Agent> seedNodes) {
        this.seedNodes = seedNodes;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    @Override
    public String toString() {
        return "Config{" +
                "clusterName='" + clusterName + '\'' +
                ", domainName='" + domainName + '\'' +
                ", listedAddressNodes=" + listedAddressNode +
                ", rpcAddressNodes=" + rpcAddressNode +
                ", numberOfSeeds=" + numberOfSeeds +
                ", seedNodes=" + seedNodes +
                ", dataDirectory='" + dataDirectory + '\'' +
                ", commitLogDirectory='" + commitLogDirectory + '\'' +
                ", savedCachesDirectory='" + savedCachesDirectory + '\'' +
                '}';
    }

    public void reset() {
//        this.clusterName = "";
//        this.listedAddressNodes = null;
//        rpcAddressNodes = null;
//        numberOfSeeds = 0;
//        seedNodes = null;
//        dataDirectory = "/var/lib/cassandra/data";
//        commitLogDirectory = "/var/lib/cassandra/commitlog";
//        savedCachesDirectory = "/var/lib/cassandra/saved_caches";
    }
}
