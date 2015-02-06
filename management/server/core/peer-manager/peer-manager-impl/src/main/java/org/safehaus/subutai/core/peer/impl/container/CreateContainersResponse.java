package org.safehaus.subutai.core.peer.impl.container;


import java.util.Set;

import org.safehaus.subutai.common.peer.HostInfoModel;


/**
 * Create container response
 */
public class CreateContainersResponse
{
    private Set<HostInfoModel> hosts;


    public CreateContainersResponse( final Set<HostInfoModel> hosts )
    {
        this.hosts = hosts;
    }


    public Set<HostInfoModel> getHosts()
    {
        return hosts;
    }
}

