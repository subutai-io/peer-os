package org.safehaus.subutai.core.container.api;


import java.util.UUID;


/**
 * Container event type.
 */
public class ContainerEvent
{
    private ContainerEventType eventType;
    private UUID envId;
    private String parentHostname;
    private String hostname;
    private long timestamp;


    public ContainerEvent( ContainerEventType eventType, UUID envId, String parentHostname, String hostname )
    {
        this.eventType = eventType;
        this.envId = envId;
        this.hostname = hostname;
        this.parentHostname = parentHostname;
        this.timestamp = System.currentTimeMillis();
    }


    public ContainerEventType getEventType()
    {
        return eventType;
    }


    public void setEventType( final ContainerEventType eventType )
    {
        this.eventType = eventType;
    }


    public long getTimestamp()
    {
        return timestamp;
    }


    public void setTimestamp( final long timestamp )
    {
        this.timestamp = timestamp;
    }


    public String getParentHostname()
    {
        return parentHostname;
    }


    public void setParentHostname( final String parentHostname )
    {
        this.parentHostname = parentHostname;
    }


    public String getHostname()
    {
        return hostname;
    }


    public void setHostname( final String hostname )
    {
        this.hostname = hostname;
    }


    public UUID getEnvId()
    {
        return envId;
    }


    public void setEnvId( final UUID envId )
    {
        this.envId = envId;
    }
}
