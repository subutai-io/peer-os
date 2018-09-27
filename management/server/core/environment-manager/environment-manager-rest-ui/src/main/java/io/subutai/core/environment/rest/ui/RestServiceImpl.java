package io.subutai.core.environment.rest.ui;


import java.io.File;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import io.subutai.common.environment.ContainerDto;
import io.subutai.common.environment.ContainerHostNotFoundException;
import io.subutai.common.environment.ContainerQuotaDto;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentCreationRef;
import io.subutai.common.environment.EnvironmentDto;
import io.subutai.common.environment.EnvironmentModificationException;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.environment.BazaarEnvironment;
import io.subutai.common.environment.Node;
import io.subutai.common.environment.PeerTemplatesDownloadProgress;
import io.subutai.common.environment.Topology;
import io.subutai.common.gson.required.RequiredDeserializer;
import io.subutai.common.metric.ResourceHostMetric;
import io.subutai.common.metric.ResourceHostMetrics;
import io.subutai.common.network.ProxyLoadBalanceStrategy;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.protocol.Template;
import io.subutai.common.settings.Common;
import io.subutai.common.util.JsonUtil;
import io.subutai.common.util.ServiceLocator;
import io.subutai.common.util.StringUtil;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.environment.api.SecureEnvironmentManager;
import io.subutai.core.environment.api.ShareDto.ShareDto;
import io.subutai.core.environment.api.exception.EnvironmentCreationException;
import io.subutai.core.environment.rest.ui.entity.ChangedContainerDto;
import io.subutai.core.environment.rest.ui.entity.NodeSchemaDto;
import io.subutai.core.environment.rest.ui.entity.PeerDto;
import io.subutai.core.environment.rest.ui.entity.ResourceHostDto;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.template.api.TemplateManager;
import io.subutai.bazaar.share.quota.ContainerQuota;
import io.subutai.bazaar.share.quota.ContainerSize;

import static io.subutai.common.util.JsonUtil.mapper;


public class RestServiceImpl implements RestService
{
    private static final Logger LOG = LoggerFactory.getLogger( RestServiceImpl.class );
    private static final String ERROR_KEY = "ERROR";
    private final EnvironmentManager environmentManager;
    private final PeerManager peerManager;
    private final TemplateManager templateManager;
    private final SecureEnvironmentManager secureEnvironmentManager;
    private Gson gson = RequiredDeserializer.createValidatingGson();
    private IdentityManager identityManager = ServiceLocator.lookup( IdentityManager.class );


    public RestServiceImpl( final EnvironmentManager environmentManager, final PeerManager peerManager,
                            final TemplateManager templateManager,
                            final SecureEnvironmentManager secureEnvironmentManager )
    {
        Preconditions.checkNotNull( environmentManager );
        Preconditions.checkNotNull( peerManager );
        Preconditions.checkNotNull( templateManager );

        this.environmentManager = environmentManager;
        this.peerManager = peerManager;
        this.templateManager = templateManager;
        this.secureEnvironmentManager = secureEnvironmentManager;
    }


    /** Templates *************************************************** */

    @Override
    public Response getVerifiedTemplate( final String templateName )
    {
        try
        {
            Template template = templateManager.getVerifiedTemplateByName( templateName );

            if ( template != null )
            {
                return Response.ok( gson.toJson( template ) ).build();
            }
            else
            {
                return Response.status( Response.Status.NOT_FOUND ).build();
            }
        }
        catch ( Exception e )
        {
            return Response.serverError().entity(
                    JsonUtil.toJson( ERROR_KEY, e.getMessage() == null ? "Internal error" : e.getMessage() ) ).build();
        }
    }


