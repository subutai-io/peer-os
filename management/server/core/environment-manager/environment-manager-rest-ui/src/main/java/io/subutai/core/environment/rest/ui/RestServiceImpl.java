package io.subutai.core.environment.rest.ui;


import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import io.subutai.common.environment.ContainerHostNotFoundException;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentModificationException;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.environment.NodeGroup;
import io.subutai.common.environment.Topology;
import io.subutai.common.gson.required.RequiredDeserializer;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostInterface;
import io.subutai.common.metric.ResourceHostMetric;
import io.subutai.common.network.DomainLoadBalanceStrategy;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ContainerSize;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.protocol.TemplateKurjun;
import io.subutai.common.quota.ContainerQuota;
import io.subutai.common.resource.PeerGroupResources;
import io.subutai.common.resource.PeerResources;
import io.subutai.common.settings.Common;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.environment.api.ShareDto.ShareDto;
import io.subutai.core.environment.api.exception.EnvironmentCreationException;
import io.subutai.core.environment.api.exception.EnvironmentDestructionException;
import io.subutai.core.kurjun.api.TemplateManager;
import io.subutai.core.lxc.quota.api.QuotaManager;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.strategy.api.ContainerPlacementStrategy;
import io.subutai.core.strategy.api.ExampleStrategy;
import io.subutai.core.strategy.api.NodeSchema;
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


    /** Templates *************************************************** */

    @Override
    public Response listTemplates()
    {
        Set<String> templates =
                templateRegistry.list().stream().map( TemplateKurjun::getName ).collect( Collectors.toSet() );

        if ( !templates.isEmpty() )
        {
            return Response.ok().entity( gson.toJson( templates ) ).build();
        }
        else
        {
            return Response.status( Response.Status.NOT_FOUND ).build();
        }
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
        //        if ( envs.size() > 0 )
        //        {
        //            return Response.ok( JsonUtil.toJson( envs ) ).build();
        //        }
        Set<Environment> environments = environmentManager.getEnvironments();
        Set<EnvironmentDto> environmentDtos = Sets.newHashSet();

        for ( Environment environment : environments )
        {
            EnvironmentDto environmentDto =
                    new EnvironmentDto( environment.getId(), environment.getName(), environment.getStatus(),
                            convertContainersToContainerJson( environment.getContainerHosts() ),
                            environment.getRelationDeclaration() );
            //            environmentDto.setRevoke( true );
            environmentDtos.add( environmentDto );
        }

        //        envs.addAll( environmentDtos );
        return Response.ok( JsonUtil.toJson( environmentDtos ) ).build();
    }


    @Override
    public Response accessStatus( final String environmentId )
    {
        //        for ( final EnvironmentDto env : envs )
        //        {
        //            if ( env.getId().equals( environmentId ) )
        //            {
        //                env.setRevoke( !env.isRevoke() );
        //            }
        //        }
        return Response.ok().build();
    }


    @Override
    public Response buildAuto( final String name, final String containersJson )
    {

        Environment environment = null;

        try
        {
            ContainerPlacementStrategy placementStrategy = strategyManager.findStrategyById(
                    ExampleStrategy.ID );

//            if( !(placementStrategy instanceof GroupPlacementStrategy ) )
//            {
//                return Response.serverError().entity( JsonUtil.toJson( ERROR_KEY, "Internal error, GroupPlacementStrategy strategy not found" ) ).build();
//            }

            List<NodeSchema> schema = JsonUtil.fromJson( containersJson, new TypeToken<List<NodeSchema>>() {}.getType() );

            placementStrategy.setScheme( schema );

            final List<PeerResources> resources = new ArrayList<>();
            for ( final Peer peer : peerManager.getPeers() )
            {
                PeerResources peerResources =
                        peerManager.getPeer( peer.getId() ).getResourceLimits( peerManager.getLocalPeer().getId() );
                resources.add( peerResources );
            }


            final PeerGroupResources peerGroupResources = new PeerGroupResources( resources );
            final Map<ContainerSize, ContainerQuota> quotas = quotaManager.getDefaultQuotas();

            Topology topology = placementStrategy.distribute( name, 0, 0, peerGroupResources, quotas );

            environment = environmentManager.setupRequisites( topology );
        }
        catch ( Exception e )
        {
            return Response.serverError().entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) ).build();
        }

        return Response.ok( JsonUtil.toJson( Lists.newArrayList( environment.getId(), environment.getRelationDeclaration() ) ) ).build();
    }


    @Override
    public Response setupStrategyRequisites( final String name, final String strategy, int sshId, int hostId,
                                             String peerIdList )
    {
        EnvironmentDto environmentDto = null;

        try
        {
            List<String> peerIds = JsonUtil.fromJson( peerIdList, new TypeToken<List<String>>() {}.getType() );

            ContainerPlacementStrategy placementStrategy = strategyManager.findStrategyById( strategy );

            final List<PeerResources> resources = new ArrayList<>();
            for ( String peerId : peerIds )
            {
                if ( "local".equals( peerId ) )
                {
                    resources.add( peerManager.getLocalPeer().getResourceLimits( peerManager.getLocalPeer().getId() ) );
                    continue;
                }

                PeerResources peerResources =
                        peerManager.getPeer( peerId ).getResourceLimits( peerManager.getLocalPeer().getId() );
                resources.add( peerResources );
            }

            final PeerGroupResources peerGroupResources = new PeerGroupResources( resources );

            final Map<ContainerSize, ContainerQuota> quotas = quotaManager.getDefaultQuotas();

            Topology topology = placementStrategy.distribute( name, sshId, hostId, peerGroupResources, quotas );

            Environment environment = environmentManager.setupRequisites( topology );

            environmentDto = new EnvironmentDto( environment.getId(), environment.getName(), environment.getStatus(),
                    Sets.newHashSet(), environment.getRelationDeclaration() );
        }
        catch ( Exception e )
        {
            return Response.serverError().entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) ).build();
        }

        return Response.ok( JsonUtil.toJson( environmentDto ) ).build();
    }


    @Override
    public Response setupRequisites( final String name, final String topologyJson )
    {
        EnvironmentDto environmentDto;
        try
        {
            Map<String, Set<NodeGroup>> nodeGroupPlacement = gson.fromJson( topologyJson, new TypeToken<Map<String, Set<NodeGroup>>>() {}.getType() );


            Topology topology = new Topology( name, 0, 0 );


            Iterator it = nodeGroupPlacement.entrySet().iterator();
            while( it.hasNext() )
            {
                Map.Entry pair = (Map.Entry)it.next();

                for( NodeGroup nodeGroup : (Set<NodeGroup>)pair.getValue() )
                {
                    topology.addNodeGroupPlacement( (String) pair.getKey(), nodeGroup );
                }
            }


            Environment environment = environmentManager.setupRequisites( topology );
            environmentDto = new EnvironmentDto( environment.getId(), environment.getName(), environment.getStatus(),
                    Sets.newHashSet(), environment.getRelationDeclaration() );
        }
        catch ( EnvironmentCreationException e )
        {
            LOG.error( "Error creating environment #createEnvironment", e );
            return Response.serverError().entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) ).build();
        }
        catch ( JsonParseException e )
        {
            LOG.error( "Error validating parameters #createEnvironment", e );
            return Response.status( Response.Status.BAD_REQUEST ).entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) )
                           .build();
        }

        return Response.ok( JsonUtil.toJson( environmentDto ) ).build();
    }


    @Override
    public Response startEnvironmentBuild( final String environmentId, final String signedMessage )
    {
        try
        {
            Environment environment = environmentManager.startEnvironmentBuild( environmentId, signedMessage, false );
        }
        catch ( EnvironmentCreationException e )
        {
            LOG.error( "Error creating environment #createEnvironment", e );
            return Response.serverError().entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) ).build();
        }
        catch ( JsonParseException e )
        {
            LOG.error( "Error validating parameters #createEnvironment", e );
            return Response.status( Response.Status.BAD_REQUEST ).entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) )
                           .build();
        }

        return Response.ok().build();
    }


    @Override
    public Response growEnvironment( final String environmentId, final String topologyJson )
    {
        try
        {
            Map<String, Set<NodeGroup>> nodeGroupPlacement = gson.fromJson( topologyJson, new TypeToken<Map<String, Set<NodeGroup>>>() {}.getType() );

            String name = environmentManager.getEnvironments().stream().filter( e -> e.getEnvironmentId().equals( environmentId )).findFirst( ).get().getName();

            Topology topology = new Topology( name, 0, 0 );


            Iterator it = nodeGroupPlacement.entrySet().iterator();
            while( it.hasNext() )
            {
                Map.Entry pair = (Map.Entry)it.next();

                for( NodeGroup nodeGroup : (Set<NodeGroup>)pair.getValue() )
                {
                    topology.addNodeGroupPlacement( (String) pair.getKey(), nodeGroup );
                }
            }



            Set<EnvironmentContainerHost> environment =
                    environmentManager.growEnvironment( environmentId, topology, false );
        }
        catch ( Exception e )
        {
            LOG.error( "Error validating parameters #growEnvironment", e );
            return Response.status( Response.Status.BAD_REQUEST ).entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) )
                           .build();
        }

        return Response.ok().build();
    }


    @Override
    public Response destroyEnvironment( final String environmentId )
    {
        try
        {
            environmentManager.destroyEnvironment( environmentId, false, false );
        }
        catch ( EnvironmentNotFoundException e )
        {
            LOG.warn( "Error getting environment by id {}", environmentId );
            return Response.status( Response.Status.NOT_FOUND ).build();
        }
        catch ( EnvironmentDestructionException e )
        {
            LOG.error( "Error destroying environment #destroyEnvironment", e );
            return Response.serverError().entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) ).build();
        }

        return Response.ok().build();
    }


    @Override
    public Response getEnvironmentSShKeys( final String environmentId )
    {
        try
        {
            Environment environment = environmentManager.loadEnvironment( environmentId );

            return Response.ok( JsonUtil.toJson( environment.getSshKeys() ) ).build();
        }
        catch ( EnvironmentNotFoundException e )
        {
            LOG.error( "Cannot find environment ", e );
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
        catch ( EnvironmentNotFoundException e )
        {
            LOG.warn( "Environment not found by id {}", environmentId );
            return Response.status( Response.Status.NOT_FOUND ).build();
        }
        catch ( EnvironmentModificationException e )
        {
            LOG.error( "Environment modification failed", e );
            return Response.serverError().entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) ).build();
        }
        catch ( Exception e )
        {
            LOG.error( "Exception setting ssh key", e );
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
        catch ( EnvironmentNotFoundException e )
        {
            LOG.warn( "Exception getting environment by id {}", environmentId );
            return Response.status( Response.Status.NOT_FOUND ).build();
        }
        catch ( EnvironmentModificationException e )
        {
            LOG.error( "Error modifying environment", e );
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
            LOG.error( "getEnvironmentDomain error", e );
            return Response.status( Response.Status.BAD_REQUEST ).entity( JsonUtil.toJson( e.getMessage() ) ).build();
        }
    }


    /** Environment domains **************************************************** */

    @Override
    public Response listDomainLoadBalanceStrategies()
    {
        return Response.ok( JsonUtil.toJson( DomainLoadBalanceStrategy.values() ) ).build();
    }


    @Override
    public Response addEnvironmentDomain( String environmentId, String hostName, String strategyJson, Attachment attr )
    {
        try
        {
            DomainLoadBalanceStrategy strategy = JsonUtil.fromJson( strategyJson, DomainLoadBalanceStrategy.class );
            if ( attr == null )
            {
                throw new Exception( "Error, cannot read an attachment", null );
            }

            File file = new File( System.getProperty( "java.io.tmpdir" ) + "/" + environmentId );
            file.createNewFile();
            attr.transferTo( file );

            environmentManager.assignEnvironmentDomain( environmentId, hostName, strategy,
                    System.getProperty( "java.io.tmpdir" ) + "/" + environmentId );
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
        catch ( EnvironmentModificationException e )
        {
            LOG.error( "Error removing sshKey ", e );
            return Response.serverError().entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) ).build();
        }
        catch ( EnvironmentNotFoundException e )
        {
            LOG.error( "Cannot find environment ", e );
            return Response.serverError().entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) ).build();
        }

        return Response.ok().build();
    }


    @Override
    public Response isContainerDomain( final String environmentId, final String containerId )
    {
        try
        {
            return Response.ok( environmentManager.isContainerInEnvironmentDomain( containerId, environmentId ) )
                           .build();
        }
        catch ( Exception e )
        {
            LOG.error( "Cannot check domain status of container", e );
            return Response.status( Response.Status.BAD_REQUEST ).entity( JsonUtil.toJson( e.getMessage() ) ).build();
        }
    }


    @Override
    public Response setContainerDomain( final String environmentId, final String containerId )
    {
        try
        {
            if ( environmentManager.isContainerInEnvironmentDomain( containerId, environmentId ) )
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
    public Response getContainerEnvironmentId( final String containerId )
    {
        if ( Strings.isNullOrEmpty( containerId ) )
        {
            return Response.status( Response.Status.BAD_REQUEST )
                           .entity( JsonUtil.toJson( ERROR_KEY, "Invalid container id" ) ).build();
        }


        Environment environment = findEnvironmentByContainerId( containerId );

        if ( environment != null )
        {
            return Response.ok( environment.getId() ).build();
        }

        return Response.status( Response.Status.NOT_FOUND ).build();
    }


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

                environmentManager.destroyContainer( environment.getId(), containerHost.getId(), false, false );

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


    /** Container quota **************************************************** */
    @Override
    public Response getContainerQuota( final String containerId )
    {
        //        try
        //        {
        //            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );
        //
        //            LocalPeer localPeer = peerManager.getLocalPeer();
        //
        //            return Response.ok( String.format("{\"cpu\": %s, \"ram\": %s, \"disk\": {\"HOME\": %s, \"VAR\":
        // %s, \"ROOT_FS\": %s, \"OPT\": %s}}",
        //                    localPeer.getContainerHostById( containerId ).getCpuQuota(),
        //                    localPeer.getContainerHostById( containerId ).getRamQuota(),
        //                    JsonUtil.toJson(
        //                        localPeer.getContainerHostById(containerId).getDiskQuota(
        //                            JsonUtil.<DiskPartition>fromJson("HOME", new TypeToken<DiskPartition>() {}
        // .getType())
        //                        )
        //                    ),
        //                    JsonUtil.toJson(
        //                        localPeer.getContainerHostById(containerId).getDiskQuota(
        //                            JsonUtil.<DiskPartition>fromJson("VAR", new TypeToken<DiskPartition>() {}
        // .getType())
        //                        )
        //                    ),
        //                    JsonUtil.toJson(
        //                        localPeer.getContainerHostById(containerId).getDiskQuota(
        //                            JsonUtil.<DiskPartition>fromJson("ROOT_FS", new TypeToken<DiskPartition>() {}
        // .getType())
        //                        )
        //                    ),
        //                    JsonUtil.toJson(
        //                        localPeer.getContainerHostById(containerId).getDiskQuota(
        //                            JsonUtil.<DiskPartition>fromJson("OPT", new TypeToken<DiskPartition>() {}
        // .getType())
        //                        )
        //                    )
        //            ) ).build();
        //        }
        //        catch ( Exception e )
        //        {
        //            LOG.error( "Error getting container quota #getContainerQuota", e );
        //            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        //        }
        return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
    }


    @Override
    public Response setContainerQuota( final String containerId, final int cpu, final int ram, final Double diskHome,
                                       final Double diskVar, final Double diskRoot, final Double diskOpt )
    {
        //        try
        //        {
        //            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );
        //
        //            LocalPeer localPeer = peerManager.getLocalPeer();
        //            localPeer.getContainerHostById( containerId ).setCpuQuota( cpu );
        //
        //            localPeer.getContainerHostById( containerId ).setRamQuota( ram );
        //
        //            if(diskHome > 0) {
        //                DiskQuota homeDiskQuota = new DiskQuota(DiskPartition.HOME, DiskQuotaUnit.GB, diskHome);
        //                localPeer.getContainerHostById(containerId).setDiskQuota(homeDiskQuota);
        //            }
        //
        //            if(diskVar > 0) {
        //                DiskQuota varDiskQuota = new DiskQuota(DiskPartition.HOME, DiskQuotaUnit.GB, diskVar);
        //                localPeer.getContainerHostById(containerId).setDiskQuota(varDiskQuota);
        //            }
        //
        //            if(diskRoot > 0) {
        //                DiskQuota rootDiskQuota = new DiskQuota(DiskPartition.HOME, DiskQuotaUnit.GB, diskRoot);
        //                localPeer.getContainerHostById(containerId).setDiskQuota(rootDiskQuota);
        //            }
        //
        //            if(diskOpt > 0) {
        //                DiskQuota optDiskQuota = new DiskQuota(DiskPartition.HOME, DiskQuotaUnit.GB, diskOpt);
        //                localPeer.getContainerHostById(containerId).setDiskQuota(optDiskQuota);
        //            }
        //            return Response.ok().build();
        //        }
        //        catch ( Exception e )
        //        {
        //            LOG.error( "Error setting container quota #setContainerQuota", e );
        //            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        //        }

        return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
    }


    @Override
    public Response getRamQuota( final String containerId )
    {
        //        try
        //        {
        //            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );
        //
        //            LocalPeer localPeer = peerManager.getLocalPeer();
        //            return Response.ok( localPeer.getContainerHostById( containerId ).getRamQuota() ).build();
        //        } catch (Exception e) {
        //            LOG.error( "Error getting ram quota #getRamQuota", e );
        //            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        //        }
        return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
    }


    @Override
    public Response setRamQuota( final String containerId, final int ram )
    {
        //        try
        //        {
        //            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );
        //
        //            LocalPeer localPeer = peerManager.getLocalPeer();
        //            localPeer.getContainerHostById( containerId ).setRamQuota( ram );
        //            return Response.ok().build();
        //        }
        //        catch ( Exception e )
        //        {
        //            LOG.error( "Error setting ram quota #setRamQuota", e );
        //            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        //        }
        return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
    }


    @Override
    public Response getCpuQuota( final String containerId )
    {
        //        try
        //        {
        //            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );
        //
        //            LocalPeer localPeer = peerManager.getLocalPeer();
        //            return Response.ok( localPeer.getContainerHostById( containerId ).getCpuQuota() ).build();
        //        }
        //        catch ( Exception e )
        //        {
        //            LOG.error( "Error getting cpu quota #getCpuQuota", e );
        //            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        //        }
        return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
    }


    @Override
    public Response setCpuQuota( final String containerId, final int cpu )
    {
        //        try
        //        {
        //            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );
        //
        //            LocalPeer localPeer = peerManager.getLocalPeer();
        //            localPeer.getContainerHostById( containerId ).setCpuQuota( cpu );
        //            return Response.ok().build();
        //        }
        //        catch ( Exception e )
        //        {
        //            LOG.error( "Error setting cpu quota #setCpuQuota", e );
        //            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        //        }
        return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
    }


    @Override
    public Response getDiskQuota( final String containerId, final String diskPartition )
    {
        //        try
        //        {
        //            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );
        //
        //            LocalPeer localPeer = peerManager.getLocalPeer();
        //            return Response.ok( JsonUtil.toJson(localPeer.getContainerHostById(containerId).getDiskQuota(
        //                    JsonUtil.<DiskPartition>fromJson(diskPartition, new TypeToken<DiskPartition>() {
        //                    }.getType()))) ).build();
        //        }
        //        catch ( Exception e )
        //        {
        //            LOG.error( "Error getting disk quota #getDiskQuota", e );
        //            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        //        }
        return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
    }


    @Override
    public Response setDiskQuota( final String containerId, final String diskQuota )
    {
        //        try
        //        {
        //            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );
        //
        //            LocalPeer localPeer = peerManager.getLocalPeer();
        //            localPeer.getContainerHostById( containerId )
        //                    .setDiskQuota( JsonUtil.<DiskQuota>fromJson(diskQuota, new TypeToken<DiskQuota>() {
        //                    }.getType()) );
        //            return Response.ok().build();
        //        }
        //        catch ( Exception e )
        //        {
        //            LOG.error( "Error setting disk quota #setDiskQuota", e );
        //            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        //        }
        return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
    }


    /** Peers strategy **************************************************** */
    @Override
    public Response listPlacementStrategies()
    {
        return Response.ok( JsonUtil.toJson( strategyManager.getPlacementStrategyTitles() ) ).build();
    }


    /** Peers **************************************************** */
    @Override
    public Response getPeers()
    {
        Map<String, List<String>> peerHostMap = Maps.newHashMap();

        try
        {
            for ( Peer peer : peerManager.getPeers() )
            {
                if ( peer.isOnline() )
                {
                    peerHostMap.put( peer.getId(), Lists.newArrayList() );

                    Collection<ResourceHostMetric> collection = peer.getResourceHostMetrics().getResources();
                    for ( ResourceHostMetric metric : collection.toArray( new ResourceHostMetric[collection.size()] ) )
                    {
                        peerHostMap.get( peer.getId() ).add( metric.getHostInfo().getId() );
                    }
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

            ContainerHost containerHost = environment.getContainerHostById( containerId );

            Set<String> tags = JsonUtil.fromJson( tagsJson, new TypeToken<Set<String>>()
            {}.getType() );

            tags.stream().forEach( tag -> containerHost.addTag( tag ) );

            environmentManager.notifyOnContainerStateChanged( environment, null );
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

            environmentManager.notifyOnContainerStateChanged( environment, null );
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.BAD_REQUEST ).entity( JsonUtil.toJson( e ) ).build();
        }

        return Response.ok().build();
    }


    /** Additional ****************************************/


    @Override
    public Response setupContainerSsh( final String environmentId, final String containerId )
    {
        try
        {
            return Response.ok( environmentManager.setupContainerSsh( containerId, environmentId ) ).build();
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
            List<ShareDto> sharedUsers = environmentManager.getSharedUsers( objectId );
            return Response.ok( JsonUtil.toJson( sharedUsers ) ).build();
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response shareEnvironment( final String users, final String environmentId )
    {
        ShareDto[] shareDto = gson.fromJson( users, ShareDto[].class );

        environmentManager.shareEnvironment( shareDto, environmentId );

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
            ContainerHostState state = containerHost.getState();

            HostInterface iface = containerHost.getInterfaceByName( Common.DEFAULT_CONTAINER_INTERFACE );


            containerDtos.add( new ContainerDto( containerHost.getId(), containerHost.getEnvironmentId().getId(),
                    containerHost.getHostname(), state, iface.getIp(), iface.getMac(), containerHost.getTemplateName(),
                    containerHost.getContainerSize(), containerHost.getArch().toString(), containerHost.getTags() ) );
        }
        return containerDtos;
    }
}