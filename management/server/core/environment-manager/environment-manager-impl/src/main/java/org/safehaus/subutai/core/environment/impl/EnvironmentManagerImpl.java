/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.environment.impl;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.UUID;

import javax.sql.DataSource;

import org.safehaus.subutai.common.protocol.CloneContainersMessage;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.EnvironmentBuildTask;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.TopologyEnum;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentDestroyException;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentManagerException;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentPersistenceException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentStatusEnum;
import org.safehaus.subutai.core.environment.impl.builder.TopologyBuilder;
import org.safehaus.subutai.core.environment.impl.dao.EnvironmentDAO;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;
import org.safehaus.subutai.core.security.api.SecurityManager;
import org.safehaus.subutai.core.security.api.SecurityManagerException;
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
public class EnvironmentManagerImpl implements EnvironmentManager, Observer
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
    private SecurityManager securityManager;
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


    public SecurityManager getSecurityManager()
    {
        return securityManager;
    }


    public void setSecurityManager( final SecurityManager securityManager )
    {
        this.securityManager = securityManager;
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
        this.securityManager = null;
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
    public boolean destroyEnvironment( final UUID uuid ) throws EnvironmentDestroyException
    {
        Environment environment = getEnvironmentByUUID( uuid );
        int count = 0;
        for ( ContainerHost container : environment.getContainers() )
        {
            String ip = null;
            try
            {
                ip = container.getPeer().getPeerInfo().getIp();
                container.dispose();
                System.out.println( String.format( "Container %s destroyed.", container.getHostname() ) );
                count++;
            }
            catch ( PeerException e )
            {
                LOG.error( String.format( "Could not destroy container %s on %s: %s", container.getHostname(), ip,
                        e.toString() ) );
                throw new EnvironmentDestroyException( e.getMessage() );
            }
        }


        if ( count == environment.getContainers().size() )
        {
            return environmentDAO.deleteInfo( ENVIRONMENT, uuid.toString() );
        }
        else
        {
            throw new EnvironmentDestroyException( String.format( "Only %d out of %d containers destroyed.", count,
                    environment.getContainers().size() ) );
        }
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
    public void createAdditionalContainers( final UUID id, final String ngJson, final Peer peer )
            throws EnvironmentBuildException
    {
        Environment environment = getEnvironmentByUUID( id );
        NodeGroup nodeGroup = GSON.fromJson( ngJson, NodeGroup.class );

        List<Template> templatesData = new ArrayList();

        List<Template> templates = templateRegistry.getParentTemplates( nodeGroup.getTemplateName() );
        Template installationTemplate = templateRegistry.getTemplate( nodeGroup.getTemplateName() );
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
            templatesData.add( t.getRemoteClone( peerId ) );
        }

        try
        {
            Set<ContainerHost> containers = peerManager.getPeer( peer.getId() ).
                    createContainers( peerId, environment.getId(), templatesData, nodeGroup.getNumberOfNodes(),
                            nodeGroup.getPlacementStrategy().toString(), null );
            if ( !containers.isEmpty() )
            {
                for ( ContainerHost container : containers )
                {
                    container.setNodeGroupName( nodeGroup.getName() );
                    environment.addContainer( container );
                }
            }
            else
            {
                environment.setStatus( EnvironmentStatusEnum.BROKEN );
                saveEnvironment( environment );
                throw new EnvironmentBuildException(
                        String.format( "FAILED creating environment on %s", peer.getId() ) );
            }
        }
        catch ( PeerException e )
        {
            throw new EnvironmentBuildException( e.getMessage() );
        }
        saveEnvironment( environment );
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
                            container.setNodeGroupName( ccm.getNodeGroupName() );
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
                catch ( PeerException e )
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
                        securityManager.configSshOnAgents( environment.getContainers() );
                    }
                    catch ( SecurityManagerException e )
                    {
                        throw new EnvironmentBuildException( e.getMessage() );
                    }
                }
                if ( blueprint.isLinkHosts() )
                {
                    try
                    {
                        securityManager.configHostsOnAgents( environment.getContainers(), blueprint.getDomainName() );
                    }
                    catch ( SecurityManagerException e )
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


    public PeerManager getPeerManager()
    {
        return peerManager;
    }


    public void setPeerManager( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    @Override
    public void update( final Observable o, final Object arg )
    {

    }
}
