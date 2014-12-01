package org.safehaus.subutai.core.peer.api.task.clone;


import org.safehaus.subutai.core.peer.api.task.TaskType;


/**
 * Created by timur on 11/30/14.
 */
public class CloneTaskType extends TaskType
{
    private static final String NAME = "SUBUTAI_CLONE_TASK";


    @Override
    public String getName()
    {
        return NAME;
    }
}
