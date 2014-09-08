package org.safehaus.subutai.ui.containermanager.clone;

/**
 * Created by timur on 9/8/14.
 */
public class AgentExecutionEvent {
    private String hostName;
    private String containerName;
    private String description;
    // TODO: create eventType enum
    private String eventType;

    public AgentExecutionEvent(String hostName, String containerName, String eventType, String description) {
        this.hostName = hostName;
        this.containerName = containerName;
        this.eventType = eventType;
        this.description = description;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEventType() {
        return eventType;
    }

    @Override
    public String toString() {
        return "AgentExecutionEvent{" +
                "hostName='" + hostName + '\'' +
                ", containerName='" + containerName + '\'' +
                ", description=" + description +
                ", eventType='" + eventType + '\'' +
                '}';
    }
}
