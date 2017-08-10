package io.subutai.common.environment;


import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;

import io.subutai.common.host.HostId;
import io.subutai.common.network.NetworkResource;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.peer.ResourceHostException;
import io.subutai.common.protocol.Template;
import io.subutai.common.settings.Common;
import io.subutai.common.task.CloneRequest;
import io.subutai.common.util.HostUtil;
import io.subutai.common.util.StringUtil;


public class CloneContainerTask extends HostUtil.Task<String>
{
    protected static final Logger LOG = LoggerFactory.getLogger( CloneContainerTask.class );
    private static final int DEFAULT_PARALLEL_CLONE_TASK_AMOUNT = 2;

    private final CloneRequest request;
    private final Template template;
    private final ResourceHost resourceHost;
    private final NetworkResource networkResource;
    private final LocalPeer localPeer;
    private final Set<String> namesToExclude;


    public CloneContainerTask( final CloneRequest request, final Template template, final ResourceHost resourceHost,
                               final NetworkResource networkResource, final LocalPeer localPeer,
                               final Set<String> namesToExclude )
    {
        Preconditions.checkNotNull( request );
        Preconditions.checkNotNull( template );
        Preconditions.checkNotNull( resourceHost );
        Preconditions.checkNotNull( networkResource );
        Preconditions.checkNotNull( localPeer );
        Preconditions.checkNotNull( namesToExclude );

        this.request = request;
        this.template = template;
        this.resourceHost = resourceHost;
        this.networkResource = networkResource;
        this.localPeer = localPeer;
        this.namesToExclude = namesToExclude;
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
        return String.format( "Clone %s from %s", request.getHostname(), template.getName() );
    }


    @Override
    public String call() throws Exception
    {
        //cleanse hostname
        request.setHostname( StringUtil.removeHtmlAndSpecialChars( request.getHostname(), true ) );

        String containerName = generateContainerName();

        while ( namesToExclude.contains( containerName.toLowerCase() ) )
        {
            containerName = generateContainerName();
        }

        request.setContainerName( containerName );

        String containerId = resourceHost
                .cloneContainer( template, request.getContainerName(), request.getHostname(), request.getIp(),
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


    private String generateContainerName()
    {
        //update hostname to make it unique on this peer
        //append 3 random letters
        //append VLAN, it will make it unique on this peer
        //append additional suffix (last IP octet) that will make it unique inside host environment

        return String
                .format( "%s-%s-%d-%s", request.getHostname(), RandomStringUtils.randomAlphanumeric( 3 ).toLowerCase(),
                        networkResource.getVlan(),
                        StringUtils.substringAfterLast( request.getIp().split( "/" )[0], "." ) );
    }
}
