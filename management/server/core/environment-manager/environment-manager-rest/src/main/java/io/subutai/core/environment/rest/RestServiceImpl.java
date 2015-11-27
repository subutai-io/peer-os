package io.subutai.core.environment.rest;


import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import io.subutai.common.environment.Blueprint;
import io.subutai.common.environment.ContainerDistributionType;
import io.subutai.common.environment.ContainerHostNotFoundException;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentModificationException;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.environment.NodeGroup;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.network.DomainLoadBalanceStrategy;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.PeerException;
import io.subutai.common.settings.Common;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.environment.api.exception.EnvironmentCreationException;
import io.subutai.core.environment.api.exception.EnvironmentDestructionException;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.registry.api.TemplateRegistry;


public class RestServiceImpl implements RestService
{
    private static final Logger LOG = LoggerFactory.getLogger( RestServiceImpl.class );
    private static final String ERROR_KEY = "ERROR";

    private final EnvironmentManager environmentManager;
    private final PeerManager peerManager;
    private final TemplateRegistry templateRegistry;


    public RestServiceImpl( final EnvironmentManager environmentManager, final PeerManager peerManager,
                            final TemplateRegistry templateRegistry )
    {
        Preconditions.checkNotNull( environmentManager );
        Preconditions.checkNotNull( peerManager );
        Preconditions.checkNotNull( templateRegistry );

        this.environmentManager = environmentManager;
        this.peerManager = peerManager;
        this.templateRegistry = templateRegistry;
    }


    @Override
    public Response createEnvironment( final Blueprint blueprint )
    {
        //validate params
        try
        {
            Preconditions.checkNotNull( blueprint );
            Preconditions.checkNotNull( blueprint.getNodeGroups() );
            Preconditions.checkArgument( blueprint.getNodeGroups().size() > 0, "Nodegroup size must be great than 0" );
            Preconditions.checkArgument( !Strings.isNullOrEmpty( blueprint.getName() ), "Invalid blueprint name" );
            //            Preconditions.checkArgument( !Strings.isNullOrEmpty( blueprint.getCidr() ), "Invalid subnet
            // cidr" );
            checkBlueprint( blueprint );
        }
        catch ( Exception e )
        {
            LOG.error( "Error validating parameters #createEnvironment", e );
            Response response =
                    Response.status( Response.Status.BAD_REQUEST ).entity( new ErrorDto( e.getMessage() ) ).build();

            return response;
        }

        try
        {
            Environment environment = environmentManager.createEnvironment( blueprint, false );

            Response response = Response.ok( environment.getEnvironmentId() ).build();

            return response;
        }
        catch ( EnvironmentCreationException e )
        {
            LOG.error( "Error creating environment #createEnvironment", e );
            Response response = Response.serverError().entity( new ErrorDto( e.getMessage() ) ).build();
            throw new WebApplicationException( response );
        }
    }


    private void checkBlueprint( Blueprint blueprint ) throws EnvironmentCreationException
    {
        for ( NodeGroup nodegroup : blueprint.getNodeGroups() )
        {
            checkNodeGroup( nodegroup );
        }
    }


    private void checkNodeGroup( final NodeGroup nodeGroup ) throws EnvironmentCreationException
    {
        String peerId = nodeGroup.getPeerId();
        if ( peerId == null )
        {
            throw new EnvironmentCreationException( "Invalid peer id" );
        }
        else if ( peerManager.getPeer( peerId ) == null )
        {
            throw new EnvironmentCreationException( String.format( "Peer %s not found", peerId ) );
        }
        if ( Strings.isNullOrEmpty( nodeGroup.getName() ) )
        {
            throw new EnvironmentCreationException( "Invalid node group name" );
        }
        else if ( nodeGroup.getNumberOfContainers() <= 0 )
        {
            throw new EnvironmentCreationException( "Invalid number of containers" );
        }
        else if ( Strings.isNullOrEmpty( nodeGroup.getTemplateName() ) )
        {
            throw new EnvironmentCreationException( "Invalid templateName" );
        }
        else if ( templateRegistry.getTemplate( nodeGroup.getTemplateName() ) == null )
        {
            throw new EnvironmentCreationException(
                    String.format( "Template %s does not exist", nodeGroup.getTemplateName() ) );
        }
        else if ( nodeGroup.getContainerDistributionType() == ContainerDistributionType.AUTO
                && nodeGroup.getContainerPlacementStrategy() == null )
        {
            throw new EnvironmentCreationException( "Invalid node container placement strategy" );
        }
    }


