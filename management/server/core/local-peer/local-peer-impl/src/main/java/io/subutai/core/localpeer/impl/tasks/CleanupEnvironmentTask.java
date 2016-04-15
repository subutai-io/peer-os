package io.subutai.core.localpeer.impl.tasks;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.subutai.common.network.NetworkResource;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.peer.ResourceHostException;
import io.subutai.common.util.HostUtil;


public class CleanupEnvironmentTask extends HostUtil.Task<Object>
{
    private static final Logger LOG = LoggerFactory.getLogger( CleanupEnvironmentTask.class );

    private final ResourceHost resourceHost;
    private final NetworkResource networkResource;


    public CleanupEnvironmentTask( final ResourceHost resourceHost, final NetworkResource networkResource )
    {
        Preconditions.checkNotNull( resourceHost );
        Preconditions.checkNotNull( networkResource );

        this.resourceHost = resourceHost;
        this.networkResource = networkResource;
    }


    @Override
    public int maxParallelTasks()
    {
        return 0;
    }


    @Override
    public String name()
    {
        return String.format( "Cleanup environment %s", networkResource.getEnvironmentId() );
    }


    @Override
    public Object call() throws Exception
    {
        try
        {
            resourceHost.cleanup( new EnvironmentId( networkResource.getEnvironmentId() ), networkResource.getVlan() );
        }
        catch ( ResourceHostException e )
        {
            LOG.error( "Failed to cleanup environment {} on RH {}", networkResource.getEnvironmentId(),
                    resourceHost.getId(), e );
        }

        return null;
    }
}
