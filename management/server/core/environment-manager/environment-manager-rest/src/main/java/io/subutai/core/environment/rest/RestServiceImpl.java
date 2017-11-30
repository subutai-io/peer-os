package io.subutai.core.environment.rest;


import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import io.subutai.common.environment.ContainerDto;
import io.subutai.common.environment.ContainerQuotaDto;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentDto;
import io.subutai.common.environment.EnvironmentModificationException;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.environment.HubEnvironment;
import io.subutai.common.environment.Node;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.security.SshEncryptionType;
import io.subutai.common.settings.Common;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.environment.api.exception.EnvironmentCreationException;
import io.subutai.hub.share.quota.ContainerQuota;


public class RestServiceImpl implements RestService
{
    private static Logger LOG = LoggerFactory.getLogger( RestServiceImpl.class );

    private EnvironmentManager environmentManager;


    public RestServiceImpl( final EnvironmentManager environmentManager )
    {
        this.environmentManager = environmentManager;
    }


    @Override
    public Response createEnvironment( final String topology ) throws EnvironmentCreationException
    {
        try
        {
            Topology theTopology = JsonUtil.fromJson( topology, Topology.class );

            //this loop sets peer id taken from placement to avoid supplying peer id for each node in JSON
            for ( Map.Entry<String, Set<Node>> entry : theTopology.getNodeGroupPlacement().entrySet() )
            {
                String peerId = entry.getKey();
                for ( Node node : entry.getValue() )
                {
                    node.setPeerId( peerId );
                    //use name as hostname to avoid supplying in JSON, also name will be later suffixed by the system
                    // during clone
                    node.setHostname( node.getName().replaceAll( "\\s+", "" ) );
                }
            }

            if ( !Strings.isNullOrEmpty( theTopology.getSshKey() ) )
            {
                theTopology.setSshKeyType( SshEncryptionType.parseTypeFromKey( theTopology.getSshKey() ) );
            }
            else
            {
                theTopology.setSshKeyType( SshEncryptionType.RSA );
            }

            theTopology.setExchangeSshKeys( true );
            theTopology.setRegisterHosts( true );

            theTopology.setId( UUID.randomUUID() );

            environmentManager.createEnvironment( theTopology, true );

            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            return Response.serverError().build();
        }
    }


    @Override
    public Response growEnvironment( final String environmentId, final Topology topology )
            throws EnvironmentModificationException, EnvironmentNotFoundException
    {
        try
        {
            environmentManager.modifyEnvironment( environmentId, topology, null, null, true );

            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            return Response.serverError().build();
        }
    }


    @Override
    public Response listEnvironments()
    {
        try
        {
            Set<Environment> environments = environmentManager.getEnvironments();

            return Response.ok( environments ).build();
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            return Response.serverError().build();
        }
    }


    @Override
    public Response getEnvironment( final String environmentId )
    {
        try
        {
            Environment environment = environmentManager.loadEnvironment( environmentId );

            String dataSource = ( environment instanceof HubEnvironment ) ? Common.HUB_ID : Common.SUBUTAI_ID;

            final Set<ContainerDto> containers = new HashSet<>();

            for ( EnvironmentContainerHost host : environment.getContainerHosts() )
            {
                ContainerDto containerDto =
                        new ContainerDto( host.getId(), environmentId, host.getHostname(), host.getIp(),
                                host.getTemplateName(), host.getContainerSize(), host.getArch().name(), host.getTags(),
                                host.getPeerId(), host.getResourceHostId().getId(), host.isLocal(), dataSource,
                                host.getState(), host.getTemplateId(), host.getContainerName(),
                                host.getResourceHostId().getId() );
                try
                {
                    ContainerQuota containerQuota = host.getQuota();
                    if ( containerQuota != null )
                    {
                        containerDto.setQuota( new ContainerQuotaDto( containerQuota ) );
                    }
                }
                catch ( Exception e )
                {
                    LOG.error( "Error getting container quota: {}", e.getMessage() );
                }

                containers.add( containerDto );
            }

            EnvironmentDto environmentDto =
                    new EnvironmentDto( environment.getId(), environment.getName(), environment.getStatus(), containers,
                            dataSource, environmentManager.getEnvironmentOwnerName( environment ) );

            return Response.ok( environmentDto ).build();
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new WebApplicationException( e.getMessage() );
        }
    }


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
}
