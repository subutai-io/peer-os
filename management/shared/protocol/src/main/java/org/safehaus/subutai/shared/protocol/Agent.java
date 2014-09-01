package org.safehaus.subutai.shared.protocol;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Used to define a physical host on the whole network. It could be management
 * server or the agent. It just defines a host in the network.
 */
public class Agent implements Serializable, Comparable<Agent> {

	private UUID uuid;
	private String macAddress;
	private String hostname;
	private List<String> listIP;
	private boolean isLXC;
	private String parentHostName;
	private String transportId;

	public Agent(UUID uuid, String hostname, String parentHostName, String macAddress, List<String> listIP, boolean isLXC, String transportId) {
		Preconditions.checkNotNull(uuid, "UUID is null");
		Preconditions.checkArgument(!Strings.isNullOrEmpty(hostname), "Hostname is null or empty");

		this.uuid = uuid;
		this.macAddress = macAddress;
		this.hostname = hostname;
		this.listIP = listIP;
		this.isLXC = isLXC;
		this.parentHostName = parentHostName;
		this.transportId = transportId;
	}

	public String getTransportId() {
		return transportId;
	}


	public String getParentHostName() {
		return parentHostName;
	}


	public boolean isIsLXC() {
		return isLXC;
	}


	public UUID getUuid() {
		return uuid;
	}


	public String getMacAddress() {
		return macAddress;
	}

	public List<String> getListIP() {
		return Collections.unmodifiableList(listIP);
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 83 * hash + (this.uuid != null ? this.uuid.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Agent other = (Agent) obj;
		return !(this.uuid != other.uuid && (this.uuid == null || !this.uuid.equals(other.uuid)));
	}

	@Override
	public String toString() {
		return "Agent{" + "uuid=" + uuid + ", macAddress=" + macAddress + ", hostname=" + hostname + ", listIP=" + listIP + ", isLXC=" + isLXC + ", parentHostName=" + parentHostName + ", transportId=" + transportId + '}';
	}

	@Override
	public int compareTo(Agent o) {
		if (hostname != null && o != null) {
			return hostname.compareTo(o.getHostname());
		}

		return -1;
	}

	public String getHostname() {
		return hostname;
	}

    public void setHostname( String hostname ) {
        this.hostname = hostname;
    }
}
