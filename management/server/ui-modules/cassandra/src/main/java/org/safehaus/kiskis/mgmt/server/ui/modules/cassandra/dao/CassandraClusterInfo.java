/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.dao;

import java.util.List;
import java.util.UUID;

/**
 * @author bahadyr
 */
public class CassandraClusterInfo {

    public static final String UUID_LABEL = "uuid",
            NAME_LABEL = "name",
            NODES_LABEL = "nodes",
            SEEDS_LABEL = "seeds",
            DATADIR_LABEL = "dataDir",
            COMMITLOGDIR_LABEL = "commitLogDir",
            SAVEDCACHEDIR_LOG = "savedCacheDir",
            DOMAINNAME_LABEL = "domainName";

    private UUID uuid;
    private String name = "Test Cluster";
    private List<UUID> nodes;
    private List<UUID> seeds;
    private String dataDir = "/var/lib/cassandra/data";
    private String commitLogDir = "/var/lib/cassandra/commitlog";
    private String savedCacheDir = "/var/lib/cassandra/saved_caches";
    private String domainName;

    public CassandraClusterInfo() {
        this.uuid = java.util.UUID.fromString(new com.eaio.uuid.UUID().toString());
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<UUID> getNodes() {
        return nodes;
    }

    public void setNodes(List<UUID> nodes) {
        this.nodes = nodes;
    }

    public List<UUID> getSeeds() {
        return seeds;
    }

    public void setSeeds(List<UUID> seeds) {
        this.seeds = seeds;
    }

    public String getDataDir() {
        return dataDir;
    }

    public void setDataDir(String dataDir) {
        this.dataDir = dataDir;
    }

    public String getCommitLogDir() {
        return commitLogDir;
    }

    public void setCommitLogDir(String commitLogDir) {
        this.commitLogDir = commitLogDir;
    }

    public String getSavedCacheDir() {
        return savedCacheDir;
    }

    public void setSavedCacheDir(String savedCacheDir) {
        this.savedCacheDir = savedCacheDir;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    @Override
    public String toString() {
        return "Cluster{" + "uuid=" + uuid + ", name=" + name + ", "
                + "nodes=" + nodes + ", seeds=" + seeds + ", "
                + "dataDir=" + dataDir + ", commitLogDir=" + commitLogDir + ", "
                + "savedCacheDir=" + savedCacheDir + '}';
    }

}
