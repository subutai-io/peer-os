package org.safehaus.subutai.core.peer.api.task;


import java.util.UUID;


public interface Task<P extends TaskParam, R extends TaskResult, T extends TaskType>
{
    public UUID getId();

    public UUID getPeerId();

    public P getParam();

    public R getResult();

    public void setResult( R result );

    public T getType();
}
