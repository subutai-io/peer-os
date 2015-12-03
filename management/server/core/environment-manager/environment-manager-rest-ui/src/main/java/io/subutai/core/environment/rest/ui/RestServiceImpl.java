package io.subutai.core.environment.rest.ui;


import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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

import io.subutai.common.environment.Blueprint;
import io.subutai.common.environment.ContainerDistributionType;
import io.subutai.common.environment.ContainerHostNotFoundException;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentModificationException;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.environment.NodeGroup;
import io.subutai.common.gson.required.RequiredDeserializer;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostInterface;
import io.subutai.common.metric.ResourceHostMetric;
import io.subutai.common.network.DomainLoadBalanceStrategy;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ContainerType;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.settings.Common;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.environment.api.exception.EnvironmentCreationException;
import io.subutai.core.environment.api.exception.EnvironmentDestructionException;
import io.subutai.core.environment.api.exception.EnvironmentManagerException;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.registry.api.TemplateRegistry;
import io.subutai.core.strategy.api.StrategyManager;


public class RestServiceImpl implements RestService
{
    private static final Logger LOG = LoggerFactory.getLogger( RestServiceImpl.class );

    private Gson gson = RequiredDeserializer.createValidatingGson();

    private static final String ERROR_KEY = "ERROR";

    private final EnvironmentManager environmentManager;
    private final PeerManager peerManager;
    private final TemplateRegistry templateRegistry;
    private final StrategyManager strategyManager;


    public RestServiceImpl( final EnvironmentManager environmentManager, final PeerManager peerManager,
                            final TemplateRegistry templateRegistry, final StrategyManager strategyManager )
    {
        Preconditions.checkNotNull( environmentManager );
        Preconditions.checkNotNull( peerManager );
        Preconditions.checkNotNull( templateRegistry );
        Preconditions.checkNotNull( strategyManager );

        this.environmentManager = environmentManager;
        this.peerManager = peerManager;
        this.templateRegistry = templateRegistry;
        this.strategyManager = strategyManager;
    }



    /** Templates *****************************************************/

    @Override
    public Response listTemplates()
    {
        List<String> templates = templateRegistry.getAllTemplates().stream()
                                                 .map( t -> t.getTemplateName() )
                                                 .collect( Collectors.toList() );

        if ( !templates.isEmpty() )
        {
            return Response.ok().entity( gson.toJson( templates ) ).build();
        }
        else
        {
            return Response.status( Response.Status.NOT_FOUND ).build();
        }
    }



    /** Blueprints ****************************************************/

    @Override
    public Response getBlueprints()
    {
        try
        {
            return Response.ok( gson.toJson( environmentManager.getBlueprints()) ).build();
        }
        catch (EnvironmentManagerException e )
        {
            return Response.status( Response.Status.BAD_REQUEST )
                    .entity( JsonUtil.toJson( ERROR_KEY, "Error loading blueprints" ) ).build();
        }
    }

    @Override
    public Response getBlueprint( UUID blueprintId )
    {
        try
        {
            return Response.ok( gson.toJson( environmentManager.getBlueprint( blueprintId ))).build();
        }
        catch (EnvironmentManagerException e )
        {
            return Response.status( Response.Status.BAD_REQUEST )
                           .entity(JsonUtil.toJson(ERROR_KEY, "Error blueprint not found")).build();
        }

    }

    @Override
    public Response saveBlueprint(final String content)
    {
        try
        {
            Blueprint blueprint = gson.fromJson( content, Blueprint.class );

            for ( NodeGroup nodeGroup : blueprint.getNodeGroups() )
            {
                if ( nodeGroup.getNumberOfContainers() <= 0 )
                {
                    return Response.status( Response.Status.BAD_REQUEST )
                                   .entity(JsonUtil.toJson(ERROR_KEY, "You must specify at least 1 container")).build();
                }
            }

            if( blueprint.getId() == null )
            {
                blueprint.setId( UUID.randomUUID() );
            }

            environmentManager.saveBlueprint( blueprint );

            return Response.ok(gson.toJson(blueprint)).build();
        }
        catch ( Exception e )
        {
            LOG.error( "Error validating blueprint", e );
            return Response.status( Response.Status.BAD_REQUEST ).entity( JsonUtil.toJson( ERROR_KEY, e.getMessage()))
                    .build();
        }
    }

