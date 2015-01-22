package org.safehaus.subutai.core.peer.api.task.clone;


import java.util.Set;

import org.safehaus.subutai.common.peer.HostInfoModel;
import org.safehaus.subutai.core.peer.api.task.TaskResult;


/**
 * Created by timur on 11/30/14.
 */
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
