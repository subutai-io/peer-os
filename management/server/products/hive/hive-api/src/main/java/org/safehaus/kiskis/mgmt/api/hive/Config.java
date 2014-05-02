package org.safehaus.kiskis.mgmt.api.hive;

import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.ConfigBase;

import java.util.Set;

public class Config implements ConfigBase {

    public static final String PRODUCT_KEY = "Hive";
    private String clusterName = "";
    private Agent server;
    private Set<Agent> clients;

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

    @Override
    public String toString() {
        return "Config{" + "clusterName=" + clusterName + ", server=" + server
                + ", clients=" + (clients != null ? clients.size() : 0) + '}';
    }

}
