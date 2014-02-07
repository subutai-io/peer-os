package org.safehaus.kiskis.mgmt.server.ui.modules.oozie;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

/**
 *
 * @author dilshat
 */
public class OozieConfig implements Serializable {

    private UUID uuid;
    String domainInfo;
    private Set<Agent> servers;
    private Set<Agent> clients;

    public OozieConfig() {
        this.uuid = java.util.UUID.fromString(new com.eaio.uuid.UUID().toString());
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void reset() {
        this.servers = null;
        this.clients = null;
        this.domainInfo = "";
    }

    public String getDomainInfo() {
        return domainInfo;
    }

    public void setDomainInfo(String domainInfo) {
        this.domainInfo = domainInfo;
    }

    public Set<Agent> getServers() {
        return servers;
    }

    public void setServers(Set<Agent> servers) {
        this.servers = servers;
    }

    public Set<Agent> getClients() {
        return clients;
    }

    public void setClients(Set<Agent> clients) {
        this.clients = clients;
    }

}