    @Override
    public Response createTemplate( final String environmentId, final String containerId, final String templateName,
                                    final String version, final boolean privateTemplate )
    {

        try
        {
            environmentManager.createTemplate( environmentId, containerId, templateName, version, privateTemplate );

            return Response.ok().build();
        }
        catch ( Exception e )
        {
            return Response.serverError().entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) ).build();
        }
    }


    /** Environments **************************************************** */


    @Override
    public Response build( final String name, final String topologyJson )
    {

        Map<String, String> envCreationRef = Maps.newHashMap();

        try
        {
            //disallow bazaar users to use this operation
            filterBazaarUser();
        }
        catch ( AccessControlException e )
        {
            return Response.status( Response.Status.FORBIDDEN ).
                    entity( JsonUtil.GSON.toJson(
                            "You don't have permission to perform this operation, please visit Bazaar to perform"
                                    + " this operation." ) ).build();
        }

        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( name ), "Invalid environment name" );
            Preconditions.checkArgument( !Strings.isNullOrEmpty( topologyJson ), "Invalid environment topology" );

            checkName( name );

            List<NodeSchemaDto> nodes = parseNodes( topologyJson );

            Topology topology = new Topology( name );

            distribute( nodes );

            for ( NodeSchemaDto dto : nodes )
            {
                topology.addNodePlacement( dto.getPeerId(),
                        new Node( dto.getName(), dto.getName(), dto.getQuota().getContainerQuota(), dto.getPeerId(),
                                dto.getHostId(), dto.getTemplateId() ) );
            }

            EnvironmentCreationRef ref = environmentManager.createEnvironment( topology, true );

            envCreationRef.put( "trackerId", ref.getTrackerId() );
            envCreationRef.put( "environmentId", ref.getEnvironmentId() );
        }
        catch ( Exception e )
        {
            if ( e.getClass() == AccessControlException.class )
            {
                LOG.error( e.getMessage() );
                return Response.status( Response.Status.FORBIDDEN ).
                        entity( JsonUtil.GSON.toJson( "You don't have permission to perform this operation" ) ).build();
            }

            return Response.serverError().entity(
                    JsonUtil.toJson( ERROR_KEY, e.getMessage() == null ? "Internal error" : e.getMessage() ) ).build();
        }

        return Response.ok( JsonUtil.toJson( envCreationRef ) ).build();
    }


    private void distribute( final List<NodeSchemaDto> nodes )
    {
        //fetch online peers with connected resource hosts
        Map<String, PeerDto> peersNHosts = getPeersNConnectedRHs();

        //filter peers that have ALL resource hosts connected
        for ( Map.Entry<String, PeerDto> peerEntry : peersNHosts.entrySet() )
        {
            String peerId = peerEntry.getKey();
            PeerDto peerDto = peerEntry.getValue();

            if ( peerDto.getRhCount() > peerDto.getResourceHosts().size() )
            {
                LOG.warn( "Peer {} has disconnected resource hosts, skipping it", peerDto.getName() );
                peersNHosts.remove( peerId );
            }
        }

        //collect all available resource hosts
        List<ResourceHostDto> resourceHosts = Lists.newArrayList();
        for ( PeerDto peerDto : peersNHosts.values() )
        {
            resourceHosts.addAll( peerDto.getResourceHosts() );
        }

        //check if we have resource hosts to use
        if ( resourceHosts.size() == 0 )
        {
            throw new IllegalStateException(
                    "Not enough connected resource hosts. All resource hosts of selected peers must be connected" );
        }

        //distribute nodes over resource hosts (round-robin)
        Iterator<ResourceHostDto> rhIterator = Iterables.cycle( resourceHosts ).iterator();
        for ( NodeSchemaDto node : nodes )
        {
            ResourceHostDto rh = rhIterator.next();
            node.setPeerId( rh.getPeerId() );
            node.setHostId( rh.getId() );
        }
    }


    private List<NodeSchemaDto> parseNodes( final String nodes ) throws java.io.IOException
    {
        TypeFactory typeFactory = mapper.getTypeFactory();
        CollectionType arrayType = typeFactory.constructCollectionType( ArrayList.class, NodeSchemaDto.class );
        return mapper.readValue( nodes, arrayType );
    }


    @Override
    public Response buildAdvanced( final String name, final String topologyJson )
    {
        Map<String, String> envCreationRef = Maps.newHashMap();
        try
        {
            //disallow bazaar users to use this operation
            filterBazaarUser();
        }
        catch ( AccessControlException e )
        {
            return Response.status( Response.Status.FORBIDDEN ).
                    entity( JsonUtil.GSON.toJson(
                            "You don't have permission to perform this operation, please visit Bazaar to perform"
                                    + " this operation." ) ).build();
        }

        try
        {
            checkName( name );

            List<NodeSchemaDto> schemaDto = parseNodes( topologyJson );

            final List<Node> nodes = getNodes( schemaDto );

            Topology topology = new Topology( name );

            topology.addAllNodePlacement( nodes );

            EnvironmentCreationRef ref = environmentManager.createEnvironment( topology, true );

            envCreationRef.put( "trackerId", ref.getTrackerId() );
            envCreationRef.put( "environmentId", ref.getEnvironmentId() );
        }
        catch ( Exception e )
        {
            if ( e.getClass() == AccessControlException.class )
            {
                LOG.error( e.getMessage() );
                return Response.status( Response.Status.FORBIDDEN ).
                        entity( JsonUtil.GSON.toJson( "You don't have permission to perform this operation" ) ).build();
            }

            return Response.serverError().entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) ).build();
        }

        return Response.ok( JsonUtil.toJson( envCreationRef ) ).build();
    }


    @Override
    public Response modify( final String environmentId, final String topologyJson, final String removedContainers,
                            final String quotaContainers )
    {
        String trackerId;
        try
        {
            //disallow bazaar users to use this operation
            filterBazaarUser();
        }
        catch ( AccessControlException e )
        {
            return Response.status( Response.Status.FORBIDDEN ).
                    entity( JsonUtil.GSON.toJson(
                            "You don't have permission to perform this operation, please visit Bazaar to perform"
                                    + " this operation." ) ).build();
        }

        try
        {
            String name = environmentManager.loadEnvironment( environmentId ).getName();

            Topology topology = new Topology( name );

            List<NodeSchemaDto> nodes = parseNodes( topologyJson );

            distribute( nodes );

            for ( NodeSchemaDto dto : nodes )
            {
                topology.addNodePlacement( dto.getPeerId(),
                        new Node( dto.getName(), dto.getName(), dto.getQuota().getContainerQuota(), dto.getPeerId(),
                                dto.getHostId(), dto.getTemplateId() ) );
            }


            Set<String> removedContainersList = JsonUtil.fromJson( removedContainers, new TypeToken<Set<String>>()
            {
            }.getType() );


            Map<String, ContainerQuota> changedContainersFiltered = getChangedContainers( quotaContainers );


            EnvironmentCreationRef ref = environmentManager
                    .modifyEnvironment( environmentId, topology, removedContainersList, changedContainersFiltered,
                            true );

            trackerId = ref.getTrackerId();
        }
        catch ( Exception e )
        {
            if ( e.getClass() == AccessControlException.class )
            {
                LOG.error( e.getMessage() );
                return Response.status( Response.Status.FORBIDDEN ).
                        entity( JsonUtil.GSON.toJson( "You don't have permission to perform this operation" ) ).build();
            }

            return Response.status( Response.Status.BAD_REQUEST ).entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) )
                           .build();
        }

        return Response.ok( JsonUtil.toJson( trackerId ) ).build();
    }


    @Override
    public Response modifyAdvanced( final String environmentId, final String topologyJson,
                                    final String removedContainers, final String quotaContainers )
    {
        String trackerId;
        try
        {
            //disallow bazaar users to use this operation
            filterBazaarUser();
        }
        catch ( AccessControlException e )
        {
            return Response.status( Response.Status.FORBIDDEN ).
                    entity( JsonUtil.GSON.toJson(
                            "You don't have permission to perform this operation, please visit Bazaar to perform"
                                    + " this operation." ) ).build();
        }

        try
        {
            String name = environmentManager.loadEnvironment( environmentId ).getName();

            List<NodeSchemaDto> schemaDto = parseNodes( topologyJson );

            Topology topology = new Topology( name );

            final List<Node> nodes = getNodes( schemaDto );

            topology.addAllNodePlacement( nodes );

            Set<String> containersToRemove = JsonUtil.fromJson( removedContainers, new TypeToken<Set<String>>()
            {
            }.getType() );

            Map<String, ContainerQuota> changedContainersFiltered = getChangedContainers( quotaContainers );

            EnvironmentCreationRef ref = environmentManager
                    .modifyEnvironment( environmentId, topology, containersToRemove, changedContainersFiltered, true );

            trackerId = ref.getTrackerId();
        }
        catch ( Exception e )
        {
            if ( e.getClass() == AccessControlException.class )
            {
                LOG.error( e.getMessage() );
                return Response.status( Response.Status.FORBIDDEN ).
                        entity( JsonUtil.GSON.toJson( "You don't have permission to perform this operation" ) ).build();
            }

            return Response.status( Response.Status.BAD_REQUEST ).entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) )
                           .build();
        }

        return Response.ok( JsonUtil.toJson( trackerId ) ).build();
    }


    private List<Node> getNodes( final List<NodeSchemaDto> schemaDto )
    {
        List<Node> result = new ArrayList<>();
        for ( NodeSchemaDto dto : schemaDto )
        {
            ContainerQuota quota = dto.getQuota().getContainerQuota();
            if ( quota.getContainerSize() != ContainerSize.CUSTOM )
            {
                quota = ContainerSize.getDefaultContainerQuota( quota.getContainerSize() );
            }
            Node node = new Node( dto.getName(), dto.getName(), quota, dto.getPeerId(), dto.getHostId(),
                    dto.getTemplateId() );
            result.add( node );
        }

        return result;
    }


    private Map<String, ContainerQuota> getChangedContainers( final String quotaContainers ) throws java.io.IOException
    {
        Map<String, ContainerQuota> changedContainersFiltered = new HashMap<>();
        TypeFactory typeFactory = mapper.getTypeFactory();
        CollectionType arrayType = typeFactory.constructCollectionType( ArrayList.class, ChangedContainerDto.class );
        List<ChangedContainerDto> changedContainers = mapper.readValue( quotaContainers, arrayType );
        for ( ChangedContainerDto cont : changedContainers )
        {
            ContainerQuotaDto containerQuotaDto = cont.getQuota();
            ContainerSize containerSize = containerQuotaDto.getContainerSize();
            ContainerQuota defaultQuota = ContainerSize.getDefaultContainerQuota( containerSize );
            if ( containerSize == ContainerSize.CUSTOM )
            {
                defaultQuota = containerQuotaDto.getContainerQuota();
            }

            changedContainersFiltered.put( cont.getHostId(), defaultQuota );
        }
        return changedContainersFiltered;
    }


    @Override
    public Response destroyEnvironment( final String environmentId )
    {
        try
        {
            environmentManager.cancelEnvironmentWorkflow( environmentId );

            environmentManager.destroyEnvironment( environmentId, false );
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

        return Response.ok().build();
    }


    @Override
    public Response getSshKeys( final String environmentId )
    {
        try
        {
            Environment environment = environmentManager.loadEnvironment( environmentId );

            return Response.ok( JsonUtil.toJson( environment.getSshKeys() ) ).build();
        }
        catch ( Exception e )
        {
            return Response.serverError().entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) ).build();
        }
    }


    @Override
    public Response addSshKey( final String environmentId, final String key )
    {
        if ( Strings.isNullOrEmpty( environmentId ) )
        {
            return Response.status( Response.Status.BAD_REQUEST )
                           .entity( JsonUtil.toJson( ERROR_KEY, "Invalid environment id" ) ).build();
        }
        else if ( Strings.isNullOrEmpty( key ) )
        {
            return Response.status( Response.Status.BAD_REQUEST )
                           .entity( JsonUtil.toJson( ERROR_KEY, "Invalid ssh key" ) ).build();
        }


        try
        {
            byte[] bytesEncoded = Base64.decodeBase64( key.getBytes() );
            environmentManager.addSshKey( environmentId, new String( bytesEncoded ).trim(), false );
        }
        catch ( Exception e )
        {
            return Response.serverError().entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) ).build();
        }

        return Response.ok().build();
    }


    /** Environments SSH keys **************************************************** */

    @Override
    public Response removeSshKey( final String environmentId, final String key )
    {

        if ( Strings.isNullOrEmpty( environmentId ) )
        {
            return Response.status( Response.Status.BAD_REQUEST )
                           .entity( JsonUtil.toJson( ERROR_KEY, "Invalid environment id" ) ).build();
        }
        else if ( Strings.isNullOrEmpty( key ) )
        {
            return Response.status( Response.Status.BAD_REQUEST )
                           .entity( JsonUtil.toJson( ERROR_KEY, "Invalid ssh key" ) ).build();
        }

        try
        {
            byte[] bytesEncoded = Base64.decodeBase64( key.getBytes() );
            environmentManager.removeSshKey( environmentId, new String( bytesEncoded ).trim(), false );
        }
        catch ( Exception e )
        {
            return Response.serverError().entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) ).build();
        }

        return Response.ok().build();
    }


    @Override
    public Response getEnvironmentDomain( final String environmentId )
    {
        try
        {
            return Response.ok( JsonUtil.toJson( environmentManager.getEnvironmentDomain( environmentId ) ) ).build();
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.BAD_REQUEST ).entity( JsonUtil.toJson( e.getMessage() ) ).build();
        }
    }


    /** Environment domains **************************************************** */

    @Override
    public Response listDomainLoadBalanceStrategies()
    {
        return Response.ok( JsonUtil.toJson( ProxyLoadBalanceStrategy.values() ) ).build();
    }


    @Override
    public Response addEnvironmentDomain( String environmentId, String hostName, String strategyJson, Attachment attr )
    {
        try
        {
            String path = null;

            ProxyLoadBalanceStrategy strategy = JsonUtil.fromJson( strategyJson, ProxyLoadBalanceStrategy.class );

            try
            {
                // will throw exception if no attachment
                attr.getDataHandler().getContent();

                String certPath = System.getProperty( "java.io.tmpdir" ) + "/" + environmentId;

                File file = new File( certPath );

                if ( !file.createNewFile() )
                {
                    LOG.warn( "Domain ssl cert exists, overwriting..." );
                }

                attr.transferTo( file );

                // prefix path to enable agent auto-prepend full path from RH to cert location
                path = Common.MANAGEMENT_HOSTNAME + ":" + certPath;
            }
            catch ( Exception e )
            {
                // path
            }

            environmentManager.assignEnvironmentDomain( environmentId, hostName, strategy, path );
        }
        catch ( Exception e )
        {
            return Response.serverError().entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) ).build();
        }

        return Response.ok().build();
    }


    @Override
    public Response removeEnvironmentDomain( String environmentId )
    {
        try
        {
            environmentManager.removeEnvironmentDomain( environmentId );
        }
        catch ( Exception e )
        {
            return Response.serverError().entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) ).build();
        }

        return Response.ok().build();
    }


    @Override
    public Response getContainerDomainNPort( final String environmentId, final String containerId )
    {
        try
        {
            if ( environmentManager.getEnvironmentDomain( environmentId ) == null )
            {
                return Response.serverError()
                               .entity( JsonUtil.toJson( "You must first register domain for environment" ) ).build();
            }

            Map<String, String> result = new HashMap<>();

            Environment environment = environmentManager.loadEnvironment( environmentId );

            EnvironmentContainerHost containerHost = environment.getContainerHostById( containerId );

            result.put( "status",
                    String.valueOf( environmentManager.isContainerInEnvironmentDomain( containerId, environmentId ) ) );

            result.put( "port", String.valueOf( containerHost.getDomainPort() ) );

            return Response.ok( JsonUtil.toJson( result ) ).build();
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.BAD_REQUEST ).entity( JsonUtil.toJson( e.getMessage() ) ).build();
        }
    }


    @Override
    public Response setContainerDomainNPort( final String environmentId, final String containerId, final Boolean state,
                                             final int port )
    {
        try
        {
            if ( !state )
            {
                environmentManager.removeContainerFromEnvironmentDomain( containerId, environmentId );
            }
            else
            {
                environmentManager.addContainerToEnvironmentDomain( containerId, environmentId, port );
            }
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.BAD_REQUEST ).entity( JsonUtil.toJson( e.getMessage() ) ).build();
        }

        return Response.ok().build();
    }


    @Override
    public Response setContainerName( String environmentId, String containerId, String name )
    {
        try
        {
            Environment environment = findEnvironmentByContainerId( containerId );

            Preconditions.checkNotNull( environment, "Environment not found" );

            ContainerHost containerHost = environment.getContainerHostById( containerId );

            environmentManager.changeContainerHostname( containerHost.getContainerId(), name, false );
        }
        catch ( Exception e )
        {
            return Response.serverError().entity( e.getMessage() ).build();
        }

        return Response.ok().build();
    }


    /** Containers **************************************************** */

    @Override
    public Response destroyContainer( final String containerId )
    {
        if ( Strings.isNullOrEmpty( containerId ) )
        {
            return Response.status( Response.Status.BAD_REQUEST )
                           .entity( JsonUtil.toJson( ERROR_KEY, "Invalid container id" ) ).build();
        }

        Environment environment = findEnvironmentByContainerId( containerId );

        if ( environment != null )
        {
            try
            {
                ContainerHost containerHost = environment.getContainerHostById( containerId );

                environmentManager.destroyContainer( environment.getId(), containerHost.getId(), false );

                return Response.ok().build();
            }
            catch ( ContainerHostNotFoundException | EnvironmentNotFoundException | EnvironmentModificationException e )
            {
                LOG.error( "Error destroying container #destroyContainer", e );
                return Response.serverError().entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) ).build();
            }
        }

        return Response.status( Response.Status.NOT_FOUND )
                       .entity( JsonUtil.toJson( ERROR_KEY, "Container not found" ) ).build();
    }


    @Override
    public Response getContainerState( final String containerId )
    {
        if ( Strings.isNullOrEmpty( containerId ) )
        {
            return Response.status( Response.Status.BAD_REQUEST )
                           .entity( JsonUtil.toJson( ERROR_KEY, "Invalid container id" ) ).build();
        }


        Environment environment = findEnvironmentByContainerId( containerId );

        if ( environment != null )
        {
            try
            {
                ContainerHost containerHost = environment.getContainerHostById( containerId );

                return Response.ok().entity( JsonUtil.toJson( "STATE", containerHost.getState() ) ).build();
            }
            catch ( ContainerHostNotFoundException e )
            {
                LOG.error( "Error getting container state", e );
                return Response.serverError().entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) ).build();
            }
        }

        return Response.status( Response.Status.NOT_FOUND ).build();
    }


    @Override
    public Response startContainer( final String containerId )
    {
        try
        {
            //disallow bazaar users to use this operation
            filterBazaarUser();
        }
        catch ( AccessControlException e )
        {
            return Response.status( Response.Status.FORBIDDEN ).
                    entity( JsonUtil.GSON.toJson(
                            "You don't have permission to perform this operation, please visit Bazaar to perform"
                                    + " this operation." ) ).build();
        }

        if ( Strings.isNullOrEmpty( containerId ) )
        {
            return Response.status( Response.Status.BAD_REQUEST )
                           .entity( JsonUtil.toJson( ERROR_KEY, "Invalid container id" ) ).build();
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
            catch ( PeerException e )
            {
                LOG.error( "Exception starting container host", e );
                return Response.serverError().entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) ).build();
            }
        }

        return Response.status( Response.Status.NOT_FOUND ).build();
    }


    @Override
    public Response stopContainer( final String containerId )
    {
        if ( Strings.isNullOrEmpty( containerId ) )
        {
            return Response.status( Response.Status.BAD_REQUEST )
                           .entity( JsonUtil.toJson( ERROR_KEY, "Invalid container id" ) ).build();
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
            catch ( PeerException e )
            {
                LOG.error( "Exception stopping container host", e );
                return Response.serverError().entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) ).build();
            }
        }

        return Response.status( Response.Status.NOT_FOUND ).build();
    }


    /** Container types **************************************************** */
    @Override
    public Response listContainerTypes()
    {
        return Response.ok().entity( gson.toJson( ContainerSize.values() ) ).build();
    }


    @Override
    public Response listContainerTypesInfo()
    {
        try
        {
            return Response.ok().entity( gson.toJson( ContainerSize.getContainerSizesDescription() ) ).build();
        }
        catch ( Exception e )
        {
            return Response.serverError().entity( gson.toJson( e.getMessage() ) ).build();
        }
    }


    private CompletionService<Boolean> getCompletionService( Executor executor )
    {
        return new ExecutorCompletionService<>( executor );
    }


    /** Peers **************************************************** */
    @Override
    public Response getPeers()
    {
        return Response.ok().entity( JsonUtil.toJson( getPeersNConnectedRHs() ) ).build();
    }


    private Map<String, PeerDto> getPeersNConnectedRHs()
    {
        List<Peer> peers = peerManager.getPeers();

        ExecutorService taskExecutor =
                Executors.newFixedThreadPool( Math.min( Common.MAX_EXECUTOR_SIZE, peers.size() ) );

        CompletionService<Boolean> taskCompletionService = getCompletionService( taskExecutor );

        Map<String, PeerDto> peerHostMap = Maps.newHashMap();

        try
        {
            for ( Peer peer : peers )
            {
                taskCompletionService.submit( () -> {
                    boolean isOnline = peer.isOnline();
                    PeerDto peerDto = new PeerDto( peer.getId(), peer.getName(), isOnline, peer.isLocal() );
                    if ( isOnline )
                    {
                        ResourceHostMetrics metrics = peer.getResourceHostMetrics();
                        Collection<ResourceHostMetric> collection = metrics.getResources();
                        peerDto.setRhCount( metrics.getResourceHostCount() );
                        for ( ResourceHostMetric metric : collection )
                        {
                            peerDto.addResourceHostDto(
                                    new ResourceHostDto( metric.getHostInfo().getId(), metric.getHostName(),
                                            metric.getCpuModel(), metric.getUsedCpu().toString(),
                                            metric.getTotalRam().toString(), metric.getAvailableRam().toString(),
                                            metric.getTotalSpace().toString(), metric.getAvailableSpace().toString(),
                                            metric.isManagement(), metric.getPeerId() ) );
                        }
                    }

                    peerHostMap.put( peer.getId(), peerDto );
                    return true;
                } );
            }

            taskExecutor.shutdown();

            for ( int i = 0; i < peers.size(); i++ )
            {
                try
                {
                    Future<Boolean> future = taskCompletionService.take();
                    future.get();
                }
                catch ( ExecutionException | InterruptedException e )
                {
                    //ignored
                }
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Resource hosts are empty", e );
        }

        return peerHostMap;
    }


    @Override
    public Response getResourceHosts()
    {
        List<ResourceHostDto> resourceHostDtos = Lists.newArrayList();
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();

            for ( ResourceHost resourceHost : localPeer.getResourceHosts() )
            {
                resourceHostDtos.add( new ResourceHostDto( resourceHost.getId(), resourceHost.getHostname(),
                        resourceHost.getInstanceType(), resourceHost.isManagementHost(), resourceHost.getArch() ) );
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Resource hosts are empty", e );
        }

        return Response.ok().entity( JsonUtil.toJson( resourceHostDtos ) ).build();
    }


    /** Tags **************************************************** */

    @Override
    public Response addTags( final String environmentId, final String containerId, final String tagsJson )
    {
        try
        {
            Environment environment = environmentManager.loadEnvironment( environmentId );

            EnvironmentContainerHost containerHost = environment.getContainerHostById( containerId );

            Set<String> tags = JsonUtil.fromJson( tagsJson, new TypeToken<Set<String>>()
            {
            }.getType() );

            for ( String tag : tags )
            {
                containerHost = containerHost.addTag( tag );
            }
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.BAD_REQUEST ).entity( JsonUtil.toJson( e ) ).build();
        }

        return Response.ok().build();
    }


    @Override
    public Response removeTag( final String environmentId, final String containerId, final String tag )
    {
        try
        {
            Environment environment = environmentManager.loadEnvironment( environmentId );
            environment.getContainerHostById( containerId ).removeTag( tag );
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.BAD_REQUEST ).entity( JsonUtil.toJson( e ) ).build();
        }

        return Response.ok().build();
    }


    /** Additional *************************************** */


    @Override
    public Response setupContainerSsh( final String environmentId, final String containerId )
    {
        try
        {

            return Response.ok( JsonUtil
                    .toJson( environmentManager.setupSshTunnelForContainer( containerId, environmentId ) ) ).build();
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.BAD_REQUEST ).entity( e ).build();
        }
    }


    @Override
    public Response getSharedUsers( final String objectId )
    {
        try
        {
            List<ShareDto> sharedUsers = secureEnvironmentManager.getSharedUsers( objectId );
            return Response.ok( JsonUtil.toJson( sharedUsers ) ).build();
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.getMessage() ).build();
        }
    }


    @Override
    public Response share( final String users, final String environmentId )
    {
        ShareDto[] shareDto = gson.fromJson( users, ShareDto[].class );

        secureEnvironmentManager.shareEnvironment( shareDto, environmentId );

        return Response.ok().build();
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
    public Response getDownloadProgress( String environmentId )
    {
        try
        {
            List<Peer> peers = Lists.newArrayList( environmentManager.loadEnvironment( environmentId ).getPeers() );

            List<PeerTemplatesDownloadProgress> result = peers.stream().map( p -> {
                try
                {
                    return p.getTemplateDownloadProgress( new EnvironmentId( environmentId ) );
                }
                catch ( Exception e )
                {
                    return new PeerTemplatesDownloadProgress( "NONE" );
                }
            } ).sorted( Comparator.comparing( PeerTemplatesDownloadProgress::getPeerId ) )
                                                              .collect( Collectors.toList() );

            if ( result.stream().filter( s -> !s.getTemplatesDownloadProgresses().isEmpty() ).count() == 0 )
            {
                return Response.ok().build();
            }

            return Response.ok( JsonUtil.toJson( result ) ).build();
        }

        catch ( Exception e )
        {
            return Response.serverError().entity( e.getMessage() ).build();
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
    public Response listTenantEnvironments()
    {
        Set<EnvironmentDto> tenantEnvs = environmentManager.getTenantEnvironments();

        return Response.ok( JsonUtil.toJson( removeXss( tenantEnvs ) ) ).build();
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


    /** AUX **************************************************** */

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


    private void checkName( final String name ) throws EnvironmentCreationException
    {
        if ( environmentManager.getEnvironments().stream().filter( e -> e.getName().equalsIgnoreCase( name.trim() ) )
                               .count() > 0 )
        {
            throw new EnvironmentCreationException( "Duplicated environment name" );
        }

        if ( name.trim().length() > 50 )
        {
            throw new EnvironmentCreationException( "Environment name is too long, it should be 50 chars max" );
        }
    }


    /**
     * Filter if active user isbazaar user.
     */
    private void filterBazaarUser() throws AccessControlException
    {
        User user = identityManager.getActiveUser();
        if ( user.isBazaarUser() )
        {
            throw new AccessControlException( "You don't have permission to perform this operation" );
        }
    }
}