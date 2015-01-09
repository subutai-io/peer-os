package org.safehaus.subutai.core.peer.api.task.clone;


import java.util.List;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Criteria;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.peer.api.task.TaskParam;


/**
 * Created by timur on 11/30/14.
 */
public class CloneTaskParam extends TaskParam
{
    private List<Template> templates;
    private UUID creatorPeerId;
    private int quantity;
    private String strategyId;
    private List<Criteria> criteria;


    public CloneTaskParam( final UUID creatorPeerId, final List<Template> templates, final int quantity,
                           final String strategyId, final List<Criteria> criteria )
    {
        this.creatorPeerId = creatorPeerId;
        this.templates = templates;
        this.quantity = quantity;
        this.strategyId = strategyId;
        this.criteria = criteria;
    }


    public List<Template> getTemplates()
    {
        return templates;
    }


    public UUID getCreatorPeerId()
    {
        return creatorPeerId;
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
