package org.safehaus.kiskis.mgmt.server.ui.modules.oozie;

import org.doomdark.uuid.UUIDGenerator;

import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopClusterInfo;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

/**
 * @author dilshat
 */
public class OozieConfig implements Serializable {

    private UUID uuid;
    String domainInfo;
    private Agent server;
    private Set<Agent> clients;
    HadoopClusterInfo cluster;

    public OozieConfig() {
        this.uuid = java.util.UUID.fromString(UUIDGenerator.getInstance().generateTimeBasedUUID().toString());
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void reset() {
        this.server = null;
        this.clients = null;
        this.domainInfo = "";
    }

    public String getDomainInfo() {
        return domainInfo;
    }

    public void setDomainInfo(String domainInfo) {
        this.domainInfo = domainInfo;
    }

    public Agent getServer() {
        return server;
    }

    public void setServer(Agent server) {
        this.server = server;
    }

    public Set<Agent> getClients() {
        return clients;
    }

    public void setClients(Set<Agent> clients) {
        this.clients = clients;
    }

    public HadoopClusterInfo getCluster() {
        return cluster;
    }

    public void setCluster(HadoopClusterInfo cluster) {
        this.cluster = cluster;
    }

}
