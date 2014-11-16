package org.safehaus.subutai.core.peer.api;


import java.util.List;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.protocol.Criteria;


/**
 * Created by bahadyr on 11/6/14.
 */
public class CreateContainerInfo
{
    private final UUID creatorPeerId;
    private final UUID environmentId;
    private final List<Template> templates;
    private final int quantity;
    private final String strategyId;
    private final List<Criteria> criteria;
    private final String groupName;


    public CreateContainerInfo( final UUID creatorPeerId, final UUID environmentId, final List<Template> templates,
                                final int quantity, final String strategyId, final List<Criteria> criteria,
                                final String groupName )
    {
        this.creatorPeerId = creatorPeerId;
        this.environmentId = environmentId;
        this.templates = templates;
        this.quantity = quantity;
        this.strategyId = strategyId;
        this.criteria = criteria;
        this.groupName = groupName;
    }


    public UUID getCreatorPeerId()
    {
        return creatorPeerId;
    }


    public UUID getEnvironmentId()
    {
        return environmentId;
    }


    public List<Template> getTemplates()
    {
        return templates;
    }


    public int getQuantity()
    {
        return quantity;
    }


    public String getStrategyId()
    {
        return strategyId;
    }


    public List<Criteria> getCriteria()
    {
        return criteria;
    }


    public String getGroupName()
    {
        return groupName;
    }
}
