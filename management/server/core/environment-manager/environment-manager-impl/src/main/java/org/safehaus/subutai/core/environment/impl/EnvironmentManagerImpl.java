/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.environment.impl;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.sql.DataSource;

import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.EnvironmentBuildTask;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentDestroyException;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentManagerException;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentPersistenceException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;
import org.safehaus.subutai.core.environment.api.topology.Blueprint2PeerData;
import org.safehaus.subutai.core.environment.api.topology.Blueprint2PeerGroupData;
import org.safehaus.subutai.core.environment.api.topology.Node2PeerData;
import org.safehaus.subutai.core.environment.api.topology.NodeGroup2PeerData;
import org.safehaus.subutai.core.environment.api.topology.NodeGroup2PeerGroupData;
import org.safehaus.subutai.core.environment.api.topology.TopologyData;
import org.safehaus.subutai.core.environment.impl.builder.Blueprint2PeerBuilder;
import org.safehaus.subutai.core.environment.impl.builder.Blueprint2PeerGroupBuilder;
import org.safehaus.subutai.core.environment.impl.builder.EnvironmentBuildProcessFactory;
import org.safehaus.subutai.core.environment.impl.builder.Node2PeerBuilder;
import org.safehaus.subutai.core.environment.impl.builder.NodeGroup2PeerBuilder;
import org.safehaus.subutai.core.environment.impl.builder.NodeGroup2PeerGroupBuilder;
import org.safehaus.subutai.core.environment.impl.builder.ProcessBuilderException;
import org.safehaus.subutai.core.environment.impl.dao.EnvironmentDAO;
import org.safehaus.subutai.core.environment.impl.environment.BuildException;
import org.safehaus.subutai.core.environment.impl.environment.DestroyException;
import org.safehaus.subutai.core.environment.impl.environment.EnvironmentBuilder;
import org.safehaus.subutai.core.environment.impl.environment.EnvironmentBuilderImpl;
import org.safehaus.subutai.core.environment.impl.environment.EnvironmentDestroyer;
import org.safehaus.subutai.core.environment.impl.environment.EnvironmentDestroyerImpl;
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
public class EnvironmentManagerImpl implements EnvironmentManager
{

    private static final Logger LOG = LoggerFactory.getLogger( EnvironmentManagerImpl.class.getName() );
    private static final String ENVIRONMENT = "ENVIRONMENT";
    private static final String PROCESS = "PROCESS";
    private static final String BLUEPRINT = "BLUEPRINT";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private PeerManager peerManager;
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
        try
        {
            UUID blueprintId = saveBlueprint( GSON.toJson( blueprint ) );
            TopologyData data = new Blueprint2PeerData( peerManager.getLocalPeer().getId(), blueprintId );
            UUID processId = saveBuildProcess( data );

            EnvironmentBuildProcess process =
                    environmentDAO.getInfo( PROCESS, processId.toString(), EnvironmentBuildProcess.class );
            return buildEnvironment( process );
        }
        catch ( EnvironmentManagerException e )
        {
            throw new EnvironmentBuildException( e.getMessage() );
        }
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
    public boolean destroyEnvironment( final UUID environmentId ) throws EnvironmentDestroyException
    {
        Environment environment = getEnvironmentByUUID( environmentId );
        EnvironmentDestroyer destroyer = new EnvironmentDestroyerImpl();
        try
        {
            destroyer.destroy( environment );
            environmentDAO.deleteInfo( ENVIRONMENT, environment.getId().toString() );
        }
        catch ( DestroyException e )
        {
            LOG.error( e.getMessage(), e );
            throw new EnvironmentDestroyException( e.getMessage() );
        }
        return true;
    }


