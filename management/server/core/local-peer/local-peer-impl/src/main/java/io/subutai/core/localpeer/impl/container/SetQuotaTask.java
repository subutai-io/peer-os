package io.subutai.core.localpeer.impl.container;


import com.google.common.base.Preconditions;

import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.task.CloneRequest;
import io.subutai.common.util.HostUtil;


public class SetQuotaTask extends HostUtil.Task
{
    private final CloneRequest request;
    private final ResourceHost resourceHost;
    private final ContainerHost containerHost;


    public SetQuotaTask( final CloneRequest request, final ResourceHost resourceHost,
                         final ContainerHost containerHost )
    {
        Preconditions.checkNotNull( request );
        Preconditions.checkNotNull( resourceHost );
        Preconditions.checkNotNull( containerHost );

        this.request = request;
        this.resourceHost = resourceHost;
        this.containerHost = containerHost;
    }


    @Override
    public int maxParallelTasks()
    {
        return 0;
    }


    @Override
    public String name()
    {
        return String.format( "Set quota %s to container %s", request.getContainerQuota(), request.getHostname() );
    }


    @Override
    public Object call() throws Exception
    {
        resourceHost.setContainerQuota( containerHost, request.getContainerQuota() );

        return null;
    }
}
