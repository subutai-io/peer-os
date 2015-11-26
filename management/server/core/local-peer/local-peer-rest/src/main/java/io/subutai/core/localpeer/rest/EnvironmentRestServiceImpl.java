package io.subutai.core.localpeer.rest;


import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.reflect.TypeToken;

import io.subutai.common.host.ContainerHostState;
import io.subutai.common.metric.ProcessResourceUsage;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.quota.CpuQuota;
import io.subutai.common.quota.DiskPartition;
import io.subutai.common.quota.DiskQuota;
import io.subutai.common.quota.RamQuota;
import io.subutai.common.util.JsonUtil;


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
    public Response getAvailableRamQuota( final ContainerId containerId )
    {
        try
        {
            Preconditions.checkNotNull( containerId );
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId.getId() ) );

            return Response.ok( localPeer.getContainerHostById( containerId.getId() ).getAvailableRamQuota() ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting available ram quota #getAvailableRamQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getAvailableCpuQuota( final ContainerId containerId )
    {
        try
        {
            Preconditions.checkNotNull( containerId );
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId.getId() ) );

            return Response.ok( localPeer.getContainerHostById( containerId.getId() ).getAvailableCpuQuota() ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting available cpu quota #getAvailableCpuQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getAvailableDiskQuota( final ContainerId containerId, final DiskPartition diskPartition )
    {
        try
        {
            Preconditions.checkNotNull( containerId );
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId.getId() ) );
            Preconditions.checkNotNull( diskPartition );

            return Response
                    .ok( localPeer.getContainerHostById( containerId.getId() ).getAvailableDiskQuota( diskPartition ) )
                    .build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting available disk quota #getAvailableDiskQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getRamQuota( final ContainerId containerId )
    {
        try
        {
            Preconditions.checkNotNull( containerId );
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId.getId() ) );

            return Response.ok( localPeer.getContainerHostById( containerId.getId() ).getRamQuota() ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting ram quota #getRamQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response setRamQuota( final ContainerId containerId, final RamQuota ramQuota )
    {
        try
        {
            Preconditions.checkNotNull( containerId );
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId.getId() ) );

            localPeer.getContainerHostById( containerId.getId() ).setRamQuota( ramQuota );
            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error setting ram quota #setRamQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getCpuQuota( final ContainerId containerId )
    {
        try
        {
            Preconditions.checkNotNull( containerId );
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId.getId() ) );

            return Response.ok( localPeer.getContainerHostById( containerId.getId() ).getCpuQuota() ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting cpu quota #getCpuQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response setCpuQuota( final ContainerId containerId, final CpuQuota cpuQuota )
    {
        try
        {
            Preconditions.checkNotNull( containerId );
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId.getId() ) );

            localPeer.getContainerHostById( containerId.getId() ).setCpuQuota( cpuQuota );
            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error setting cpu quota #setCpuQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


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
    public Response getDiskQuota( final ContainerId containerId, final DiskPartition diskPartition )
    {
        try
        {
            Preconditions.checkNotNull( containerId );
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId.getId() ) );

            return Response.ok( localPeer.getContainerHostById( containerId.getId() ).getDiskQuota( diskPartition ) )
                           .build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting disk quota #getDiskQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response setDiskQuota( final ContainerId containerId, final DiskQuota diskQuota )
    {
        try
        {
            Preconditions.checkNotNull( containerId );
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId.getId() ) );

            localPeer.getContainerHostById( containerId.getId() ).setDiskQuota( diskQuota );
            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error setting disk quota #setDiskQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }
}
