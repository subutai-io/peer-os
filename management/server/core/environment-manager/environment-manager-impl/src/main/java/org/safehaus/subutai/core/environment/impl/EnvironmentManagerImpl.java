/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.environment.impl;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.sql.DataSource;

import org.safehaus.subutai.common.protocol.CloneContainersMessage;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.EnvironmentBuildTask;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.container.api.ContainerCreateException;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.TopologyEnum;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentManagerException;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentPersistenceException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentStatusEnum;
import org.safehaus.subutai.core.environment.impl.builder.TopologyBuilder;
import org.safehaus.subutai.core.environment.impl.dao.EnvironmentDAO;
import org.safehaus.subutai.core.network.api.NetworkManager;
import org.safehaus.subutai.core.network.api.NetworkManagerException;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;


/**
 * This is an implementation of EnvironmentManager
 */
public class EnvironmentManagerImpl implements EnvironmentManager
{

    private static final Logger LOG = LoggerFactory.getLogger( EnvironmentManagerImpl.class.getName() );
    private static final String ENVIRONMENT = "ENVIRONMENT";
    private static final String PROCESS = "PROCESS";
    private static final String BLUEPRINT = "BLUEPRINT";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private PeerManager peerManager;
    private TopologyBuilder topologyBuilder;
    //    private ServiceLocator serviceLocator;
    private EnvironmentDAO environmentDAO;
    private TemplateRegistry templateRegistry;
    private NetworkManager networkManager;
    private Tracker tracker;
    private DataSource dataSource;


    public EnvironmentManagerImpl( final DataSource dataSource ) throws SQLException
    {
        Preconditions.checkNotNull( dataSource, "Data source is null" );
        this.dataSource = dataSource;
    }


    public TemplateRegistry getTemplateRegistry()
    {
        return templateRegistry;
    }


    public void setTemplateRegistry( final TemplateRegistry templateRegistry )
    {
        this.templateRegistry = templateRegistry;
    }


    public NetworkManager getNetworkManager()
    {
        return networkManager;
    }


    public void setNetworkManager( final NetworkManager networkManager )
    {
        this.networkManager = networkManager;
    }


    public Tracker getTracker()
    {
        return tracker;
    }


    public void setTracker( final Tracker tracker )
    {
        this.tracker = tracker;
    }


    public void init()
    {
        try
        {
            //            this.serviceLocator = new ServiceLocator();
            //            this.peerManager = serviceLocator.getService( PeerManager.class );
            //            this.tracker = serviceLocator.getService( Tracker.class );
            //            this.templateRegistry = serviceLocator.getService( TemplateRegistry.class );
            //            this.networkManager = serviceLocator.getService( NetworkManager.class );

            this.topologyBuilder = new TopologyBuilder( this );
            this.environmentDAO = new EnvironmentDAO( dataSource );
        }
        catch ( SQLException e )
        {
            LOG.error( e.getMessage(), e );
        }
    }


    public void destroy()
    {
        this.environmentDAO = null;
        this.templateRegistry = null;
        this.networkManager = null;
        this.peerManager = null;
        this.tracker = null;
        this.topologyBuilder = null;
        this.environmentDAO = null;
    }


    public EnvironmentDAO getEnvironmentDAO()
    {
        return environmentDAO;
    }


    public void setEnvironmentDAO( final EnvironmentDAO environmentDAO )
    {
        this.environmentDAO = environmentDAO;
    }


    /**
     * This module has to be improved by leveraging process action
     */
    @Override
    public Environment buildEnvironment( final EnvironmentBlueprint blueprint ) throws EnvironmentBuildException
    {

        saveBlueprint( GSON.toJson( blueprint ) );
        EnvironmentBuildProcess process = topologyBuilder
                .createEnvironmentBuildProcessB2P( blueprint.getId(), peerManager.getLocalPeer().getId() );


        return buildEnvironment( process );
    }


    @Override
    public List<Environment> getEnvironments()
    {
        return environmentDAO.getInfo( ENVIRONMENT, Environment.class );
    }


    @Override
    public Environment getEnvironment( final String uuid )
    {
        return environmentDAO.getInfo( ENVIRONMENT, uuid, Environment.class );
    }


    @Override
    public boolean destroyEnvironment( final UUID uuid )
    {
        Environment environment = getEnvironmentByUUID( uuid );
        int count = 0;
        /*for ( EnvironmentContainer container : environment.getContainers() )
        {
            DestroyContainersMessage dcm =
                    new DestroyContainersMessage( PeerCommandType.DESTROY, environment.getUuid(), container.getPeerId(),
                            container.getAgentId() );
            dcm.setHostname( container.getHostname() );
            peerCommandDispatcher.invoke( dcm, 1000 * 60 );
            if ( dcm.isSuccess() )
            {
                count++;
            }
        }*/

        //TODO: fix workaround
        /*if ( count == environment.getContainers().size() )
        {
            return environmentDAO.deleteInfo( ENVIRONMENT, uuid );
        }*/
        return environmentDAO.deleteInfo( ENVIRONMENT, uuid.toString() );
    }


