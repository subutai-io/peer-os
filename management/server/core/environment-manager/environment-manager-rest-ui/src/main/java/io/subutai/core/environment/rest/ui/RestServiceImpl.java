package io.subutai.core.environment.rest.ui;


import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import io.subutai.common.environment.ContainerHostNotFoundException;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentModificationException;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.environment.Node;
import io.subutai.common.environment.NodeSchema;
import io.subutai.common.environment.Topology;
import io.subutai.common.gson.required.RequiredDeserializer;
import io.subutai.common.host.HostInterface;
import io.subutai.common.metric.ResourceHostMetric;
import io.subutai.common.network.ProxyLoadBalanceStrategy;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ContainerSize;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.protocol.TemplateKurjun;
import io.subutai.common.quota.ContainerQuota;
import io.subutai.common.resource.PeerGroupResources;
import io.subutai.common.settings.Common;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.environment.api.SecureEnvironmentManager;
import io.subutai.core.environment.api.ShareDto.ShareDto;
import io.subutai.core.environment.rest.ui.entity.ContainerDto;
import io.subutai.core.environment.rest.ui.entity.EnvironmentDto;
import io.subutai.core.environment.rest.ui.entity.PeerDto;
import io.subutai.core.environment.rest.ui.entity.ResourceHostDto;
import io.subutai.core.kurjun.api.TemplateManager;
import io.subutai.core.lxc.quota.api.QuotaManager;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.strategy.api.ContainerPlacementStrategy;
import io.subutai.core.strategy.api.RoundRobinStrategy;
import io.subutai.core.strategy.api.StrategyManager;


public class RestServiceImpl implements RestService
{
    private static final Logger LOG = LoggerFactory.getLogger( RestServiceImpl.class );
    private static final String ERROR_KEY = "ERROR";
    private final EnvironmentManager environmentManager;
    private final PeerManager peerManager;
    private final TemplateManager templateRegistry;
    private final StrategyManager strategyManager;
    private final QuotaManager quotaManager;
    private Gson gson = RequiredDeserializer.createValidatingGson();
    private SecureEnvironmentManager secureEnvironmentManager;


    public RestServiceImpl( final EnvironmentManager environmentManager, final PeerManager peerManager,
                            final TemplateManager templateRegistry, final StrategyManager strategyManager,
                            final QuotaManager quotaManager )
    {
        Preconditions.checkNotNull( environmentManager );
        Preconditions.checkNotNull( peerManager );
        Preconditions.checkNotNull( templateRegistry );
        Preconditions.checkNotNull( strategyManager );

        this.environmentManager = environmentManager;
        this.peerManager = peerManager;
        this.templateRegistry = templateRegistry;
        this.strategyManager = strategyManager;
        this.quotaManager = quotaManager;
    }


    public void setSecureEnvironmentManager( final SecureEnvironmentManager secureEnvironmentManager )
    {
        this.secureEnvironmentManager = secureEnvironmentManager;
    }


    /** Templates *************************************************** */

    @Override
    public Response listTemplates()
    {
        // @todo check for management container should be here
        Set<TemplateKurjun> templates = templateRegistry.list().stream()
                                            .filter( n -> !n.getName().equalsIgnoreCase( Common.MANAGEMENT_HOSTNAME ) )
                                            .collect( Collectors.toSet() );

        return Response.ok().entity( gson.toJson( templates ) ).build();
    }


    /** Domain **************************************************** */

    @Override
    public Response getDefaultDomainName()
    {
        return Response.ok( environmentManager.getDefaultDomainName() ).build();
    }


    /** Environments **************************************************** */

    @Override
    public Response listEnvironments()
    {
        Set<Environment> environments = environmentManager.getEnvironments();
        Set<EnvironmentDto> environmentDtos = Sets.newHashSet();


        for ( Environment environment : environments )
        {
            try
            {
                EnvironmentDto environmentDto =
                        new EnvironmentDto( environment.getId(), environment.getName(), environment.getStatus(),
                                convertContainersToContainerJson( environment.getContainerHosts() ),
                                environment.getRelationDeclaration(), environment.getClass().getName() );

                environmentDtos.add( environmentDto );
            }
            catch ( Exception e )
            {
                LOG.error( "Error JSON-ifying environment {}: {}", environment.getId(), e.getMessage() );
            }
        }

        return Response.ok( JsonUtil.toJson( environmentDtos ) ).build();
    }


