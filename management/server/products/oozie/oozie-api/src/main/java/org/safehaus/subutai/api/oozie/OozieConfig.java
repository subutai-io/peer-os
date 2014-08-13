package org.safehaus.subutai.api.oozie;

import java.util.Set;
import java.util.UUID;

import org.doomdark.uuid.UUIDGenerator;
import org.safehaus.subutai.shared.protocol.ConfigBase;

/**
 * @author dilshat
 */
public class OozieConfig implements ConfigBase {

    public static final String PRODUCT_KEY = "Oozie";
    String domainInfo;
    private UUID uuid;
    private String server;
    private Set<String> clients;
    private Set<String> hadoopNodes;
    private String clusterName = "";

    public OozieConfig() {
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

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public Set<String> getClients() {
        return clients;
    }

    public void setClients(Set<String> clients) {
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

    public Set<String> getHadoopNodes() {
        return hadoopNodes;
    }

    public void setHadoopNodes(Set<String> hadoopNodes) {
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
