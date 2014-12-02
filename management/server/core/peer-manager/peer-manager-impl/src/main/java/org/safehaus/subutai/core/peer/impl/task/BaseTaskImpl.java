package org.safehaus.subutai.core.peer.impl.task;


import org.safehaus.subutai.core.peer.api.task.Task;


/**
 * Created by timur on 11/30/14.
 */
public abstract class BaseTaskImpl<T extends Task>
{
    protected T task;


    public BaseTaskImpl( T task )
    {
        this.task = task;
    }


    public T getTask()
    {
        return task;
    }


    public abstract void run();
}
