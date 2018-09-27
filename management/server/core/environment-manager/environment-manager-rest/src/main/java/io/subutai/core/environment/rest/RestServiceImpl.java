package io.subutai.core.environment.rest;


import java.util.Map;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.subutai.common.environment.ContainerDto;
import io.subutai.common.environment.ContainerQuotaDto;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentCreationRef;
import io.subutai.common.environment.EnvironmentDto;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.environment.BazaarEnvironment;
import io.subutai.common.environment.Node;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.protocol.Template;
import io.subutai.common.security.SshEncryptionType;
import io.subutai.common.settings.Common;
import io.subutai.common.util.CollectionUtil;
import io.subutai.common.util.JsonUtil;
import io.subutai.common.util.ServiceLocator;
import io.subutai.common.util.StringUtil;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.template.api.TemplateManager;
import io.subutai.bazaar.share.quota.ContainerQuota;
import io.subutai.bazaar.share.quota.ContainerSize;


public class RestServiceImpl implements RestService
{
    private static Logger LOG = LoggerFactory.getLogger( RestServiceImpl.class );
    private static final String ERROR_KEY = "ERROR";

    private EnvironmentManager environmentManager;


    public RestServiceImpl( final EnvironmentManager environmentManager )
    {
        this.environmentManager = environmentManager;
    }


    private Topology prepare2( String request, boolean create ) throws HostNotFoundException
    {
        EnvironmentCreationDto environmentCreationDto = JsonUtil.fromJson( request, EnvironmentCreationDto.class );

        Preconditions.checkNotNull( environmentCreationDto, "Invalid request" );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( environmentCreationDto.getNodes() ),
                "No containers provided" );
        Preconditions
                .checkArgument( !( create && Strings.isNullOrEmpty( environmentCreationDto.getEnvironmentName() ) ),
                        "Invalid environment name" );

        TemplateManager templateManager = ServiceLocator.lookup( TemplateManager.class );
        LocalPeer localPeer = ServiceLocator.lookup( LocalPeer.class );

        Topology topology = new Topology( environmentCreationDto.getEnvironmentName() );

        for ( NodeDto node : environmentCreationDto.getNodes() )
        {
            Preconditions
                    .checkArgument( !Strings.isNullOrEmpty( node.getHostname() ), "No container hostname provided" );
            Preconditions.checkArgument(
                    !Strings.isNullOrEmpty( node.getTemplateId() ) || !Strings.isNullOrEmpty( node.getTemplateName() ),
                    "No template provided" );

            String peerId = Strings.isNullOrEmpty( node.getPeerId() ) ? localPeer.getId() : node.getPeerId();
            String rhId = Strings.isNullOrEmpty( node.getResourceHostId() ) ? localPeer.getManagementHost().getId() :
                          node.getResourceHostId();
            String templateId = node.getTemplateId();

            if ( Strings.isNullOrEmpty( templateId ) )
            {
                Template template = templateManager.getVerifiedTemplateByName( node.getTemplateName() );

                Preconditions.checkNotNull( template,
                        String.format( "Verified template not found by name %s", node.getTemplateName() ) );

                templateId = template.getId();
            }

            Preconditions.checkArgument( !Strings.isNullOrEmpty( node.getResourceHostId() ) || (
                    Strings.isNullOrEmpty( node.getResourceHostId() ) && StringUtils
                            .equals( peerId, localPeer.getId() ) ), "Invalid host for container provided" );

            ContainerQuota quota = new ContainerQuota( node.getSize() == null ? ContainerSize.TINY : node.getSize() );

            topology.addNodePlacement( peerId,
                    new Node( node.getHostname(), node.getHostname(), quota, peerId, rhId, templateId ) );
        }

        if ( !Strings.isNullOrEmpty( environmentCreationDto.getSshKey() ) )
        {
            topology.setSshKey( environmentCreationDto.getSshKey() );
            topology.setSshKeyType( SshEncryptionType.parseTypeFromKey( environmentCreationDto.getSshKey() ) );
        }

        topology.setExchangeSshKeys( true );
        topology.setRegisterHosts( true );

