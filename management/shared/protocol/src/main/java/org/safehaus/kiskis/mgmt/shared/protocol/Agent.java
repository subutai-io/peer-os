package org.safehaus.kiskis.mgmt.shared.protocol;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Used to define a physical host on the whole network. It could be management
 * server or the agent. It just defines a host in the network.
 */
public class Agent implements Serializable {

    private UUID uuid;
    private String macAddress;
    private String hostname;
    private List<String> listIP;
    private boolean isLXC;
    private Date lastHeartbeat;
    private String parentHostName;
    private String transportId;

    public String getTransportId() {
        return transportId;
    }

    public void setTransportId(String transportId) {
        this.transportId = transportId;
    }

    public String getParentHostName() {
        return parentHostName;
    }

    public void setParentHostName(String parentHostName) {
        this.parentHostName = parentHostName;
    }

    public boolean isIsLXC() {
        return isLXC;
    }

    public void setIsLXC(boolean isLXC) {
        this.isLXC = isLXC;
    }

    public Date getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(Date lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String mac) {
        this.macAddress = mac;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public List<String> getListIP() {
        return listIP;
    }

    public void setListIP(List<String> listIP) {
        this.listIP = listIP;
    }

    @Override
    public String toString() {
        return "Agent{" + "uuid=" + uuid + ", macAddress=" + macAddress + ", hostname=" + hostname + ", listIP=" + listIP + ", isLXC=" + isLXC + ", lastHeartbeat=" + lastHeartbeat + ", parentHostName=" + parentHostName + ", transportId=" + transportId + '}';
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
        if (this.uuid != other.uuid && (this.uuid == null || !this.uuid.equals(other.uuid))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + (this.uuid != null ? this.uuid.hashCode() : 0);
        return hash;
    }
}
