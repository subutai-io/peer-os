package org.safehaus.subutai.api.hive;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ConfigBase;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

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

	@Override
	public String getProductName() {
		return PRODUCT_KEY;
	}

	public String getHadoopClusterName() {
		return hadoopClusterName;
	}

	public void setHadoopClusterName(String hadoopClusterName) {
		this.hadoopClusterName = hadoopClusterName;
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
	public int hashCode() {
		int hash = 7;
		hash = 29 * hash + Objects.hashCode(this.clusterName);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Config) {
			Config o = (Config) obj;
			return clusterName != null ? clusterName.equals(o.clusterName) : false;
		}
		return false;
	}

	@Override
	public String toString() {
		return "Config{" + "clusterName=" + clusterName + ", server=" + server
				+ ", clients=" + (clients != null ? clients.size() : 0) + '}';
	}

}
