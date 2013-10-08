package org.safehaus.kiskis.mgmt.shared.protocol.elements;

import java.util.List;

/**
 * It defines a management server in the network at agent's end
 */
public class Host {
    private String ip;
    private String hostName;
    private List<Agent> agentList;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public List<Agent> getAgentList() {
        return agentList;
    }

    public void setAgentList(List<Agent> agentList) {
        this.agentList = agentList;
    }

    @Override
    public String toString() {
        return "Host{" +
                "ip='" + ip + '\'' +
                ", hostName='" + hostName + '\'' +
                ", agentList=" + agentList +
                '}';
    }
}
