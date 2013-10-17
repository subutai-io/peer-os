package org.safehaus.kiskismgmt.protocol;

/**
 * Used to define a physical host on the whole network. It could be management server or the agent.
 * It just defines a host in the network.
 */
public class Agent {
    private String uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        return "Agent{" +
                "uuid='" + uuid + '\'' +
                '}';
    }
}
