package org.safehaus.subutai.core.peer.impl.container;


import java.util.Set;

import org.safehaus.subutai.core.peer.api.HostInfoModel;


/**
 * Create container response
 */
public class CreateContainerResponse
{
    private Set<HostInfoModel> hosts;


    public CreateContainerResponse( final Set<HostInfoModel> hosts )
    {
        this.hosts = hosts;
    }


    public Set<HostInfoModel> getHosts()
    {
        return hosts;
    }
}

