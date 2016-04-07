package io.subutai.core.localpeer.rest;


import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.environment.HostAddresses;
import io.subutai.common.environment.SshPublicKeys;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostId;
import io.subutai.common.metric.ProcessResourceUsage;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.quota.ContainerQuota;


/**
 * Environment REST endpoint implementation
 */
public class EnvironmentRestServiceImpl implements EnvironmentRestService
{
    private static final Logger LOGGER = LoggerFactory.getLogger( EnvironmentRestServiceImpl.class );

    private LocalPeer localPeer;


    public EnvironmentRestServiceImpl( final LocalPeer localPeer )
    {
        Preconditions.checkNotNull( localPeer );

        this.localPeer = localPeer;
    }


    @Override
    public void destroyContainer( final ContainerId containerId )
    {
        try
        {
            Preconditions.checkNotNull( containerId );

            localPeer.destroyContainer( containerId );
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage(), e );
            throw new WebApplicationException( Response.serverError().entity( e.getMessage() ).build() );
        }
    }


    @Override
    public void startContainer( final ContainerId containerId )
    {
        try
        {
            Preconditions.checkNotNull( containerId );

            localPeer.startContainer( containerId );
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage(), e );
            throw new WebApplicationException( Response.serverError().entity( e.getMessage() ).build() );
        }
    }


    @Override
    public void stopContainer( final ContainerId containerId )
    {
        try
        {
            Preconditions.checkNotNull( containerId );

            localPeer.stopContainer( containerId );
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage(), e );
            throw new WebApplicationException( Response.serverError().entity( e.getMessage() ).build() );
        }
    }


    @Override
    public ContainerHostState getContainerState( final ContainerId containerId )
    {
        try
        {
            Preconditions.checkNotNull( containerId );
            Preconditions.checkNotNull( containerId.getId() );
            return localPeer.getContainerState( containerId );
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage(), e );
            throw new WebApplicationException( Response.serverError().entity( e.getMessage() ).build() );
        }
    }


    @Override
    public ProcessResourceUsage getProcessResourceUsage( ContainerId containerId, int pid )
    {
        try
        {
            Preconditions.checkNotNull( containerId );
            Preconditions.checkArgument( pid > 0 );

            return localPeer.getProcessResourceUsage( containerId, pid );
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage(), e );
            throw new WebApplicationException( Response.serverError().entity( e.getMessage() ).build() );
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
            LOGGER.error( e.getMessage(), e );
            throw new WebApplicationException( Response.serverError().entity( e.getMessage() ).build() );
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
            LOGGER.error( e.getMessage(), e );
            throw new WebApplicationException( Response.serverError().entity( e.getMessage() ).build() );
        }
    }


    @Override
    public SshPublicKeys generateSshKeysForEnvironment( final EnvironmentId environmentId )
    {
        try
        {
            Preconditions.checkNotNull( environmentId );

            return localPeer.generateSshKeyForEnvironment( environmentId );
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage(), e );
            throw new WebApplicationException( Response.serverError().entity( e.getMessage() ).build() );
        }
    }


    @Override
    public void addSshKey( final EnvironmentId environmentId, final String sshPublicKey )
    {
        try
        {
            Preconditions.checkNotNull( environmentId );
            Preconditions.checkArgument( !Strings.isNullOrEmpty( sshPublicKey ) );

            localPeer.addSshKey( environmentId, sshPublicKey );
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage(), e );
            throw new WebApplicationException( Response.serverError().entity( e.getMessage() ).build() );
        }
    }


    @Override
    public void removeSshKey( final EnvironmentId environmentId, final String sshPublicKey )
    {
        try
        {
            Preconditions.checkNotNull( environmentId );
            Preconditions.checkArgument( !Strings.isNullOrEmpty( sshPublicKey ) );

            localPeer.removeSshKey( environmentId, sshPublicKey );
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage(), e );
            throw new WebApplicationException( Response.serverError().entity( e.getMessage() ).build() );
        }
    }


    @Override
    public Response configureSshInEnvironment( final EnvironmentId environmentId, final SshPublicKeys sshPublicKeys )
    {
        try
        {
            Preconditions.checkNotNull( environmentId );
            Preconditions.checkNotNull( sshPublicKeys );
            Preconditions.checkArgument( !sshPublicKeys.isEmpty() );

            localPeer.configureSshInEnvironment( environmentId, sshPublicKeys );

            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage(), e );
            throw new WebApplicationException( Response.serverError().entity( e.getMessage() ).build() );
        }
    }


    @Override
    public Response configureHostsInEnvironment( final EnvironmentId environmentId, final HostAddresses hostAddresses )
    {
        try
        {
            Preconditions.checkNotNull( environmentId );
            Preconditions.checkNotNull( hostAddresses );
            Preconditions.checkArgument( !hostAddresses.isEmpty() );

            localPeer.configureHostsInEnvironment( environmentId, hostAddresses );

            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage(), e );
            throw new WebApplicationException( Response.serverError().entity( e.getMessage() ).build() );
        }
    }


    @Override
    public Response getQuota( final ContainerId containerId )
    {
        try
        {
            Preconditions.checkNotNull( containerId );
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId.getId() ) );

            ContainerQuota resourceValue = localPeer.getQuota( containerId );

            return Response.ok( resourceValue ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage(), e );
            throw new WebApplicationException( Response.serverError().entity( e.getMessage() ).build() );
        }
    }


    @Override
    public Response setQuota( final ContainerId containerId, ContainerQuota containerQuota )
    {
        try
        {
            Preconditions.checkNotNull( containerId );
            Preconditions.checkNotNull( containerQuota );
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId.getId() ) );

            localPeer.setQuota( containerId, containerQuota );

            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage(), e );
            throw new WebApplicationException( Response.serverError().entity( e.getMessage() ).build() );
        }
    }


    @Override
    public HostId getResourceHostIdByContainerId( final ContainerId containerId )
    {
        try
        {
            Preconditions.checkNotNull( containerId );
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId.getId() ) );

            return localPeer.getResourceHostIdByContainerId( containerId );
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage(), e );
            throw new WebApplicationException( Response.serverError().entity( e.getMessage() ).build() );
        }
    }
}