    @Deprecated
    private void checkNodeGroup( final Map.Entry<String, Set<NodeGroup>> placementKey )
            throws EnvironmentCreationException
    {
        String peerId = placementKey.getKey();
        Set<NodeGroup> nodeGroups = placementKey.getValue();
        if ( peerId == null )
        {
            throw new EnvironmentCreationException( "Invalid peer id" );
        }
        else if ( peerManager.getPeer( peerId ) == null )
        {
            throw new EnvironmentCreationException( String.format( "Peer %s not found", peerId ) );
        }
        for ( NodeGroup nodeGroup : nodeGroups )
        {
            if ( Strings.isNullOrEmpty( nodeGroup.getName() ) )
            {
                throw new EnvironmentCreationException( "Invalid node group name" );
            }
            else if ( nodeGroup.getNumberOfContainers() <= 0 )
            {
                throw new EnvironmentCreationException( "Invalid number of containers" );
            }
            else if ( Strings.isNullOrEmpty( nodeGroup.getTemplateName() ) )
            {
                throw new EnvironmentCreationException( "Invalid templateName" );
            }
            else if ( templateRegistry.getTemplate( nodeGroup.getTemplateName() ) == null )
            {
                throw new EnvironmentCreationException(
                        String.format( "Template %s does not exist", nodeGroup.getTemplateName() ) );
            }
            else if ( nodeGroup.getContainerPlacementStrategy() == null )
            {
                throw new EnvironmentCreationException( "Invalid node container placement strategy" );
            }
        }
    }


    @Override
    public Response getContainerEnvironmentId( final String containerId )
    {
        if ( Strings.isNullOrEmpty( containerId ) )
        {
            return Response.status( Response.Status.BAD_REQUEST ).entity( new ErrorDto( "Invalid container id" ) )
                           .build();
        }


        Environment environment = findEnvironmentByContainerId( containerId );

        if ( environment != null )
        {
            return Response.ok( environment.getId() ).build();
        }

        return Response.status( Response.Status.NOT_FOUND ).build();
    }


    @Override
    public Response getDefaultDomainName()
    {
        return Response.ok( environmentManager.getDefaultDomainName() ).build();
    }