        return topology;
    }


    @Override
    public Response createEnvironment( final String request )
    {
        try
        {
            Topology topology = prepare2( request, true );

            EnvironmentCreationRef ref = environmentManager.createEnvironment( topology, true );

            Map<String, String> envCreationRef = Maps.newHashMap();
            envCreationRef.put( "trackerId", ref.getTrackerId() );
            envCreationRef.put( "environmentId", ref.getEnvironmentId() );

            return Response.accepted( JsonUtil.toJson( envCreationRef ) ).build();
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            return Response.serverError().entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) ).build();
        }
    }


    @Override
    public Response growEnvironment( final String environmentId, final String topology )
    {
        try
        {
            Topology theTopology = prepare2( topology, false );

            environmentManager.modifyEnvironment( environmentId, theTopology, null, null, true );

            return Response.accepted().build();
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            return Response.serverError().entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) ).build();
        }
    }


    @Override
    public Response listEnvironments()
    {
        Set<Environment> environments = environmentManager.getEnvironments();
        Set<EnvironmentDto> environmentDtos = Sets.newHashSet();


        for ( Environment environment : environments )
        {
            try
            {
                String dataSource = ( environment instanceof BazaarEnvironment
                        || String.format( "Of %s", Common.BAZAAR_ID )
                                 .equals(
                                                                                             environment.getName() ) ) ?
                                    Common.BAZAAR_ID : Common.SUBUTAI_ID;

                EnvironmentDto environmentDto =
                        new EnvironmentDto( environment.getId(), environment.getName(), environment.getStatus(),
                                convertContainersToContainerJson( environment.getContainerHosts(), dataSource ),
                                dataSource, environmentManager.getEnvironmentOwnerName( environment ) );

                environmentDtos.add( environmentDto );
            }
            catch ( Exception e )
            {
                LOG.error( "Error JSON-ifying environment {}: {}", environment.getId(), e.getMessage() );
            }
        }

        return Response.ok( JsonUtil.toJson( removeXss( environmentDtos ) ) ).build();
    }


    @Override
    public Response destroyEnvironment( final String environmentId )
    {
        try
        {
            environmentManager.cancelEnvironmentWorkflow( environmentId );

            environmentManager.destroyEnvironment( environmentId, true );
        }
        catch ( EnvironmentNotFoundException e )
        {
            return Response.serverError().entity( JsonUtil.toJson( ERROR_KEY, "Environment is already destroyed" ) )
                           .build();
        }
        catch ( Exception e )
        {
            return Response.serverError().entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) ).build();
        }

        return Response.accepted().build();
    }


    @Override
    public Response destroyContainer( final String containerId )
    {
        Environment environment = findEnvironmentByContainerId( containerId );

        if ( environment != null )
        {
            try
            {
                ContainerHost containerHost = environment.getContainerHostById( containerId );

                environmentManager.destroyContainer( environment.getId(), containerHost.getId(), true );

                return Response.accepted().build();
            }
            catch ( Exception e )
            {
                LOG.error( "Error destroying container #destroyContainer", e );
                return Response.serverError().entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) ).build();
            }
        }

        return Response.status( Response.Status.NOT_FOUND )
                       .entity( JsonUtil.toJson( ERROR_KEY, "Container not found" ) ).build();
    }


    //--------


    //this rest endpoint is obsolete and can be removed
    @Override
    public Response placeEnvironmentInfoByContainerIp( String containerIp )
    {
        try
        {
            environmentManager.placeEnvironmentInfoByContainerIp( containerIp );

            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new WebApplicationException( e.getMessage() );
        }
    }


    /** AUX **************************************************** */

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


    private Set<ContainerDto> convertContainersToContainerJson( Set<EnvironmentContainerHost> containerHosts,
                                                                String datasource )
    {
        Set<ContainerDto> containerDtos = Sets.newHashSet();

        for ( EnvironmentContainerHost containerHost : containerHosts )
        {
            ContainerDto containerDto =
                    new ContainerDto( containerHost.getId(), containerHost.getEnvironmentId().getId(),
                            containerHost.getHostname(), containerHost.getIp(), containerHost.getTemplateName(),
                            containerHost.getContainerSize(), containerHost.getArch().toString(),
                            containerHost.getTags(), containerHost.getPeerId(),
                            containerHost.getResourceHostId().getId(), containerHost.isLocal(), datasource,
                            containerHost.getState(), containerHost.getTemplateId(), containerHost.getContainerName(),
                            containerHost.getResourceHostId().getId() );

            try
            {
                ContainerQuota containerQuota = containerHost.getQuota();
                if ( containerQuota != null )
                {
                    containerDto.setQuota( new ContainerQuotaDto( containerQuota ) );
                }
            }
            catch ( Exception e )
            {
                LOG.error( "Error getting container quota: {}", e.getMessage() );
            }

            containerDtos.add( containerDto );
        }

        return containerDtos;
    }


    private Set<EnvironmentDto> removeXss( final Set<EnvironmentDto> environmentDtos )
    {
        for ( EnvironmentDto environmentDto : environmentDtos )
        {
            environmentDto.setName( StringUtil.removeHtml( environmentDto.getName() ) );

            for ( ContainerDto containerDto : environmentDto.getContainers() )
            {
                containerDto.setContainerName( StringUtil.removeHtml( containerDto.getContainerName() ) );
                containerDto.setHostname( StringUtil.removeHtml( containerDto.getHostname() ) );
                containerDto.setTemplateName( StringUtil.removeHtml( containerDto.getTemplateName() ) );
            }
        }

        return environmentDtos;
    }
}
