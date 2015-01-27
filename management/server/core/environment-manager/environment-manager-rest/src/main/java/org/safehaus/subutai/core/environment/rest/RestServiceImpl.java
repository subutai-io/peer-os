package org.safehaus.subutai.core.environment.rest;


import java.util.Set;
import java.util.UUID;

import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.safehaus.subutai.common.host.ContainerHostState;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.peer.PeerException;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentManagerException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.gson.JsonSyntaxException;


public class RestServiceImpl implements RestService
{
    private static final Logger LOG = LoggerFactory.getLogger( RestServiceImpl.class.getName() );

    private static final String ERROR_KEY = "ERROR";
    private final EnvironmentManager environmentManager;
    private final PeerManager peerManager;


    public RestServiceImpl( final EnvironmentManager environmentManager, final PeerManager peerManager )
    {
        Preconditions.checkNotNull( environmentManager );
        Preconditions.checkNotNull( peerManager );

        this.environmentManager = environmentManager;
        this.peerManager = peerManager;
    }


    @Override
    public Response buildLocalEnvironment( final String blueprint )
    {
        EnvironmentBlueprint environmentBlueprint;
        try
        {
            environmentBlueprint = JsonUtil.fromJson( blueprint, EnvironmentBlueprint.class );
        }
        catch ( JsonSyntaxException | NullPointerException e )
        {
            return Response.status( Response.Status.BAD_REQUEST ).entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) )
                           .build();
        }
        try
        {
            Environment environment = environmentManager.buildEnvironment( environmentBlueprint );
            return Response.ok( JsonUtil.toJson(
                    new EnvironmentJson( environment.getId(), environment.getName(), environment.getStatus(),
                            environment.getPublicKey(),
                            convertContainersToContainerJson( environment.getContainerHosts() ) ) ) ).build();
        }
        catch ( EnvironmentBuildException e )
        {
            return Response.serverError().entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) ).build();
        }
    }


    @Override
    public Response getContainerEnvironmentId( final String containerId )
    {
        if ( !UUIDUtil.isStringAUuid( containerId ) )
        {
            return Response.status( Response.Status.BAD_REQUEST )
                           .entity( JsonUtil.toJson( ERROR_KEY, "Invalid container id" ) ).build();
        }
        UUID hostId = UUID.fromString( containerId );

        Environment environment = findEnvironmentByContainerId( hostId );

        if ( environment != null )
        {
            return Response.ok( environment.getId().toString() ).build();
        }

        return Response.status( Response.Status.NOT_FOUND ).build();
    }


    @Override
    public Response getEnvironment( final String environmentId )
    {
        if ( !UUIDUtil.isStringAUuid( environmentId ) )
        {
            return Response.status( Response.Status.BAD_REQUEST )
                           .entity( JsonUtil.toJson( ERROR_KEY, "Invalid environment id" ) ).build();
        }

        UUID envId = UUID.fromString( environmentId );

        try
        {
            Environment environment = environmentManager.findEnvironmentByID( envId );

            return Response.ok( JsonUtil.toJson(
                    new EnvironmentJson( environment.getId(), environment.getName(), environment.getStatus(),
                            environment.getPublicKey(),
                            convertContainersToContainerJson( environment.getContainerHosts() ) ) ) ).build();
        }
        catch ( EnvironmentManagerException e )
        {
            return Response.serverError().entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) ).build();
        }
    }


    @Override
    public Response destroyEnvironment( final String environmentId )
    {
        if ( !UUIDUtil.isStringAUuid( environmentId ) )
        {
            return Response.status( Response.Status.BAD_REQUEST )
                           .entity( JsonUtil.toJson( ERROR_KEY, "Invalid environment id" ) ).build();
        }

        UUID envId = UUID.fromString( environmentId );

        try
        {
            environmentManager.destroyEnvironment( envId );

            return Response.ok().build();
        }
        catch ( EnvironmentManagerException e )
        {
            return Response.serverError().entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) ).build();
        }
    }


    @Override
    public Response destroyContainer( final String containerId )
    {
        if ( !UUIDUtil.isStringAUuid( containerId ) )
        {
            return Response.status( Response.Status.BAD_REQUEST )
                           .entity( JsonUtil.toJson( ERROR_KEY, "Invalid container id" ) ).build();
        }
        UUID hostId = UUID.fromString( containerId );

        Environment environment = findEnvironmentByContainerId( hostId );

        if ( environment != null )
        {
            try
            {
                environmentManager.destroyContainer( hostId );

                return Response.ok().build();
            }
            catch ( EnvironmentManagerException e )
            {
                return Response.serverError().entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) ).build();
            }
        }

        return Response.status( Response.Status.NOT_FOUND ).build();
    }


    private Environment findEnvironmentByContainerId( UUID containerId )
    {
        for ( Environment environment : environmentManager.getEnvironments() )
        {
            for ( ContainerHost containerHost : environment.getContainerHosts() )
            {
                if ( containerHost.getId().equals( containerId ) )
                {
                    return environment;
                }
            }
        }

        return null;
    }


    @Override
    public Response addNodeGroup( final String environmentId, final String nodeGroup )
    {

        if ( !UUIDUtil.isStringAUuid( environmentId ) )
        {
            return Response.status( Response.Status.BAD_REQUEST )
                           .entity( JsonUtil.toJson( ERROR_KEY, "Invalid environment id" ) ).build();
        }

        NodeGroup ng;
        try
        {
            ng = JsonUtil.fromJson( nodeGroup, NodeGroup.class );
        }
        catch ( NullPointerException | JsonSyntaxException e )
        {
            return Response.status( Response.Status.BAD_REQUEST ).entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) )
                           .build();
        }

        try
        {
            UUID envId = UUID.fromString( environmentId );

            Set<ContainerHost> newContainers =
                    environmentManager.createAdditionalContainers( envId, ng, peerManager.getLocalPeer() );

            return Response.ok( JsonUtil.toJson( newContainers ) ).build();
        }
        catch ( EnvironmentManagerException e )
        {
            return Response.serverError().entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) ).build();
        }
    }


    @Override
    public Response getContainerState( @QueryParam( "containerId" ) final String containerId )
    {
        if ( !UUIDUtil.isStringAUuid( containerId ) )
        {
            return Response.status( Response.Status.BAD_REQUEST )
                           .entity( JsonUtil.toJson( ERROR_KEY, "Invalid container id" ) ).build();
        }
        UUID hostId = UUID.fromString( containerId );

        Environment environment = findEnvironmentByContainerId( hostId );

        if ( environment != null )
        {
            ContainerHost containerHost = environment.getContainerHostById( hostId );

            if ( containerHost != null )
            {
                try
                {
                    return Response.ok().entity( JsonUtil.toJson( "STATE", containerHost.getState() ) ).build();
                }
                catch ( PeerException e )
                {
                    return Response.serverError().entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) ).build();
                }
            }
        }

        return Response.status( Response.Status.NOT_FOUND ).build();
    }


    private Set<ContainerJson> convertContainersToContainerJson( Set<ContainerHost> containerHosts )
    {
        Set<ContainerJson> jsonSet = Sets.newHashSet();
        for ( ContainerHost containerHost : containerHosts )
        {
            ContainerHostState state = ContainerHostState.UNKNOWN;

            try
            {
                state = containerHost.getState();
            }
            catch ( PeerException e )
            {
                LOG.error( "Failed to obtain container state", e );
            }

            jsonSet.add( new ContainerJson( containerHost.getId(), UUID.fromString( containerHost.getEnvironmentId() ),
                    containerHost.getHostname(), state, containerHost.getIpByInterfaceName( "eth0" ),
                    containerHost.getTemplateName() ) );
        }
        return jsonSet;
    }
}