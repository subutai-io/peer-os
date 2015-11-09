package io.subutai.common.environment;


import java.util.Set;

import io.subutai.common.host.HostInfoModel;


public class CreateEnvironmentContainerGroupResponse
{
    private Set<HostInfoModel> hosts;


    public CreateEnvironmentContainerGroupResponse( final Set<HostInfoModel> hosts )
    {
        this.hosts = hosts;
    }


    public Set<HostInfoModel> getHosts()
    {
        return hosts;
    }
}
