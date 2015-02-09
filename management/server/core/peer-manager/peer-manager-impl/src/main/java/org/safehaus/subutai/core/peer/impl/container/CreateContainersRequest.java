package org.safehaus.subutai.core.peer.impl.container;


import java.util.List;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Criteria;
import org.safehaus.subutai.common.protocol.Template;


/**
 * Request for container creation at a remote peer
 */
public class CreateContainersRequest
{
    private final UUID environmentId;
    private final UUID initiatorPeerId;
    private final UUID ownerId;
    private final List<Template> templates;
    private final int numberOfContainers;
    private final String strategyId;
    private final List<Criteria> criteria;


    public CreateContainersRequest( final UUID environmentId, final UUID initiatorPeerId, final UUID ownerId,
                                    final List<Template> templates, final int numberOfContainers,
                                    final String strategyId, final List<Criteria> criteria )
    {
        this.environmentId = environmentId;
        this.initiatorPeerId = initiatorPeerId;
        this.ownerId = ownerId;
        this.templates = templates;
        this.numberOfContainers = numberOfContainers;
        this.strategyId = strategyId;
        this.criteria = criteria;
    }


    public UUID getEnvironmentId()
    {
        return environmentId;
    }


    public List<Template> getTemplates()
    {
        return templates;
    }


    public int getNumberOfContainers()
    {
        return numberOfContainers;
    }


    public String getStrategyId()
    {
        return strategyId;
    }


    public List<Criteria> getCriteria()
    {
        return criteria;
    }


    public UUID getInitiatorPeerId()
    {
        return initiatorPeerId;
    }


    public UUID getOwnerId()
    {
        return ownerId;
    }
}
