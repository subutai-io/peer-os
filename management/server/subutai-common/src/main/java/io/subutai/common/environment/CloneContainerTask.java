package io.subutai.common.environment;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;

import io.subutai.common.host.HostId;
import io.subutai.common.network.NetworkResource;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.peer.ResourceHostException;
import io.subutai.common.settings.Common;
import io.subutai.common.task.CloneRequest;
import io.subutai.common.util.HostUtil;


public class CloneContainerTask extends HostUtil.Task<String>
{
    protected static final Logger LOG = LoggerFactory.getLogger( CloneContainerTask.class );
    private static final int DEFAULT_PARALLEL_CLONE_TASK_AMOUNT = 2;

    private final CloneRequest request;
    private final ResourceHost resourceHost;
    private final NetworkResource networkResource;
    private final LocalPeer localPeer;


    public CloneContainerTask( final CloneRequest request, final ResourceHost resourceHost,
                               final NetworkResource networkResource, final LocalPeer localPeer )
    {
        Preconditions.checkNotNull( request );
        Preconditions.checkNotNull( resourceHost );
        Preconditions.checkNotNull( networkResource );
        Preconditions.checkNotNull( localPeer );

        this.request = request;
        this.resourceHost = resourceHost;
        this.networkResource = networkResource;
        this.localPeer = localPeer;
    }


    public CloneRequest getRequest()
    {
        return request;
    }


    @Override
    public int maxParallelTasks()
    {
        try
        {
            return resourceHost.getNumberOfCpuCores();
        }
        catch ( ResourceHostException e )
        {
            LOG.warn( "Failed to get RH cpu cores number. Defaulting to {}", DEFAULT_PARALLEL_CLONE_TASK_AMOUNT, e );

            return DEFAULT_PARALLEL_CLONE_TASK_AMOUNT;
        }
    }


    @Override
    public String name()
    {
        return String.format( "Clone %s from %s", request.getHostname(), request.getTemplateName() );
    }


    @Override
    public String call() throws Exception
    {
        //update hostname to make it unique on this peer
        //VLAN will make it unique on this peer
        //additional suffix (last IP octet) will make it unique inside host environment
        request.setHostname( String.format( "%s-%d-%s", request.getHostname(), networkResource.getVlan(),
                StringUtils.substringAfterLast( request.getIp().split( "/" )[0], "." ) ) );

        String containerId = resourceHost
                .cloneContainer( request.getTemplateName(), request.getHostname(), request.getIp(),
                        networkResource.getVlan(), networkResource.getEnvironmentId() );

        //wait for container connection
        boolean connected = false;

        long waitStart = System.currentTimeMillis();

        HostId containerHostId = new HostId( containerId );

        while ( !connected && System.currentTimeMillis() - waitStart < Common.WAIT_CONTAINER_CONNECTION_SEC * 1000 )
        {
            connected = localPeer.isConnected( containerHostId );

            if ( !connected )
            {
                Thread.sleep( 100 );
            }
        }

        if ( !connected )
        {
            throw new IllegalStateException(
                    String.format( "Container %s has not connected within %d sec", request.getHostname(),
                            Common.WAIT_CONTAINER_CONNECTION_SEC ) );
        }

        return containerId;
    }
}
