package org.safehaus.subutai.api.hive;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.ConfigBase;

public class Config implements ConfigBase {

    public static final String PRODUCT_KEY = "Hive";
    private String clusterName = "";
    private String hadoopClusterName = "";
    private Agent server;
    private Set<Agent> clients = new HashSet();

    @Override
    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getHadoopClusterName() {
        return hadoopClusterName;
    }

    public void setHadoopClusterName(String hadoopClusterName) {
        this.hadoopClusterName = hadoopClusterName;
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

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Config) {
            Config o = (Config)obj;
            return clusterName != null ? clusterName.equals(o.clusterName) : false;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.clusterName);
        return hash;
    }

}
