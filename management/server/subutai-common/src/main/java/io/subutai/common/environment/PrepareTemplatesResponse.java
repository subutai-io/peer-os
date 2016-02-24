package io.subutai.common.environment;


import java.util.Set;

import io.subutai.common.host.ContainerHostInfoModel;


public class PrepareTemplatesResponse
{
    private Set<ContainerHostInfoModel> hosts;


    public PrepareTemplatesResponse( final Set<ContainerHostInfoModel> hosts )
    {
        this.hosts = hosts;
    }


    public Set<ContainerHostInfoModel> getHosts()
    {
        return hosts;
    }
}
