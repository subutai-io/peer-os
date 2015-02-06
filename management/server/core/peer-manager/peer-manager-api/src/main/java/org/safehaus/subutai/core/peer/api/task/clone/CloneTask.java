package org.safehaus.subutai.core.peer.api.task.clone;


import java.util.List;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Criteria;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.peer.api.task.Task;



public class CloneTask implements Task<CloneTaskParam, CloneTaskResult, CloneTaskType>
{

    private UUID id;
    private UUID peerId;
    private CloneTaskParam param;
    private CloneTaskResult result;
    private CloneTaskType type = new CloneTaskType();


    public CloneTask( final UUID id, final UUID peerId, final UUID creatorPeerId, final List<Template> templates,
                      final int quantity, final String strategyId, final List<Criteria> criteria )
    {
        this.id = id;
        this.peerId = peerId;
        this.param = new CloneTaskParam( creatorPeerId, templates, quantity, strategyId, criteria );
    }


    @Override
    public UUID getId()
    {
        return id;
    }


    @Override
    public UUID getPeerId()
    {
        return peerId;
    }


    @Override
    public CloneTaskParam getParam()
    {
        return param;
    }


    @Override
    public CloneTaskResult getResult()
    {
        return result;
    }


    @Override
    public void setResult( CloneTaskResult result )
    {
        this.result = result;
    }


    @Override
    public CloneTaskType getType()
    {
        return type;
    }
}