    @Override
    public Response addEnvironmentDomain( String environmentId, String hostName, DomainLoadBalanceStrategy strategy,
                                          Attachment attr )
    {
        if ( attr == null )
        {
            return Response.status( Response.Status.BAD_REQUEST ).build();
        }

        try
        {
            File file = new File( System.getProperty( "java.io.tmpdir" ) + "/" + environmentId );
            file.createNewFile();
            attr.transferTo( file );
        }
        catch ( IOException e )
        {
            return Response.serverError().entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) ).build();
        }

        try
        {
            environmentManager.assignEnvironmentDomain( environmentId, hostName, strategy,
                    System.getProperty( "java.io.tmpdir" ) + "/" + environmentId );
        }
        catch ( EnvironmentModificationException e )
        {
            return Response.serverError().entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) ).build();
        }
        catch ( EnvironmentNotFoundException e )
        {
            return Response.serverError().entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) ).build();
        }

        return Response.ok().build();
    }


    @Override
    public Response listEnvironments()
    {

        Set<Environment> environments = environmentManager.getEnvironments();

        Set<EnvironmentDto> environmentDtos = Sets.newHashSet();

        for ( Environment environment : environments )
        {
            environmentDtos
                    .add( new EnvironmentDto( environment.getId(), environment.getName(), environment.getStatus(),
                            convertContainersToContainerJson( environment.getContainerHosts() ) ) );
        }

        return Response.ok( environmentDtos ).build();
    }


    @Override
    public Response viewEnvironment( final String environmentId )
    {
        if ( Strings.isNullOrEmpty( environmentId ) )
        {
            Response response =
                    Response.status( Response.Status.BAD_REQUEST ).entity( new ErrorDto( "Invalid environment id" ) )
                            .build();
            return response;
        }


        try
        {
            Environment environment = environmentManager.loadEnvironment( environmentId );

            Response response = Response.ok(
                    new EnvironmentDto( environment.getId(), environment.getName(), environment.getStatus(),
                            convertContainersToContainerJson( environment.getContainerHosts() ) ) ).build();
            return response;
        }
        catch ( EnvironmentNotFoundException e )
        {
            LOG.warn( "Error getting environment by id", environmentId );
            Response response = Response.status( Response.Status.NOT_FOUND ).build();
            return response;
        }
    }


    @Override
    public Response destroyEnvironment( final String environmentId )
    {
        if ( Strings.isNullOrEmpty( environmentId ) )
        {
            return Response.status( Response.Status.BAD_REQUEST ).entity( new ErrorDto( "Invalid environment id" ) )
                           .build();
        }


        try
        {
            environmentManager.destroyEnvironment( environmentId, false, false );

            return Response.ok().build();
        }
        catch ( EnvironmentNotFoundException e )
        {
            LOG.warn( "Error getting environment by id {}", environmentId );
            return Response.status( Response.Status.NOT_FOUND ).build();
        }
        catch ( EnvironmentDestructionException e )
        {
            LOG.error( "Error destroying environment #destroyEnvironment", e );
            return Response.serverError().entity( new ErrorDto( e.getMessage() ) ).build();
        }
    }


    @Override
    public Response destroyContainer( final String containerId )
    {
        if ( Strings.isNullOrEmpty( containerId ) )
        {
            return Response.status( Response.Status.BAD_REQUEST ).entity( new ErrorDto( "Invalid container id" ) )
                           .build();
        }

        Environment environment = findEnvironmentByContainerId( containerId );

        if ( environment != null )
        {
            try
            {
                ContainerHost containerHost = environment.getContainerHostById( containerId );

                environmentManager.destroyContainer( environment.getId(), containerHost.getId(), false, false );

                return Response.ok().build();
            }
            catch ( ContainerHostNotFoundException | EnvironmentNotFoundException | EnvironmentModificationException e )
            {
                LOG.error( "Error destroying container #destroyContainer", e );
                return Response.serverError().entity( new ErrorDto( e.getMessage() ) ).build();
            }
        }

        return Response.status( Response.Status.NOT_FOUND ).build();
    }


    private Environment findEnvironmentByContainerId( String containerId )
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
    public void growEnvironment( final String environmentId, final Blueprint blueprint )
    {
        //validate params
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );
            Preconditions.checkNotNull( blueprint );
            Preconditions.checkNotNull( blueprint.getNodeGroups() );
            Preconditions.checkArgument( blueprint.getNodeGroups().size() > 0, "Nodegroup size must be great than 0" );
            Preconditions.checkArgument( !Strings.isNullOrEmpty( blueprint.getName() ), "Invalid blueprint name" );
            //            Preconditions.checkArgument( !Strings.isNullOrEmpty( blueprint.getCidr() ), "Invalid subnet
            // cidr" );
            checkBlueprint( blueprint );
        }
        catch ( Exception e )
        {
            LOG.error( "Error validating parameters #growEnvironment", e );
            Response response =
                    Response.status( Response.Status.BAD_REQUEST ).entity( new ErrorDto( e.getMessage() ) ).build();

            throw new WebApplicationException( response );
        }

        try
        {
            /*Set<EnvironmentContainerHost> newContainers = */
            environmentManager.growEnvironment( environmentId, blueprint, false );

            //            return Response.ok( JsonUtil.toJson( convertContainersToContainerJson( newContainers ) ) )
            // .build();
        }
        catch ( EnvironmentNotFoundException e )
        {
            LOG.warn( "Error looking for environment by id {}", environmentId );
            Response response = Response.status( Response.Status.NOT_FOUND ).build();
            throw new WebApplicationException( response );
        }
        catch ( EnvironmentModificationException e )
        {
            LOG.error( "Error modifying environment #growEnvironment", e );
            Response response = Response.serverError().entity( new ErrorDto( e.getMessage() ) ).build();
            throw new WebApplicationException( response );
        }
    }


    @Override
    public Response getContainerState( final String containerId )
    {
        if ( Strings.isNullOrEmpty( containerId ) )
        {
            return Response.status( Response.Status.BAD_REQUEST ).entity( new ErrorDto( "Invalid container id" ) )
                           .build();
        }


        Environment environment = findEnvironmentByContainerId( containerId );

        if ( environment != null )
        {
            try
            {
                ContainerHost containerHost = environment.getContainerHostById( containerId );

                return Response.ok( new ContainerStateDto( containerHost.getStatus() ) ).build();
            }
            catch ( ContainerHostNotFoundException e )
            {
                LOG.error( "Error getting container state", e );
                return Response.serverError().entity( new ErrorDto( e.getMessage() ) ).build();
            }
        }

        return Response.status( Response.Status.NOT_FOUND ).build();
    }


    @Override
    public Response startContainer( final String containerId )
    {
        if ( Strings.isNullOrEmpty( containerId ) )
        {
            return Response.status( Response.Status.BAD_REQUEST ).entity( new ErrorDto( "Invalid container id" ) )
                           .build();
        }


        Environment environment = findEnvironmentByContainerId( containerId );

        if ( environment != null )
        {
            try
            {
                ContainerHost containerHost = environment.getContainerHostById( containerId );

                containerHost.start();

                return Response.ok().build();
            }
            catch ( ContainerHostNotFoundException | PeerException e )
            {
                LOG.error( "Exception starting container host", e );
                return Response.serverError().entity( new ErrorDto( e.getMessage() ) ).build();
            }
        }

        return Response.status( Response.Status.NOT_FOUND ).build();
    }


    @Override
    public Response stopContainer( final String containerId )
    {
        if ( Strings.isNullOrEmpty( containerId ) )
        {
            return Response.status( Response.Status.BAD_REQUEST ).entity( new ErrorDto( "Invalid container id" ) )
                           .build();
        }


        Environment environment = findEnvironmentByContainerId( containerId );

        if ( environment != null )
        {
            try
            {
                ContainerHost containerHost = environment.getContainerHostById( containerId );

                containerHost.stop();

                return Response.ok().build();
            }
            catch ( ContainerHostNotFoundException | PeerException e )
            {
                LOG.error( "Exception stopping container host", e );
                return Response.serverError().entity( new ErrorDto( e.getMessage() ) ).build();
            }
        }

        return Response.status( Response.Status.NOT_FOUND ).build();
    }


    @Override
    public Response setSshKey( final String environmentId, final String key )
    {
        if ( Strings.isNullOrEmpty( environmentId ) )
        {
            return Response.status( Response.Status.BAD_REQUEST ).entity( new ErrorDto( "Invalid environment id" ) )
                           .build();
        }
        else if ( Strings.isNullOrEmpty( key ) )
        {
            return Response.status( Response.Status.BAD_REQUEST ).entity( new ErrorDto( "Invalid ssh key" ) ).build();
        }


        try
        {
            environmentManager.setSshKey( environmentId, key, false );

            return Response.ok().build();
        }
        catch ( EnvironmentNotFoundException e )
        {
            LOG.warn( "Environment not found by id {}", environmentId );
            return Response.status( Response.Status.NOT_FOUND ).build();
        }
        catch ( EnvironmentModificationException e )
        {
            LOG.error( "Environment modification failed", e );
            return Response.serverError().entity( new ErrorDto( e.getMessage() ) ).build();
        }
    }


    @Override
    public Response removeSshKey( final String environmentId )
    {
        if ( Strings.isNullOrEmpty( environmentId ) )
        {
            return Response.status( Response.Status.BAD_REQUEST ).entity( new ErrorDto( "Invalid environment id" ) )
                           .build();
        }


        try
        {
            environmentManager.setSshKey( environmentId, null, false );

            return Response.ok().build();
        }
        catch ( EnvironmentNotFoundException e )
        {
            LOG.warn( "Exception getting environment by id {}", environmentId );
            return Response.status( Response.Status.NOT_FOUND ).build();
        }
        catch ( EnvironmentModificationException e )
        {
            LOG.error( "Error modifying environment", e );
            return Response.serverError().entity( new ErrorDto( e.getMessage() ) ).build();
        }
    }


    private Set<ContainerDto> convertContainersToContainerJson( Set<EnvironmentContainerHost> containerHosts )
    {
        Set<ContainerDto> jsonSet = Sets.newHashSet();
        for ( EnvironmentContainerHost containerHost : containerHosts )
        {
            ContainerHostState state = containerHost.getStatus();


            jsonSet.add( new ContainerDto( containerHost.getId(), containerHost.getEnvironmentId().getId(),
                    containerHost.getHostname(), containerHost.getContainerType(), state,
                    containerHost.getInterfaceByName( Common.DEFAULT_CONTAINER_INTERFACE ).getIp(),
                    containerHost.getTemplateName() ) );
        }
        return jsonSet;
    }
}