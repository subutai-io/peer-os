package io.subutai.common.environment;


import java.util.Set;

import io.subutai.common.host.ContainerHostInfoModel;


public class CreateEnvironmentContainerGroupResponse
{
    private Set<ContainerHostInfoModel> hosts;


    public CreateEnvironmentContainerGroupResponse( final Set<ContainerHostInfoModel> hosts )
    {
        this.hosts = hosts;
    }


    public Set<ContainerHostInfoModel> getHosts()
    {
        return hosts;
    }
}