    @Override
    public boolean saveBlueprint( String blueprint )
    {
        try
        {
            EnvironmentBlueprint environmentBlueprint = GSON.fromJson( blueprint, EnvironmentBlueprint.class );
            environmentDAO.saveBlueprint( environmentBlueprint );
            return true;
        }
        catch ( JsonParseException | EnvironmentPersistenceException e )
        {
            LOG.error( e.getMessage() );
        }
        return false;
    }


    @Override
    public List<EnvironmentBuildTask> getBlueprintTasks()
    {
        return environmentDAO.getInfo( BLUEPRINT, EnvironmentBuildTask.class );
    }


    @Override
    public List<EnvironmentBlueprint> getBlueprints()
    {
        List<EnvironmentBlueprint> blueprints = new ArrayList<>();
        try
        {
            blueprints = environmentDAO.getBlueprints();
        }
        catch ( EnvironmentPersistenceException e )
        {
            LOG.error( e.getMessage(), e );
        }
        return blueprints;
    }


    @Override
    public boolean deleteBlueprintTask( String uuid )
    {
        return environmentDAO.deleteInfo( BLUEPRINT, uuid );
    }


    @Override
    public boolean deleteBlueprint( UUID blueprintId )
    {
        try
        {
            environmentDAO.deleteBlueprint( blueprintId );
            return true;
        }
        catch ( EnvironmentPersistenceException e )
        {
            LOG.error( e.getMessage(), e );
        }
        return false;
    }


    @Override
    public void saveEnvironment( final Environment environment )
    {
        environmentDAO.saveInfo( ENVIRONMENT, environment.getId().toString(), environment );
    }


    @Override
    public boolean saveBuildProcess( final EnvironmentBuildProcess buildProgress )
    {
        return environmentDAO.saveInfo( PROCESS, buildProgress.getId().toString(), buildProgress );
    }


    @Override
    public List<EnvironmentBuildProcess> getBuildProcesses()
    {
        return environmentDAO.getInfo( PROCESS, EnvironmentBuildProcess.class );
    }


    @Override
    public Environment buildEnvironment( final EnvironmentBuildProcess process ) throws EnvironmentBuildException
    {
        try
        {
            EnvironmentBlueprint blueprint = environmentDAO.getBlueprint( process.getBlueprintId() );

            Environment environment = new Environment( blueprint.getName() );
            saveEnvironment( environment );
            TrackerOperation operation = tracker.createTrackerOperation( environment.getName(), environment.getName() );

            int containerCount = 0;
            for ( String key : process.getMessageMap().keySet() )
            {
                CloneContainersMessage ccm = process.getMessageMap().get( key );
                ccm.setEnvId( environment.getId() );

                containerCount = containerCount + ccm.getNumberOfNodes();

                //TODO: move template addition on create ccm
                List<Template> templates = templateRegistry.getParentTemplates( ccm.getTemplate() );
                Template installationTemplate = templateRegistry.getTemplate( ccm.getTemplate() );
                if ( installationTemplate != null )
                {
                    templates.add( installationTemplate );
                }
                else
                {
                    environment.setStatus( EnvironmentStatusEnum.BROKEN );
                    saveEnvironment( environment );
                    throw new EnvironmentBuildException( "Could not get installation template data" );
                }

                UUID peerId = peerManager.getLocalPeer().getId();

                if ( peerId == null )
                {
                    environment.setStatus( EnvironmentStatusEnum.BROKEN );
                    saveEnvironment( environment );
                    throw new EnvironmentBuildException( "Could not get Peer ID" );
                }


                for ( Template t : templates )
                {
                    ccm.addTemplate( t.getRemoteClone( peerId ) );
                }

                try
                {
                    Set<ContainerHost> containers = peerManager.getPeer( ccm.getPeerId() ).
                            createContainers( peerId, ccm.getEnvId(), ccm.getTemplates(), ccm.getNumberOfNodes(),
                                    ccm.getStrategy(), null );
                    if ( !containers.isEmpty() )
                    {
                        for ( ContainerHost container : containers )
                        {
                            environment.addContainer( container );
                        }
                    }
                    else
                    {
                        environment.setStatus( EnvironmentStatusEnum.BROKEN );
                        saveEnvironment( environment );
                        throw new EnvironmentBuildException(
                                String.format( "FAILED creating environment on %s", ccm.getPeerId() ) );
                    }
                }
                catch ( ContainerCreateException e )
                {
                    LOG.error( e.getMessage(), e );
                    operation.addLogFailed( "Error occured while invoking command." );
                    environment.setStatus( EnvironmentStatusEnum.BROKEN );
                    saveEnvironment( environment );
                    throw new EnvironmentBuildException( e.getMessage() );
                }
            }

            if ( environment.getContainers().isEmpty() )
            {
                environment.setStatus( EnvironmentStatusEnum.EMPTY );
                saveEnvironment( environment );
                throw new EnvironmentBuildException( "No containers assigned to the Environment" );
            }
            else
            {
                if ( blueprint.isExchangeSshKeys() )
                {
                    try
                    {
                        networkManager.configSshOnAgents( environment.getContainers() );
                    }
                    catch ( NetworkManagerException e )
                    {
                        throw new EnvironmentBuildException( e.getMessage() );
                    }
                }
                if ( blueprint.isLinkHosts() )
                {
                    try
                    {
                        networkManager.configHostsOnAgents( environment.getContainers(), blueprint.getDomainName() );
                    }
                    catch ( NetworkManagerException e )
                    {
                        throw new EnvironmentBuildException( e.getMessage() );
                    }
                }
                if ( environment.getContainers().size() != containerCount )
                {
                    environment.setStatus( EnvironmentStatusEnum.UNHEALTHY );
                }
                else
                {
                    environment.setStatus( EnvironmentStatusEnum.HEALTHY );
                }
                saveEnvironment( environment );
            }
            operation.addLogDone( "Complete" );
            return environment;
        }
        catch ( EnvironmentPersistenceException e )
        {
            throw new EnvironmentBuildException( e.getMessage() );
        }
    }