    @Override
    public Response build( final String name, final String topologyJson )
    {

        UUID eventId;

        try
        {
            checkName( name );

            ContainerPlacementStrategy placementStrategy = strategyManager.findStrategyById( RoundRobinStrategy.ID );

            List<NodeSchema> schema = JsonUtil.fromJson( topologyJson, new TypeToken<List<NodeSchema>>()
            {
            }.getType() );


            final PeerGroupResources peerGroupResources = peerManager.getPeerGroupResources();
            final Map<ContainerSize, ContainerQuota> quotas = quotaManager.getDefaultQuotas();

            Topology topology = placementStrategy.distribute( name, schema, peerGroupResources, quotas );

            eventId = environmentManager.createEnvironmentAndGetTrackerID( topology, true );
        }
        catch ( Exception e )
        {
            return Response.serverError().entity(
                    JsonUtil.toJson( ERROR_KEY, ( e.getMessage() == null ? "Internal error" : e.getMessage() ) ) )
                           .build();
        }

        return Response.ok( JsonUtil.toJson( eventId ) ).build();
    }


    @Override
    public Response buildAdvanced( final String name, final String topologyJson )
    {
        UUID eventId;

        try
        {
            checkName( name );

            List<Node> schema = JsonUtil.fromJson( topologyJson, new TypeToken<List<Node>>()
            {
            }.getType() );

            Topology topology = new Topology( name );

            schema.forEach( s -> topology.addNodePlacement( s.getPeerId(), s ) );

            eventId = environmentManager.createEnvironmentAndGetTrackerID( topology, true );
        }
        catch ( Exception e )
        {
            return Response.serverError().entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) ).build();
        }

        return Response.ok( JsonUtil.toJson( eventId ) ).build();
    }


    @Override
    public Response modify( final String environmentId, final String topologyJson, final String removedContainers )
    {
        UUID eventId;
        try
        {
            String name = environmentManager.getEnvironments().stream()
                                            .filter( e -> e.getEnvironmentId().getId().equals( environmentId ) )
                                            .findFirst().get().getName();

            ContainerPlacementStrategy placementStrategy = strategyManager.findStrategyById( RoundRobinStrategy.ID );


            List<NodeSchema> schema = JsonUtil.fromJson( topologyJson, new TypeToken<List<NodeSchema>>()
            {
            }.getType() );

            List<String> containers = JsonUtil.fromJson( removedContainers, new TypeToken<List<String>>()
            {
            }.getType() );


            Topology topology = null;
            if ( schema.size() > 0 )
            {
                final PeerGroupResources peerGroupResources = peerManager.getPeerGroupResources();
                final Map<ContainerSize, ContainerQuota> quotas = quotaManager.getDefaultQuotas();

                topology = placementStrategy.distribute( name, schema, peerGroupResources, quotas );
            }

            eventId = environmentManager.modifyEnvironmentAndGetTrackerID( environmentId, topology, containers, true );
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.BAD_REQUEST ).entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) )
                           .build();
        }

        return Response.ok( JsonUtil.toJson( eventId ) ).build();
    }


    @Override
    public Response modifyAdvanced( final String environmentId, final String topologyJson,
                                    final String removedContainers )
    {
        UUID eventId;

        try
        {
            String name = environmentManager.getEnvironments().stream()
                                            .filter( e -> e.getEnvironmentId().getId().equals( environmentId ) )
                                            .findFirst().get().getName();

            List<Node> schema = JsonUtil.fromJson( topologyJson, new TypeToken<List<Node>>()
            {
            }.getType() );
            List<String> containers = JsonUtil.fromJson( removedContainers, new TypeToken<List<String>>()
            {
            }.getType() );


            Topology topology = new Topology( name );


            schema.forEach( s -> topology.addNodePlacement( s.getPeerId(), s ) );

            eventId = environmentManager.modifyEnvironmentAndGetTrackerID( environmentId, topology, containers, true );
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.BAD_REQUEST ).entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) )
                           .build();
        }

        return Response.ok( JsonUtil.toJson( eventId ) ).build();
    }


    @Override
    public Response destroyEnvironment( final String environmentId )
    {
        try
        {
            environmentManager.cancelEnvironmentWorkflow( environmentId );

            environmentManager.destroyEnvironment( environmentId, false );
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
            environmentManager.addSshKey( environmentId, new String( bytesEncoded ), false );
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
            environmentManager.removeSshKey( environmentId, new String( bytesEncoded ), false );
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
                attr.getDataHandler().getContent();
                File file = new File( System.getProperty( "java.io.tmpdir" ) + "/" + environmentId );
                file.createNewFile();
                attr.transferTo( file );

                path = System.getProperty( "java.io.tmpdir" ) + "/" + environmentId;
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
    public Response isContainerDomain( final String environmentId, final String containerId )
    {
        try
        {
            if ( environmentManager.getEnvironmentDomain( environmentId ) == null )
            {
                return Response.serverError()
                               .entity( JsonUtil.toJson( "You must first register domain for environment" ) ).build();
            }


            return Response.ok( JsonUtil
                    .toJson( environmentManager.isContainerInEnvironmentDomain( containerId, environmentId ) ) )
                           .build();
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.BAD_REQUEST ).entity( JsonUtil.toJson( e.getMessage() ) ).build();
        }
    }


    @Override
    public Response setContainerDomain( final String environmentId, final String containerId, final Boolean state )
    {
        try
        {
            if ( !state )
            {
                environmentManager.removeContainerFromEnvironmentDomain( containerId, environmentId );
            }
            else
            {
                environmentManager.addContainerToEnvironmentDomain( containerId, environmentId );
            }
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.BAD_REQUEST ).entity( JsonUtil.toJson( e.getMessage() ) ).build();
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

        return Response.status( Response.Status.NOT_FOUND ).build();
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
            catch ( ContainerHostNotFoundException | PeerException e )
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
            catch ( ContainerHostNotFoundException | PeerException e )
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
            return Response.ok().entity( gson.toJson( ContainerSize.getConteinerSizeDescription() ) ).build();
        }
        catch ( Exception e )
        {
            return Response.serverError().entity( gson.toJson( e.getMessage() ) ).build();
        }
    }


    /** Peers strategy **************************************************** */
    @Override
    public Response listPlacementStrategies()
    {
        return Response.ok( JsonUtil.toJson( strategyManager.getPlacementStrategyTitles() ) ).build();
    }


    protected CompletionService<Boolean> getCompletionService( Executor executor )
    {
        return new ExecutorCompletionService<>( executor );
    }


    /** Peers **************************************************** */
    @Override
    public Response getPeers()
    {
        List<Peer> peers = peerManager.getPeers();

        String localId = peerManager.getLocalPeer().getId();

        ExecutorService taskExecutor = Executors.newFixedThreadPool( peers.size() );

        CompletionService<Boolean> taskCompletionService = getCompletionService( taskExecutor );

        Map<String, PeerDto> peerHostMap = Maps.newHashMap();

        try
        {
            for ( Peer peer : peers )
            {
                taskCompletionService.submit( () -> {
                    PeerDto peerDto = new PeerDto( peer.getId(), peer.getName(), peer.isOnline(),
                            peer.getId().equals( localId ) );
                    if ( peer.isOnline() )
                    {
                        Collection<ResourceHostMetric> collection = peer.getResourceHostMetrics().getResources();
                        for ( ResourceHostMetric metric : collection
                                .toArray( new ResourceHostMetric[collection.size()] ) )
                        {
                            peerDto.addResourceHostDto(
                                    new ResourceHostDto( metric.getHostInfo().getId(), metric.getCpuModel(),
                                            metric.getUsedCpu().toString(), metric.getTotalRam().toString(),
                                            metric.getAvailableRam().toString(), metric.getTotalSpace().toString(),
                                            metric.getAvailableSpace().toString() ) );
                        }
                    }

                    peerHostMap.put( peer.getId(), peerDto );
                    return true;
                } );
            }

            taskExecutor.shutdown();

            for ( Peer ignored : peers )
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


        return Response.ok().entity( JsonUtil.toJson( peerHostMap ) ).build();
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
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
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


    /** AUX **************************************************** */

    private Set<ContainerDto> convertContainersToContainerJson( Set<EnvironmentContainerHost> containerHosts )
    {
        Set<ContainerDto> containerDtos = Sets.newHashSet();

        for ( EnvironmentContainerHost containerHost : containerHosts )
        {
            try
            {
                HostInterface iface = containerHost.getInterfaceByName( Common.DEFAULT_CONTAINER_INTERFACE );

                containerDtos.add( new ContainerDto( containerHost.getId(), containerHost.getEnvironmentId().getId(),
                        containerHost.getHostname(), iface.getIp(), containerHost.getTemplateName(),
                        containerHost.getContainerSize(), containerHost.getArch().toString(), containerHost.getTags(),
                        containerHost.getPeerId(), containerHost.getResourceHostId().getId(), containerHost.isLocal(),
                        containerHost.getClass().getName() ) );
            }
            catch ( Exception e )
            {
                containerDtos.add( new ContainerDto( containerHost.getId(), containerHost.getEnvironmentId().getId(),
                        containerHost.getHostname(), "UNKNOWN", containerHost.getTemplateName(),
                        containerHost.getContainerSize(), containerHost.getArch().toString(), containerHost.getTags(),
                        containerHost.getPeerId(), "UNKNOWN", containerHost.isLocal(),
                        containerHost.getClass().getName() ) );
            }
        }

        return containerDtos;
    }


    private void checkName( final String name ) throws Exception
    {
        if ( environmentManager.getEnvironments().stream().filter( e -> e.getName().equals( name ) ).count() > 0 )
        {
            throw new Exception( "Duplicated environment name" );
        }
        if ( name.length() > 50 )
        {
            throw new Exception( "Environment name is too long, it should be 50 chars max" );
        }
    }
}