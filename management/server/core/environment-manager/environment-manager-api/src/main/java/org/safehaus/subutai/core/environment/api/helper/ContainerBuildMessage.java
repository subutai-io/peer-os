package org.safehaus.subutai.core.environment.api.helper;


import java.util.List;
import java.util.UUID;


/**
 * Created by bahadyr on 9/14/14.
 */
public class ContainerBuildMessage
{


    private int numberOfContainers;
    private String templateName;
    private String strategy;
    private UUID environmentUuid;
    private boolean completeState;
    private int timestamp;
    private UUID targetPeerId;
    private UUID peerId;
    private List<String> criteria;


    public ContainerBuildMessage()
    {
        this.numberOfContainers = 0;
    }


    public List<String> getCriteria()
    {
        return criteria;
    }


    public void setCriteria( final List<String> criteria )
    {
        this.criteria = criteria;
    }


    public UUID getPeerId()
    {
        return peerId;
    }


    public void setPeerId( final UUID peerId )
    {
        this.peerId = peerId;
    }


    public int getNumberOfContainers()
    {
        return numberOfContainers;
    }


    public void setNumberOfContainers( final int numberOfContainers )
    {
        this.numberOfContainers = numberOfContainers;
    }


    public String getTemplateName()
    {
        return templateName;
    }


    public void setTemplateName( final String templateName )
    {
        this.templateName = templateName;
    }


    public String getStrategy()
    {
        return strategy;
    }


    public void setStrategy( final String strategy )
    {
        this.strategy = strategy;
    }


    public UUID getEnvironmentUuid()
    {
        return environmentUuid;
    }


    public void setEnvironmentUuid( final UUID environmentUuid )
    {
        this.environmentUuid = environmentUuid;
    }


    public boolean isCompleteState()
    {
        return completeState;
    }


    public void setCompleteState( final boolean completeState )
    {
        this.completeState = completeState;
    }


    public int getTimestamp()
    {
        return timestamp;
    }


    public void setTimestamp( final int timestamp )
    {
        this.timestamp = timestamp;
    }


    public UUID getTargetPeerId()
    {
        return targetPeerId;
    }


    public void setTargetPeerId( final UUID targetPeerId )
    {
        this.targetPeerId = targetPeerId;
    }


    public void incrementNumOfCont()
    {
        this.numberOfContainers++;
    }
}
