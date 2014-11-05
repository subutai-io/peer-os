package org.safehaus.subutai.core.environment.impl.environment;


import java.util.List;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.strategy.api.Criteria;


/**
 * Created by bahadyr on 11/5/14.
 */
public class ContainerDistributionMessage
{

    UUID targetPeerId;
    private UUID sourcePeerId;
    private UUID environmentId;
    private List<Template> templates;
    private int numberOfContainers;
    private String placementStrategy;
    private List<Criteria> criterias;


    public UUID getSourcePeerId()
    {
        return sourcePeerId;
    }


    public void setSourcePeerId( final UUID sourcePeerId )
    {
        this.sourcePeerId = sourcePeerId;
    }


    public UUID getEnvironmentId()
    {
        return environmentId;
    }


    public void setEnvironmentId( final UUID environmentId )
    {
        this.environmentId = environmentId;
    }


    public List<Template> getTemplates()
    {
        return templates;
    }


    public void setTemplates( final List<Template> templates )
    {
        this.templates = templates;
    }


    public int getNumberOfContainers()
    {
        return numberOfContainers;
    }


    public void setNumberOfContainers( final int numberOfContainers )
    {
        this.numberOfContainers = numberOfContainers;
    }


    public String getPlacementStrategy()
    {
        return placementStrategy;
    }


    public void setPlacementStrategy( final String placementStrategy )
    {
        this.placementStrategy = placementStrategy;
    }


    public List<Criteria> getCriterias()
    {
        return criterias;
    }


    public void setCriterias( final List<Criteria> criterias )
    {
        this.criterias = criterias;
    }


    public void setTargetPeerId( final UUID targetPeerId )
    {
        this.targetPeerId = targetPeerId;
    }


    public UUID targetPeerId()
    {
        return targetPeerId;
    }
}
