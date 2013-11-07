package org.safehaus.kiskis.mgmt.shared.protocol;

import java.util.List;

/**
 * Used to define a physical host on the whole network. It could be management server or the agent.
 * It just defines a host in the network.
 */
public class Agent {
    private String uuid;
    private String mac;
    private String hostname;
    private List<String> listIP;
    private boolean isLXC;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
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

    public boolean isLXC() {
        return isLXC;
    }

    public void setLXC(boolean LXC) {
        isLXC = LXC;
    }

    @Override
    public String toString() {
        return "Agent{" +
                "uuid='" + uuid + '\'' +
                ", mac='" + mac + '\'' +
                ", hostname='" + hostname + '\'' +
                ", listIP=" + listIP +
                ", isLXC=" + isLXC +
                '}';
    }
}