    @Override
    public void deleteBuildProcess( final EnvironmentBuildProcess environmentBuildProcess )
    {
        environmentDAO.deleteInfo( PROCESS, environmentBuildProcess.getId().toString() );
    }


    @Override
    public Set<ContainerHost> getConnectedContainers( final Environment environment )
    {

        Set<UUID> peers = new HashSet<>();
        for ( ContainerHost ec : environment.getContainers() )
        {
            peers.add( ec.getPeerId() );
        }

        Set<ContainerHost> freshContainers = new HashSet<>();
        for ( UUID peerId : peers )
        {

            Peer peer = peerManager.getPeer( peerId );
            Set<ContainerHost> containers = new HashSet();
            try
            {
                for ( ContainerHost c : peer.getContainerHostsByEnvironmentId( environment.getId() ) )
                {
                    containers.add(c);
                }
            }
            catch ( PeerException e )
            {
                LOG.warn( "Error on getting environment containers: " + e.toString() );
            }
        }
        return freshContainers;
    }


    @Override
    public Environment getEnvironmentByUUID( final UUID environmentId )
    {
        return environmentDAO.getInfo( ENVIRONMENT, environmentId.toString(), Environment.class );
    }


    @Override
    public boolean saveBuildProcess( final UUID blueprintId, final Map<Object, Peer> topology,
                                     final Map<Object, NodeGroup> map, TopologyEnum topologyEnum )
    {
        EnvironmentBuildProcess process = null;

        switch ( topologyEnum )
        {
            case NODE_2_PEER:
            {
                process = topologyBuilder.createEnvironmentBuildProcessN2P( blueprintId, topology, map );
                break;
            }
            case NODE_GROUP_2_PEER:
            {
                process = topologyBuilder.createEnvironmentBuildProcessNG2Peer( blueprintId, topology, map );
                break;
            }
            case BLUEPRINT_2_PEER:
                break;
            case BLUEPRINT_2_PEER_GROUP:
                break;
            case NODE_GROUP_2_PEER_GROUP:
                break;
            default:
            {
                break;
            }
        }
        if ( process != null )
        {
            return environmentDAO.saveInfo( PROCESS, process.getId().toString(), process );
        }
        else
        {
            return false;
        }
    }


    @Override
    public boolean saveBuildProcessB2PG( final UUID blueprintId, final UUID peerGroupId )
            throws EnvironmentManagerException
    {
        TopologyBuilder topologyBuilder = new TopologyBuilder( this );
        try
        {
            EnvironmentBuildProcess process =
                    topologyBuilder.createEnvironmentBuildProcessB2PG( blueprintId, peerGroupId );
            if ( process != null )
            {
                return environmentDAO.saveInfo( PROCESS, process.getId().toString(), process );
            }
            else
            {
                return false;
            }
        }
        catch ( EnvironmentBuildException e )
        {
            throw new EnvironmentManagerException( e.getMessage() );
        }
    }


    @Override
    public EnvironmentBlueprint getEnvironmentBlueprint( final UUID blueprintId ) throws EnvironmentManagerException
    {
        try
        {
            return environmentDAO.getBlueprint( blueprintId );
        }
        catch ( EnvironmentPersistenceException e )
        {
            throw new EnvironmentManagerException( e.getMessage() );
        }
    }


    @Override
    public boolean saveBuildProcessNG2PG( final UUID blueprintId, final UUID peerGroupId )
            throws EnvironmentManagerException
    {
        TopologyBuilder topologyBuilder = new TopologyBuilder( this );
        try
        {
            EnvironmentBuildProcess process =
                    topologyBuilder.createEnvironmentBuildProcessNG2PG( blueprintId, peerGroupId );
            if ( process != null )
            {
                return environmentDAO.saveInfo( PROCESS, process.getId().toString(), process );
            }
            else
            {
                return false;
            }
        }
        catch ( EnvironmentBuildException e )
        {
            throw new EnvironmentManagerException( e.getMessage() );
        }
    }


    public PeerManager getPeerManager()
    {
        return peerManager;
    }


    public void setPeerManager( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }
}
