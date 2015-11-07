package io.subutai.core.environment.rest.ui;


import java.util.*;

import javax.ws.rs.core.Response;

import io.subutai.common.environment.*;
import io.subutai.common.protocol.Template;
import io.subutai.common.quota.DiskPartition;
import io.subutai.common.quota.DiskQuota;
import io.subutai.common.quota.DiskQuotaUnit;
import io.subutai.common.util.CollectionUtil;
import io.subutai.core.environment.api.exception.EnvironmentManagerException;
import io.subutai.core.peer.api.LocalPeer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;

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
    public Response listTemplates()
    {
        List<String> templates = new ArrayList<>();
        for ( Template template : templateRegistry.getAllTemplates() )
        {
            templates.add( template.getTemplateName() );
        }
        if ( !templates.isEmpty() )
        {
            return Response.ok().entity( JsonUtil.toJson( templates ) ).build();
        }
        else
        {
            return Response.status( Response.Status.NOT_FOUND ).build();
        }
    }

    @Override
    public Response getBlueprints()
    {
        try
        {
            return Response.ok( JsonUtil.toJson( environmentManager.getBlueprints() ) ).build();
        }
        catch (EnvironmentManagerException e )
        {
            return Response.status( Response.Status.BAD_REQUEST )
                    .entity(JsonUtil.toJson(ERROR_KEY, "Error loading blueprints")).build();
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

    @Override
    public Response saveBlueprint(final String content)
    {
        if ( content.length() > 0 )
        {
            try
            {
                Blueprint blueprint = JsonUtil.fromJson( content, Blueprint.class );

                if ( Strings.isNullOrEmpty( blueprint.getName() ) )
                {
                    return Response.status( Response.Status.BAD_REQUEST )
                            .entity(JsonUtil.toJson(ERROR_KEY, "Invalid blueprint name")).build();
                }
                else if ( CollectionUtil.isCollectionEmpty(blueprint.getNodeGroups()) )
                {
                    return Response.status( Response.Status.BAD_REQUEST )
                            .entity(JsonUtil.toJson(ERROR_KEY, "Invalid node group set")).build();
                }
                else
                {
                    for ( NodeGroup nodeGroup : blueprint.getNodeGroups() )
                    {
                        if ( Strings.isNullOrEmpty( nodeGroup.getName() ) )
                        {
                            return Response.status( Response.Status.BAD_REQUEST )
                                    .entity( JsonUtil.toJson( ERROR_KEY, "Invalid node group name" ) ).build();
                        }
                        else if ( nodeGroup.getNumberOfContainers() <= 0 )
                        {
                            return Response.status( Response.Status.BAD_REQUEST )
                                    .entity(JsonUtil.toJson(ERROR_KEY, "Invalid number of containers")).build();
                        }
                        else if ( Strings.isNullOrEmpty( nodeGroup.getTemplateName() ) )
                        {
                            return Response.status( Response.Status.BAD_REQUEST )
                                    .entity(JsonUtil.toJson(ERROR_KEY, "Invalid templateName")).build();
                        }
                        else if ( templateRegistry.getTemplate( nodeGroup.getTemplateName() ) == null )
                        {
                            return Response.status( Response.Status.BAD_REQUEST )
                                    .entity(JsonUtil.toJson(ERROR_KEY, String.format(
                                            "Template %s does not exist", nodeGroup.getTemplateName()
                                    ))).build();
                        }
                        else if ( nodeGroup.getContainerPlacementStrategy() == null )
                        {
                            return Response.status( Response.Status.BAD_REQUEST )
                                    .entity(JsonUtil.toJson(ERROR_KEY, "Invalid node container placement strategy")).build();
                        }
                    }

                    blueprint.setId( UUID.randomUUID() );

                    environmentManager.saveBlueprint( blueprint );

                    return Response.ok(JsonUtil.toJson(blueprint)).build();
                }
            } catch ( Exception e )
            {
                LOG.error( "Error validating blueprint", e );
                return Response.status( Response.Status.BAD_REQUEST ).entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) )
                        .build();
            }
        }
        else
        {
            return Response.status( Response.Status.BAD_REQUEST )
                    .entity(JsonUtil.toJson(ERROR_KEY, "Empty request")).build();
        }
    }


    @Override
    public Response createEnvironment( final String environmentName, final String topologyJsonString,
                                       final String subnetCidr, final String sshKey )
    {
        TopologyJson topologyJson = new TopologyJson();

        //validate params
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentName ), "Invalid environment name" );
            Preconditions.checkArgument( !Strings.isNullOrEmpty( subnetCidr ), "Invalid subnet cidr" );

            topologyJson.setNodeGroupPlacement((Map<String, Set<NodeGroup>>) JsonUtil.fromJson( topologyJsonString, new TypeToken<Map<String, Set<NodeGroup>>>(){}.getType()));
            checkTopology( topologyJson );
        }
        catch ( Exception e )
        {
            LOG.error( "Error validating parameters #createEnvironment", e );
            return Response.status( Response.Status.BAD_REQUEST ).entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) )
                           .build();
        }

        try
        {
            Topology topology = new Topology();

            for ( Map.Entry<String, Set<NodeGroup>> placementEntry : topologyJson.getNodeGroupPlacement().entrySet() )
            {
                Peer peer = peerManager.getPeer( placementEntry.getKey() );
                for ( NodeGroup nodeGroup : placementEntry.getValue() )
                {
                    topology.addNodeGroupPlacement( peer, nodeGroup );
                }
            }

            Environment environment =
                    environmentManager.createEnvironment( environmentName, topology, subnetCidr, sshKey, false );

            return Response.ok( JsonUtil.toJson(
                    new EnvironmentJson( environment.getId(), environment.getName(), environment.getStatus(),
                            convertContainersToContainerJson( environment.getContainerHosts() ) ) ) ).build();
        }
        catch ( EnvironmentCreationException e )
        {
            LOG.error( "Error creating environment #createEnvironment", e );
            return Response.serverError().entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) ).build();
        }
    }


    private void checkTopology( TopologyJson topologyJson ) throws EnvironmentCreationException
    {

        if ( topologyJson.getNodeGroupPlacement() == null || topologyJson.getNodeGroupPlacement().isEmpty() )
        {
            throw new EnvironmentCreationException( "Invalid node group placement" );
        }
        else
        {
            for ( Map.Entry<String, Set<NodeGroup>> placementKey : topologyJson.getNodeGroupPlacement().entrySet() )
            {
                checkNodeGroup( placementKey );
            }
        }
    }


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
    public Response getDefaultDomainName()
    {
        return Response.ok( environmentManager.getDefaultDomainName() ).build();
    }


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
    public Response viewEnvironment( final String environmentId )
    {
        if ( Strings.isNullOrEmpty( environmentId ) )
        {
            return Response.status( Response.Status.BAD_REQUEST )
                           .entity( JsonUtil.toJson( ERROR_KEY, "Invalid environment id" ) ).build();
        }


        try
        {
            Environment environment = environmentManager.loadEnvironment( environmentId );

            return Response.ok( JsonUtil.toJson(
                    new EnvironmentJson( environment.getId(), environment.getName(), environment.getStatus(),
                            convertContainersToContainerJson( environment.getContainerHosts() ) ) ) ).build();
        }
        catch ( EnvironmentNotFoundException e )
        {
            LOG.warn( "Error getting environment by id", environmentId );
            return Response.status( Response.Status.NOT_FOUND ).build();
        }
    }


    @Override
    public Response destroyEnvironment( final String environmentId )
    {
        if ( Strings.isNullOrEmpty( environmentId ) )
        {
            return Response.status( Response.Status.BAD_REQUEST )
                           .entity( JsonUtil.toJson( ERROR_KEY, "Invalid environment id" ) ).build();
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
            return Response.serverError().entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) ).build();
        }
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
    public Response growEnvironment( final String environmentId, final String topologyJsonString )
    {
        if ( Strings.isNullOrEmpty( environmentId ) )
        {
            return Response.status( Response.Status.BAD_REQUEST )
                           .entity( JsonUtil.toJson( ERROR_KEY, "Invalid environment id" ) ).build();
        }

        TopologyJson topologyJson = new TopologyJson();

        try
        {
            topologyJson.setNodeGroupPlacement((Map<String, Set<NodeGroup>>) JsonUtil.fromJson( topologyJsonString, new TypeToken<Map<String, Set<NodeGroup>>>(){}.getType()));
            checkTopology( topologyJson );
        }
        catch ( Exception e )
        {
            LOG.error( "Error validating topology #growEnvironment", e );
            return Response.status( Response.Status.BAD_REQUEST ).entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) )
                           .build();
        }

        try
        {
            Topology topology = buildTopology( topologyJson );

            Set<EnvironmentContainerHost> newContainers = environmentManager.growEnvironment( environmentId, topology, false );

            return Response.ok( JsonUtil.toJson( convertContainersToContainerJson( newContainers ) ) ).build();
        }
        catch ( EnvironmentNotFoundException e )
        {
            LOG.warn( "Error looking for environment by id {}", environmentId );
            return Response.status( Response.Status.NOT_FOUND ).build();
        }
        catch ( EnvironmentModificationException e )
        {
            LOG.error( "Error modifying environment #growEnvironment", e );
            return Response.serverError().entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) ).build();
        }
    }


    private Topology buildTopology( final TopologyJson topologyJson )
    {
        Topology topology = new Topology();
        for ( Map.Entry<String, Set<NodeGroup>> placementEntry : topologyJson.getNodeGroupPlacement().entrySet() )
        {
            Peer peer = peerManager.getPeer( placementEntry.getKey() );
            for ( NodeGroup nodeGroup : placementEntry.getValue() )
            {
                topology.addNodeGroupPlacement( peer, nodeGroup );
            }
        }
        return topology;
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


    @Override
    public Response setSshKey( final String environmentId, final String key )
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
            return Response.serverError().entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) ).build();
        }
    }


    @Override
    public Response removeSshKey( final String environmentId )
    {
        if ( Strings.isNullOrEmpty( environmentId ) )
        {
            return Response.status( Response.Status.BAD_REQUEST )
                           .entity( JsonUtil.toJson( ERROR_KEY, "Invalid environment id" ) ).build();
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
            return Response.serverError().entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) ).build();
        }
    }


    private Set<ContainerJson> convertContainersToContainerJson( Set<EnvironmentContainerHost> containerHosts )
    {
        Set<ContainerJson> jsonSet = Sets.newHashSet();
        for ( EnvironmentContainerHost containerHost : containerHosts )
        {
            ContainerHostState state = containerHost.getStatus();


            jsonSet.add( new ContainerJson( containerHost.getId(), containerHost.getEnvironmentId(),
                    containerHost.getHostname(), state,
                    containerHost.getIpByInterfaceName( Common.DEFAULT_CONTAINER_INTERFACE ),
                    containerHost.getTemplateName() ) );
        }
        return jsonSet;
    }

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

    //Quota
    @Override
    public Response getContainerQuota( final String containerId )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();

            return Response.ok( String.format("{\"cpu\": %s, \"ram\": %s, \"disk\": {\"HOME\": %s, \"VAR\": %s, \"ROOT_FS\": %s, \"OPT\": %s}}",
                    localPeer.getContainerHostById( containerId ).getCpuQuota(),
                    localPeer.getContainerHostById( containerId ).getRamQuota(),
                    JsonUtil.toJson(
                        localPeer.getContainerHostById(containerId).getDiskQuota(
                            JsonUtil.<DiskPartition>fromJson("HOME", new TypeToken<DiskPartition>() {}.getType())
                        )
                    ),
                    JsonUtil.toJson(
                        localPeer.getContainerHostById(containerId).getDiskQuota(
                            JsonUtil.<DiskPartition>fromJson("VAR", new TypeToken<DiskPartition>() {}.getType())
                        )
                    ),
                    JsonUtil.toJson(
                        localPeer.getContainerHostById(containerId).getDiskQuota(
                            JsonUtil.<DiskPartition>fromJson("ROOT_FS", new TypeToken<DiskPartition>() {}.getType())
                        )
                    ),
                    JsonUtil.toJson(
                        localPeer.getContainerHostById(containerId).getDiskQuota(
                            JsonUtil.<DiskPartition>fromJson("OPT", new TypeToken<DiskPartition>() {}.getType())
                        )
                    )
            ) ).build();
        }
        catch ( Exception e )
        {
            LOG.error( "Error getting container quota #getContainerQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }

    @Override
    public Response setContainerQuota( final String containerId, final int cpu, final int ram, final Double diskHome,
                                       final Double diskVar, final Double diskRoot, final Double diskOpt )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            localPeer.getContainerHostById( containerId ).setCpuQuota( cpu );

            localPeer.getContainerHostById( containerId ).setRamQuota( ram );

            if(diskHome > 0) {
                DiskQuota homeDiskQuota = new DiskQuota(DiskPartition.HOME, DiskQuotaUnit.GB, diskHome);
                localPeer.getContainerHostById(containerId).setDiskQuota(homeDiskQuota);
            }

            if(diskVar > 0) {
                DiskQuota varDiskQuota = new DiskQuota(DiskPartition.HOME, DiskQuotaUnit.GB, diskVar);
                localPeer.getContainerHostById(containerId).setDiskQuota(varDiskQuota);
            }

            if(diskRoot > 0) {
                DiskQuota rootDiskQuota = new DiskQuota(DiskPartition.HOME, DiskQuotaUnit.GB, diskRoot);
                localPeer.getContainerHostById(containerId).setDiskQuota(rootDiskQuota);
            }

            if(diskOpt > 0) {
                DiskQuota optDiskQuota = new DiskQuota(DiskPartition.HOME, DiskQuotaUnit.GB, diskOpt);
                localPeer.getContainerHostById(containerId).setDiskQuota(optDiskQuota);
            }
            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOG.error( "Error setting container quota #setContainerQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }

    @Override
    public Response getCpuQuota( final String containerId )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            return Response.ok( localPeer.getContainerHostById( containerId ).getCpuQuota() ).build();
        }
        catch ( Exception e )
        {
            LOG.error( "Error getting cpu quota #getCpuQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response setCpuQuota( final String containerId, final int cpu )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            localPeer.getContainerHostById( containerId ).setCpuQuota( cpu );
            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOG.error( "Error setting cpu quota #setCpuQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }

    @Override
    public Response getDiskQuota( final String containerId, final String diskPartition )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            return Response.ok( JsonUtil.toJson(localPeer.getContainerHostById(containerId).getDiskQuota(
                    JsonUtil.<DiskPartition>fromJson(diskPartition, new TypeToken<DiskPartition>() {
                    }.getType()))) ).build();
        }
        catch ( Exception e )
        {
            LOG.error( "Error getting disk quota #getDiskQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response setDiskQuota( final String containerId, final String diskQuota )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            localPeer.getContainerHostById( containerId )
                    .setDiskQuota( JsonUtil.<DiskQuota>fromJson(diskQuota, new TypeToken<DiskQuota>() {
                    }.getType()) );
            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOG.error( "Error setting disk quota #setDiskQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }

    @Override
    public Response getRamQuota( final String containerId )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            return Response.ok( localPeer.getContainerHostById( containerId ).getRamQuota() ).build();
        } catch (Exception e) {
            LOG.error( "Error getting ram quota #getRamQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response setRamQuota( final String containerId, final int ram )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            localPeer.getContainerHostById( containerId ).setRamQuota( ram );
            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOG.error( "Error setting ram quota #setRamQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }
}