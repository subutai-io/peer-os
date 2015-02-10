package org.safehaus.subutai.core.peer.ui.container.executor;


public class AgentExecutionEvent
{
    private String hostName;
    private String containerName;
    private String description;
    private AgentExecutionEventType eventType;


    public AgentExecutionEvent( String hostName, String containerName, AgentExecutionEventType eventType,
                                String description )
    {
        this.hostName = hostName;
        this.containerName = containerName;
        this.eventType = eventType;
        this.description = description;
    }


    public String getHostName()
    {
        return hostName;
    }


    public void setHostName( String hostName )
    {
        this.hostName = hostName;
    }


    public String getContainerName()
    {
        return containerName;
    }


    public void setContainerName( String containerName )
    {
        this.containerName = containerName;
    }


    public String getDescription()
    {
        return description;
    }


    public void setDescription( String description )
    {
        this.description = description;
    }


    public AgentExecutionEventType getEventType()
    {
        return eventType;
    }


    @Override
    public String toString()
    {
        return "AgentExecutionEvent{" +
                "hostName='" + hostName + '\'' +
                ", containerName='" + containerName + '\'' +
                ", description=" + description +
                ", eventType='" + eventType + '\'' +
                '}';
    }
}
