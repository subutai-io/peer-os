package io.subutai.core.environment.rest.ui;


import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

import io.subutai.common.environment.*;
import io.subutai.common.gson.required.RequiredDeserializer;
import io.subutai.common.peer.ContainerType;
import io.subutai.common.host.Interface;
import io.subutai.common.network.DomainLoadBalanceStrategy;
import io.subutai.core.environment.api.exception.EnvironmentManagerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import io.subutai.common.host.ContainerHostState;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.settings.Common;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.environment.api.exception.EnvironmentCreationException;
import io.subutai.core.environment.api.exception.EnvironmentDestructionException;
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
                    .entity( JsonUtil.toJson(ERROR_KEY, "Error loading blueprints") ).build();
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
        Set<EnvironmentJson> environmentJsons = Sets.newHashSet();

        for ( Environment environment : environments )
        {
            environmentJsons
                    .add( new EnvironmentJson( environment.getId(), environment.getName(), environment.getStatus(),
                            convertContainersToContainerJson( environment.getContainerHosts() ) ) );
        }

        return Response.ok( JsonUtil.toJson( environmentJsons ) ).build();
    }


    @Override
    public Response createEnvironment( final String blueprintJson )
    {
        try
        {
            Blueprint blueprint = gson.fromJson( blueprintJson, Blueprint.class );

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
            environmentManager.setSshKey( environmentId, key, false );
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
    public Response addEnvironmentDomain( String environmentId, String hostName, DomainLoadBalanceStrategy strategy,
                                          Attachment attr )
    {
        if( attr == null )
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

                return Response.ok().entity( JsonUtil.toJson( "STATE", containerHost.getStatus() ) ).build();
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
        List<Peer> peers = peerManager.getPeers();
        Map<String, String> peerNames = Maps.newHashMap();

        for( Peer peer : peers )
        {
            peerNames.put( peer.getId(), peer.getName() );
        }

        return Response.ok().entity( JsonUtil.toJson( peerNames ) ).build();
    }



    /** AUX *****************************************************/

    private Set<ContainerJson> convertContainersToContainerJson( Set<EnvironmentContainerHost> containerHosts )
    {
        String types[] = {"TINY", "SMALL", "MEDIUM", "LARGE", "HUGE", "CUSTOM"};

        Set<ContainerJson> jsonSet = Sets.newHashSet();
        for ( EnvironmentContainerHost containerHost : containerHosts )
        {
            ContainerHostState state = containerHost.getStatus();

            Interface iface = containerHost.getInterfaceByName( Common.DEFAULT_CONTAINER_INTERFACE );

            jsonSet.add( new ContainerJson( containerHost.getId(), containerHost.getEnvironmentId(),
                    containerHost.getHostname(), state,
                    iface.getIp(),
                    containerHost.getTemplateName(), ContainerType.valueOf( types[ (int)(Math.random() * types.length) ] ) ) );
        }
        return jsonSet;
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
}