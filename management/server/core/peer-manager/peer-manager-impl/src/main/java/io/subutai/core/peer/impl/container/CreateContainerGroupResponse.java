package io.subutai.core.peer.impl.container;


import java.util.Set;

import io.subutai.common.peer.HostInfoModel;


public class CreateContainerGroupResponse
{
    private Set<HostInfoModel> hosts;


    public CreateContainerGroupResponse( final Set<HostInfoModel> hosts )
    {
        this.hosts = hosts;
    }


    public Set<HostInfoModel> getHosts()
    {
        return hosts;
    }
}
