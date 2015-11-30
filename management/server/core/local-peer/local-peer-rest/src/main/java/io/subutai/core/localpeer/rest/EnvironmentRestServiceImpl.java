package io.subutai.core.localpeer.rest;


import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.host.ContainerHostState;
import io.subutai.common.metric.ProcessResourceUsage;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.resource.ResourceType;
import io.subutai.common.resource.ResourceValue;


/**
 * Environment REST endpoint implementation
 */
public class EnvironmentRestServiceImpl implements EnvironmentRestService
{
    private static final Logger LOGGER = LoggerFactory.getLogger( EnvironmentRestServiceImpl.class );

    private LocalPeer localPeer;


    public EnvironmentRestServiceImpl( final LocalPeer localPeer )
    {
        this.localPeer = localPeer;
    }


    @Override
    public void destroyContainer( final ContainerId containerId )
    {
        Preconditions.checkNotNull( containerId );
        try
        {
            localPeer.destroyContainer( containerId );
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error destroying container #destroyContainer", e );
            Response response = Response.serverError().entity( e.toString() ).build();
            throw new WebApplicationException( response );
        }
    }


    @Override
    public void startContainer( final ContainerId containerId )
    {
        Preconditions.checkNotNull( containerId );
        try
        {
            localPeer.startContainer( containerId );
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error starting container #startContainer", e );
            Response response = Response.serverError().entity( e.toString() ).build();
            throw new WebApplicationException( response );
        }
    }


    @Override
    public void stopContainer( final ContainerId containerId )
    {
        Preconditions.checkNotNull( containerId );
        try
        {
            localPeer.stopContainer( containerId );
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error stopping container #stopContainer", e );
            Response response = Response.serverError().entity( e.toString() ).build();
            throw new WebApplicationException( response );
        }
    }


    @Override
    public ContainerHostState getContainerState( final ContainerId containerId )
    {
        Preconditions.checkNotNull( containerId );
        Preconditions.checkNotNull( containerId.getId() );
        return localPeer.getContainerState( containerId );
    }


    @Override
    public ProcessResourceUsage getProcessResourceUsage( ContainerId containerId, int pid )
    {
        Preconditions.checkNotNull( containerId );
        Preconditions.checkArgument( pid > 0 );

        try
        {
            return localPeer.getProcessResourceUsage( containerId, pid );
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting processing resource usage #getProcessResourceUsage", e );
            throw new WebApplicationException(
                    Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build() );
        }
    }

    //*********** Quota functions ***************


    @Override
    public Response getCpuSet( final ContainerId containerId )
    {
        try
        {
            Preconditions.checkNotNull( containerId );
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId.getId() ) );

            return Response.ok( localPeer.getContainerHostById( containerId.getId() ).getCpuSet() ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting cpu set #getCpuSet", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response setCpuSet( final ContainerId containerId, final Set<Integer> cpuSet )
    {
        try
        {
            Preconditions.checkNotNull( containerId );
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId.getId() ) );

            localPeer.getContainerHostById( containerId.getId() ).setCpuSet( cpuSet );
            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error setting cpu set #setCpuSet", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getQuota( final ContainerId containerId, final ResourceType resourceType )
    {
        Preconditions.checkNotNull( containerId );
        Preconditions.checkNotNull( resourceType );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId.getId() ) );
        try
        {
            ResourceValue resourceValue = localPeer.getQuota( containerId, resourceType );
            return Response.ok( resourceValue ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error setting disk quota #setDiskQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response setQuota( final ContainerId containerId, final ResourceType resourceType,
                              final ResourceValue resourceValue )
    {
        Preconditions.checkNotNull( containerId );
        Preconditions.checkNotNull( resourceType );
        Preconditions.checkNotNull( resourceValue );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId.getId() ) );

        try
        {
            localPeer.setQuota( containerId, resourceType, resourceValue );
            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error setting disk quota #setDiskQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getAvailableQuota( final ContainerId containerId, final ResourceType resourceType )
    {
        Preconditions.checkNotNull( containerId );
        Preconditions.checkNotNull( resourceType );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId.getId() ) );
        try
        {

            ResourceValue resourceValue = localPeer.getAvailableQuota( containerId, resourceType );
            return Response.ok( resourceValue ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error setting disk quota #setDiskQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }
}
