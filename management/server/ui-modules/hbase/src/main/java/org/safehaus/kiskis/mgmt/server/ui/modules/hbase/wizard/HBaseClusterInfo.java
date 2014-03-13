/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.hbase.wizard;

import org.doomdark.uuid.UUIDGenerator;

import java.util.Set;
import java.util.UUID;

/**
 * @author bahadyr
 */
public class HBaseClusterInfo {

    public static final String UUID_LABEL = "uuid",
            NAME_LABEL = "name",
            NODES_LABEL = "nodes",
            SEEDS_LABEL = "seeds",
            DOMAINNAME_LABEL = "domainName";

    private UUID uuid;
    private Set<UUID> master;
    private Set<UUID> region;
    private Set<UUID> quorum;
    private Set<UUID> bmasters;
    private String domainName = "HBase";
    private Set<UUID> allnodes;

    public HBaseClusterInfo() {
        this.uuid = java.util.UUID.fromString(UUIDGenerator.getInstance().generateTimeBasedUUID().toString());
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Set<UUID> getMaster() {
        return master;
    }

    public void setMaster(Set<UUID> master) {
        this.master = master;
    }

    public Set<UUID> getRegion() {
        return region;
    }

    public void setRegion(Set<UUID> region) {
        this.region = region;
    }

    public Set<UUID> getQuorum() {
        return quorum;
    }

    public void setQuorum(Set<UUID> quorum) {
        this.quorum = quorum;
    }

    public Set<UUID> getBmasters() {
        return bmasters;
    }

    public void setBmasters(Set<UUID> bmasters) {
        this.bmasters = bmasters;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public Set<UUID> getAllnodes() {
        return allnodes;
    }

    public void setAllnodes(Set<UUID> allnodes) {
        this.allnodes = allnodes;
    }

    @Override
    public String toString() {
        return "HBaseClusterInfo{" + "uuid=" + uuid + ", master=" + master + ", region=" + region + ", quorum=" + quorum + ", bmasters=" + bmasters + ", domainName=" + domainName + '}';
    }

}