    @Override
    public UUID saveBlueprint( String blueprint ) throws EnvironmentManagerException
    {
        try
        {
            EnvironmentBlueprint environmentBlueprint = GSON.fromJson( blueprint, EnvironmentBlueprint.class );
            return environmentDAO.saveBlueprint( environmentBlueprint );
        }
        catch ( JsonParseException | EnvironmentPersistenceException e )
        {
            LOG.error( e.getMessage() );
            throw new EnvironmentManagerException( e.getMessage() );
        }
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
    public void saveEnvironment( final Environment environment ) throws EnvironmentManagerException
    {
        try
        {
            environmentDAO.saveInfo( ENVIRONMENT, environment.getId().toString(), environment );
        }
        catch ( EnvironmentPersistenceException e )
        {
            LOG.error( e.getMessage(), e );
            throw new EnvironmentManagerException( e.getMessage() );
        }
    }


    @Override
    public boolean saveBuildProcess( final EnvironmentBuildProcess buildProgress ) throws EnvironmentManagerException
    {
        try
        {
            return environmentDAO.saveInfo( PROCESS, buildProgress.getId().toString(), buildProgress );
        }
        catch ( EnvironmentPersistenceException e )
        {
            LOG.error( e.getMessage(), e );
            throw new EnvironmentManagerException( e.getMessage() );
        }
    }


    @Override
    public List<EnvironmentBuildProcess> getBuildProcesses()
    {
        return environmentDAO.getInfo( PROCESS, EnvironmentBuildProcess.class );
    }


    @Override
    public Environment buildEnvironment( final EnvironmentBuildProcess process ) throws EnvironmentBuildException
    {
        EnvironmentBuilder environmentBuilder = new EnvironmentBuilderImpl( this );
        try
        {
            EnvironmentBlueprint blueprint = environmentDAO.getBlueprint( process.getBlueprintId() );
            if ( blueprint == null )
            {
                throw new EnvironmentBuildException( "Blueprint not found..." );
            }
            Environment environment = environmentBuilder.build( blueprint, process );
            saveEnvironment( environment );

            configureSshBetweenContainers( blueprint, environment.getContainers() );
            configureLinkingHostsBetweenContainers( blueprint, environment.getContainers() );

            /*process.setProcessStatusEnum( ProcessStatusEnum.SUCCESSFUL );
            saveBuildProcess( process );*/

            return environment;
        }
        catch ( EnvironmentPersistenceException | BuildException | EnvironmentConfigureException e )
        {
            throw new EnvironmentBuildException( e.getMessage() );
        }
        catch ( EnvironmentManagerException e )
        {
            throw new EnvironmentBuildException( e.getMessage() );
        }
    }


    private void configureLinkingHostsBetweenContainers( EnvironmentBlueprint blueprint,
                                                         final Set<ContainerHost> containers )
            throws EnvironmentConfigureException
    {
        if ( blueprint.isExchangeSshKeys() )
        {
            try
            {
                securityManager.configSshOnAgents( containers );
            }
            catch ( SecurityManagerException e )
            {
                LOG.error( e.getMessage() );
                throw new EnvironmentConfigureException( e.getMessage() );
            }
        }
    }


    private void configureSshBetweenContainers( EnvironmentBlueprint blueprint, final Set<ContainerHost> containers )
            throws EnvironmentConfigureException
    {
        if ( blueprint.isLinkHosts() )
        {
            try
            {
                securityManager.configHostsOnAgents( containers, blueprint.getDomainName() );
            }
            catch ( SecurityManagerException e )
            {
                throw new EnvironmentConfigureException( e.getMessage() );
            }
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
    public UUID saveBuildProcess( final TopologyData topologyData ) throws EnvironmentManagerException
    {
        EnvironmentBuildProcessFactory factory = null;
        if ( topologyData instanceof Blueprint2PeerData )
        {
            factory = new Blueprint2PeerBuilder( this );
        }
        else if ( topologyData instanceof Blueprint2PeerGroupData )
        {
            factory = new Blueprint2PeerGroupBuilder( this );
        }
        else if ( topologyData instanceof Node2PeerData )
        {
            factory = new Node2PeerBuilder( this );
        }
        else if ( topologyData instanceof NodeGroup2PeerData )
        {
            factory = new NodeGroup2PeerBuilder( this );
        }
        else if ( topologyData instanceof NodeGroup2PeerGroupData )
        {
            factory = new NodeGroup2PeerGroupBuilder( this );
        }

        try
        {
            EnvironmentBuildProcess process = factory.prepareBuildProcess( topologyData );
            environmentDAO.saveInfo( PROCESS, process.getId().toString(), process );
            return process.getId();
        }
        catch ( ProcessBuilderException e )
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
            throw new EnvironmentBuildException( "Could not get installation template data" );
        }

        UUID peerId = peerManager.getLocalPeer().getId();

        if ( peerId == null )
        {
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
                            nodeGroup.getPlacementStrategy().getStrategyId(),
                            nodeGroup.getPlacementStrategy().getCriteriaAsList(), nodeGroup.getName() );
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
                throw new EnvironmentBuildException( String.format( "FAILED create container on %s", peer.getId() ) );
            }
            saveEnvironment( environment );
        }
        catch ( PeerException e )
        {
            throw new EnvironmentBuildException( e.getMessage() );
        }
        catch ( EnvironmentManagerException e )
        {
            throw new EnvironmentBuildException( e.getMessage() );
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
