package org.safehaus.subutai.api.oozie;

import org.doomdark.uuid.UUIDGenerator;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.ConfigBase;

import java.util.Set;
import java.util.UUID;

/**
 * @author dilshat
 */
public class Config implements ConfigBase {

    public static final String PRODUCT_KEY = "Oozie";
    String domainInfo;
    private UUID uuid;
    private Agent server;
    private Set<Agent> clients;
    private Set<Agent> hadoopNodes;
    private String clusterName = "";

    public Config() {
        this.uuid = UUID.fromString(UUIDGenerator.getInstance().generateTimeBasedUUID().toString());
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

    public Set<Agent> getHadoopNodes() {
        return hadoopNodes;
    }

    public void setHadoopNodes(Set<Agent> hadoopNodes) {
        this.hadoopNodes = hadoopNodes;
    }

    @Override
    public String toString() {
        return "OozieConfig{" +
                "uuid=" + uuid +
                ", domainInfo='" + domainInfo + '\'' +
                ", server=" + server +
                ", clients=" + clients +
                ", hadoopNodes=" + hadoopNodes +
                ", clusterName='" + clusterName + '\'' +
                '}';
    }
}
