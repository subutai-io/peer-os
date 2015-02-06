package org.safehaus.subutai.core.peer.api.task.clone;


import java.util.Set;

import org.safehaus.subutai.common.peer.HostInfoModel;
import org.safehaus.subutai.core.peer.api.task.TaskResult;



public class CloneTaskResult extends TaskResult
{
    private Set<HostInfoModel> hostInfos;


    public CloneTaskResult( final Set<HostInfoModel> hostInfos )
    {
        this.hostInfos = hostInfos;
    }


    public Set<HostInfoModel> getHostInfos()
    {
        return hostInfos;
    }
}