    @Override
    public Response deleteBlueprint(final UUID blueprintId)
    {
        try
        {
            environmentManager.removeBlueprint( blueprintId );
            return Response.ok().build();
        }
        catch (EnvironmentManagerException e )
        {
            return Response.status( Response.Status.BAD_REQUEST )
                           .entity(JsonUtil.toJson(ERROR_KEY, "Error deleting blueprint " + blueprintId)).build();
        }
    }



    /** Domain *****************************************************/

    @Override
    public Response getDefaultDomainName()
    {
        return Response.ok( environmentManager.getDefaultDomainName() ).build();
    }



    /** Environments *****************************************************/

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

        return Response.ok( JsonUtil.toJson( environmentDtos ) ).build();
    }


    @Override
    public Response createEnvironment( final String blueprintJson )
    {
        try
        {
            Blueprint blueprint = gson.fromJson( blueprintJson, Blueprint.class );

            updateContainerPlacementStrategy( blueprint );

            Environment environment =
                    environmentManager.createEnvironment( blueprint, false );
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
    public Response growEnvironment( final String environmentId, final String blueprintJson )
    {
        try
        {
            Blueprint blueprint = gson.fromJson( blueprintJson, Blueprint.class );

            updateContainerPlacementStrategy( blueprint );

            Set<EnvironmentContainerHost> environment =
                    environmentManager.growEnvironment( environmentId, blueprint, false );
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
    public Response setSshKey( final String environmentId, final String key )
    {
        if ( Strings.isNullOrEmpty( key ) )
        {
            return Response.status( Response.Status.BAD_REQUEST )
                           .entity( JsonUtil.toJson( ERROR_KEY, "Invalid ssh key" ) ).build();
        }


        try
        {
            byte[] bytesEncoded = Base64.encodeBase64( key.getBytes() );
            environmentManager.setSshKey( environmentId, new String( bytesEncoded ), false );
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


    /** Environments SSH keys *****************************************************/

    @Override
    public Response removeSshKey( final String environmentId )
    {

        try
        {
            environmentManager.setSshKey( environmentId, null, false );
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



    /** Environment domains *****************************************************/

    @Override
    public Response listDomainLoadBalanceStrategies()
    {
        return Response.ok( JsonUtil.toJson( DomainLoadBalanceStrategy.values() ) ).build();
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


    @Override
    public Response addEnvironmentDomain( String environmentId, String hostName, String strategyJson,
                                          Attachment attr )
    {
        try
        {
            DomainLoadBalanceStrategy strategy = JsonUtil.fromJson( strategyJson, DomainLoadBalanceStrategy.class );
            if( attr == null )
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
            if( environmentManager.isContainerInEnvironmentDomain( containerId, environmentId ) )
            {
                environmentManager.removeContainerFromEnvironmentDomain( containerId, environmentId  );
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


    /** Containers *****************************************************/

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



    /** Container types *****************************************************/
    @Override
    public Response listContainerTypes()
    {
        return Response.ok().entity( gson.toJson( ContainerType.values() ) ).build();
    }



    /** Container quota *****************************************************/
    @Override
    public Response getContainerQuota( final String containerId )
    {
//        try
//        {
//            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );
//
//            LocalPeer localPeer = peerManager.getLocalPeer();
//
//            return Response.ok( String.format("{\"cpu\": %s, \"ram\": %s, \"disk\": {\"HOME\": %s, \"VAR\": %s, \"ROOT_FS\": %s, \"OPT\": %s}}",
//                    localPeer.getContainerHostById( containerId ).getCpuQuota(),
//                    localPeer.getContainerHostById( containerId ).getRamQuota(),
//                    JsonUtil.toJson(
//                        localPeer.getContainerHostById(containerId).getDiskQuota(
//                            JsonUtil.<DiskPartition>fromJson("HOME", new TypeToken<DiskPartition>() {}.getType())
//                        )
//                    ),
//                    JsonUtil.toJson(
//                        localPeer.getContainerHostById(containerId).getDiskQuota(
//                            JsonUtil.<DiskPartition>fromJson("VAR", new TypeToken<DiskPartition>() {}.getType())
//                        )
//                    ),
//                    JsonUtil.toJson(
//                        localPeer.getContainerHostById(containerId).getDiskQuota(
//                            JsonUtil.<DiskPartition>fromJson("ROOT_FS", new TypeToken<DiskPartition>() {}.getType())
//                        )
//                    ),
//                    JsonUtil.toJson(
//                        localPeer.getContainerHostById(containerId).getDiskQuota(
//                            JsonUtil.<DiskPartition>fromJson("OPT", new TypeToken<DiskPartition>() {}.getType())
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


    /** Peers strategy *****************************************************/
    @Override
    public Response listPlacementStrategies()
    {
        return Response.ok( JsonUtil.toJson( strategyManager.getPlacementStrategyTitles() ) ).build();
    }



    /** Peers *****************************************************/
    @Override
    public Response getPeers()
    {
        Map<String, List<String>> peerHostMap = Maps.newHashMap();

        try
        {
            for( Peer peer : peerManager.getPeers() )
            {
                peerHostMap.put( peer.getId(), Lists.newArrayList() );

                Collection<ResourceHostMetric> collection = peer.getResourceHostMetrics().getResources();
                for( ResourceHostMetric metric : collection.toArray(new ResourceHostMetric[ collection.size() ]) )
                {
                        peerHostMap.get( peer.getId() ).add( metric.getHostInfo().getId() );

                }
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Resource hosts are empty", e );
        }


        return Response.ok().entity( JsonUtil.toJson( peerHostMap ) ).build();
    }



    /** Tags *****************************************************/

    @Override
    public Response addTags( final String environmentId, final String containerId, final String tagsJson )
    {
        try
        {
            Environment environment = environmentManager.loadEnvironment( environmentId );

            ContainerHost containerHost = environment.getContainerHostById( containerId );

            Set<String> tags = JsonUtil.fromJson( tagsJson, new TypeToken<Set<String>>(){}.getType() );

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


    @Override
    public Response setupContainerSsh( final String environmentId, final String containerId )
    {
        try
        {
            return Response.ok( environmentManager.setupContainerSsh( containerId, environmentId ) ).build();
        }
        catch( Exception e )
        {
            return Response.status( Response.Status.BAD_REQUEST ).entity( e ).build();
        }
    }



    /** AUX *****************************************************/

    private Set<ContainerDto> convertContainersToContainerJson( Set<EnvironmentContainerHost> containerHosts )
    {
        Set<ContainerDto> containerDtos = Sets.newHashSet();
        for ( EnvironmentContainerHost containerHost : containerHosts )
        {
            ContainerHostState state = containerHost.getState();

            HostInterface iface = containerHost.getInterfaceByName( Common.DEFAULT_CONTAINER_INTERFACE );




            containerDtos.add( new ContainerDto(
                    containerHost.getId(),
                    containerHost.getEnvironmentId().getId(),
                    containerHost.getHostname(),
                    state,
                    iface.getIp(),
                    iface.getMac(),
                    containerHost.getTemplateName(),
                    containerHost.getContainerType(),
                    containerHost.getArch().toString(),
                    containerHost.getTags() ) );

        }
        return containerDtos;
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

    private void updateContainerPlacementStrategy( Blueprint blueprint )
    {
        for( NodeGroup nodeGroup : blueprint.getNodeGroups() )
        {
            if( nodeGroup.getHostId() == null )
            {
                nodeGroup.setContainerDistributionType( ContainerDistributionType.AUTO );
            }
            else
            {
                nodeGroup.setContainerDistributionType( ContainerDistributionType.CUSTOM );
            }
        }
    }
}