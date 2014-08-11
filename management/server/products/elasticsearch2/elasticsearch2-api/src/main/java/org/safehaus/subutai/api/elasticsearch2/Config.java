/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.api.elasticsearch2;

import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.ConfigBase;

import java.util.Set;

/**
 * @author dilshat
 */
public class Config implements ConfigBase {

    public static final String PRODUCT_KEY = "Elasticsearch2";
    private String clusterName = "";
    private String domainName = "";
    private int numberOfSeeds;
    private int numberOfNodes;
    private Set<Agent> seedNodes;
    private Set<Agent> nodes;
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

    @Override
    public String getProductName() {
        return PRODUCT_KEY;
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

    public Set<Agent> getNodes() {
        return nodes;
    }

    public void setNodes(Set<Agent> nodes) {
        this.nodes = nodes;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public int getNumberOfNodes() {
        return numberOfNodes;
    }

    public void setNumberOfNodes(int numberOfNodes) {
        this.numberOfNodes = numberOfNodes;
    }

    @Override
    public String toString() {
        return "Config{" +
                "clusterName='" + clusterName + '\'' +
                ", domainName='" + domainName + '\'' +
                ", numberOfSeeds=" + numberOfSeeds +
                ", numberOfNodes=" + numberOfNodes +
                ", dataDirectory='" + dataDirectory + '\'' +
                ", commitLogDirectory='" + commitLogDirectory + '\'' +
                ", savedCachesDirectory='" + savedCachesDirectory + '\'' +
                '}';
    }

    public void reset() {

    }
}
