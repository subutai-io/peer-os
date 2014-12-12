package org.safehaus.subutai.core.peer.impl.container;


import java.util.List;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Criteria;
import org.safehaus.subutai.common.protocol.Template;


/**
 * Request for container creation at a remote peer
 */
public class CreateContainerRequest
{
    private final UUID creatorPeerId;
    private final List<Template> templates;
    private final int quantity;
    private final String strategyId;
    private final List<Criteria> criteria;


    public CreateContainerRequest( final UUID creatorPeerId, final List<Template> templates, final int quantity,
                                   final String strategyId, final List<Criteria> criteria )
    {
        this.creatorPeerId = creatorPeerId;
        this.templates = templates;
        this.quantity = quantity;
        this.strategyId = strategyId;
        this.criteria = criteria;
    }


    public UUID getCreatorPeerId()
    {
        return creatorPeerId;
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
}
