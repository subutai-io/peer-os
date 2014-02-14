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
    private Agent server;
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

}
