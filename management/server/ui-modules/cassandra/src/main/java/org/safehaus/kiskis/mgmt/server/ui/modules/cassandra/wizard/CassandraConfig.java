/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizard;

import java.util.Set;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

/**
 *
 * @author dilshat
 */
public class CassandraConfig {

    private String clusterName = "";
    private String dataDirectory = "";
    private String commitLogDirectory = "";
    private String savedCachesDirectory = "";
    private String domainName = "";
    private Set<Agent> seeds;
    private Set<Agent> selectedAgents;

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
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

    public Set<Agent> getSeeds() {
        return seeds;
    }

    public void setSeeds(Set<Agent> seeds) {
        this.seeds = seeds;
    }

    public Set<Agent> getSelectedAgents() {
        return selectedAgents;
    }

    public void setSelectedAgents(Set<Agent> selectedAgents) {
        this.selectedAgents = selectedAgents;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public void reset() {
        clusterName = "";
        dataDirectory = "";
        commitLogDirectory = "";
        savedCachesDirectory = "";
        domainName = "";
        seeds = null;
        selectedAgents = null;
    }

    @Override
    public String toString() {
        return "CassandraConfig{" + "clusterName=" + clusterName + ", dataDirectory=" + dataDirectory + ", commitLogDirectory=" + commitLogDirectory + ", savedCachesDirectory=" + savedCachesDirectory + ", domainName=" + domainName + ", seeds=" + seeds + ", selectedAgents=" + selectedAgents + '}';
    }

}
