package org.safehaus.subutai.core.environment.impl;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.dao.DaoManager;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.peer.HostInfoModel;
import org.safehaus.subutai.common.peer.Peer;
import org.safehaus.subutai.common.peer.PeerException;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.EnvironmentBuildTask;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentDestroyException;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentManagerException;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentPersistenceException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;
import org.safehaus.subutai.core.environment.api.topology.Blueprint2PeerData;
import org.safehaus.subutai.core.environment.api.topology.TopologyData;
import org.safehaus.subutai.core.environment.impl.builder.BuildProcessFactory;
import org.safehaus.subutai.core.environment.impl.builder.EnvironmentBuildProcessBuilder;
import org.safehaus.subutai.core.environment.impl.builder.Node2PeerBuilder;
import org.safehaus.subutai.core.environment.impl.builder.ProcessBuilderException;
import org.safehaus.subutai.core.environment.impl.dao.EnvironmentContainerDataService;
import org.safehaus.subutai.core.environment.impl.dao.EnvironmentDAO;
import org.safehaus.subutai.core.environment.impl.dao.EnvironmentDataService;
import org.safehaus.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import org.safehaus.subutai.core.environment.impl.entity.EnvironmentImpl;
import org.safehaus.subutai.core.environment.impl.environment.BuildException;
import org.safehaus.subutai.core.environment.impl.environment.DestroyException;
import org.safehaus.subutai.core.environment.impl.environment.EnvironmentBuilder;
import org.safehaus.subutai.core.environment.impl.environment.EnvironmentBuilderImpl;
import org.safehaus.subutai.core.environment.impl.environment.EnvironmentDestroyer;
import org.safehaus.subutai.core.environment.impl.environment.EnvironmentDestroyerImpl;
import org.safehaus.subutai.core.environment.impl.net.NetworkSetup;
import org.safehaus.subutai.core.network.api.NetworkManager;
import org.safehaus.subutai.core.network.api.NetworkManagerException;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.ResourceHost;
import org.safehaus.subutai.core.peer.api.ResourceHostException;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;
import org.safehaus.subutai.core.security.api.SecurityManager;
import org.safehaus.subutai.core.security.api.SecurityManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;


/**
 * This is an implementation of EnvironmentManager
 */
public class EnvironmentManagerImpl implements EnvironmentManager
{

    private static final Logger LOG = LoggerFactory.getLogger( EnvironmentManagerImpl.class.getName() );
    private static final String PROCESS = "PROCESS";
    private static final String BLUEPRINT = "BLUEPRINT";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private PeerManager peerManager;
    private NetworkManager networkManager;
    private EnvironmentDAO environmentDAO;
    private TemplateRegistry templateRegistry;
    private SecurityManager securityManager;
    private EnvironmentDataService environmentDataService;
    private EnvironmentContainerDataService environmentContainerDataService;
    private DaoManager daoManager;


    public void init() throws EnvironmentManagerException
    {
        try
        {
            this.environmentDAO = new EnvironmentDAO( daoManager );
            this.environmentDataService = new EnvironmentDataService( daoManager.getEntityManagerFactory() );
            this.environmentContainerDataService =
                    new EnvironmentContainerDataService( daoManager.getEntityManagerFactory() );
        }
        catch ( SQLException e )
        {
            LOG.error( e.getMessage(), e );
            throw new EnvironmentManagerException( e );
        }
    }


    public TemplateRegistry getTemplateRegistry()
    {
        return templateRegistry;
    }


    public void destroy()
    {

    }


    public PeerManager getPeerManager()
    {
        return peerManager;
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

            configureSshBetweenContainers( blueprint, environment.getContainerHosts() );
            configureLinkingHostsBetweenContainers( blueprint, environment.getContainerHosts() );

            setupNetwork( process, environment );
            return environment;
        }
        catch ( EnvironmentPersistenceException | BuildException | EnvironmentConfigureException e )
        {
            throw new EnvironmentBuildException( e.getMessage() );
        }
    }


    @Override
    public List<Environment> getEnvironments()
    {
        List<Environment> result = new ArrayList<>();
        result.addAll( environmentDataService.getAll() );
        for ( Environment environment : result )
        {
            for ( ContainerHost containerHost : environment.getContainerHosts() )
            {
                containerHost.setPeer( getPeerManager().getPeer( containerHost.getPeerId() ) );
                containerHost.setDataService( environmentContainerDataService );
            }
        }
        return result;
    }


    @Override
    public Environment getEnvironment( final String uuid )
    {
        Environment result = environmentDataService.find( uuid );
        for ( ContainerHost containerHost : result.getContainerHosts() )
        {
            containerHost.setPeer( getPeerManager().getPeer( containerHost.getPeerId() ) );
            containerHost.setDataService( environmentContainerDataService );
        }
        return result;
    }


    @Override
    public Environment findEnvironment( final String environmentId ) throws EnvironmentManagerException
    {
        Preconditions.checkArgument( UUIDUtil.isStringAUuid( environmentId ) );

        return findEnvironmentByID( UUID.fromString( environmentId ) );
    }


    @Override
    public void destroyEnvironment( final UUID environmentId ) throws EnvironmentDestroyException
    {
        Environment environment = getEnvironmentByUUID( environmentId );
        EnvironmentDestroyer destroyer = new EnvironmentDestroyerImpl();
        try
        {
            destroyer.destroy( environment );
            environmentDataService.remove( environmentId.toString() );
        }
        catch ( DestroyException e )
        {
            LOG.error( e.getMessage(), e );
            throw new EnvironmentDestroyException( e.getMessage() );
        }
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
        try
        {
            environmentDAO.deleteInfo( BLUEPRINT, uuid );
            return true;
        }
        catch ( EnvironmentPersistenceException e )
        {
            return false;
        }
    }


    @Override
    public void deleteBlueprint( UUID blueprintId ) throws EnvironmentManagerException
    {
        try
        {
            environmentDAO.deleteBlueprint( blueprintId );
        }
        catch ( EnvironmentPersistenceException e )
        {
            LOG.error( e.getMessage(), e );
            throw new EnvironmentManagerException( e );
        }
    }


    @Override
    public void saveEnvironment( final Environment environment )
    {
        if ( environmentDataService.find( environment.getId().toString() ) == null )
        {
            environmentDataService.persist( ( EnvironmentImpl ) environment );
        }
        else
        {
            environmentDataService.update( ( EnvironmentImpl ) environment );
        }
    }


    @Override
    public void saveBuildProcess( final EnvironmentBuildProcess buildProgress ) throws EnvironmentManagerException
    {
        try
        {
            environmentDAO.saveInfo( PROCESS, buildProgress.getId().toString(), buildProgress );
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


    private void configureLinkingHostsBetweenContainers( EnvironmentBlueprint blueprint, Set<ContainerHost> containers )
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
            throws EnvironmentManagerException
    {
        try
        {
            environmentDAO.deleteInfo( PROCESS, environmentBuildProcess.getId().toString() );
        }
        catch ( EnvironmentPersistenceException e )
        {
            throw new EnvironmentManagerException( e );
        }
    }


    @Override
    public Environment findEnvironmentByID( final UUID environmentId ) throws EnvironmentManagerException
    {
        Environment result = environmentDataService.find( environmentId.toString() );
        if ( result == null )
        {
            throw new EnvironmentManagerException( "Environment not found" );
        }
        for ( ContainerHost containerHost : result.getContainerHosts() )
        {
            containerHost.setPeer( getPeerManager().getPeer( containerHost.getPeerId() ) );
            containerHost.setDataService( environmentContainerDataService );
        }
        return result;
    }


    @Override
    public Environment getEnvironmentByUUID( final UUID environmentId )
    {
        Environment result = environmentDataService.find( environmentId.toString() );
        for ( ContainerHost containerHost : result.getContainerHosts() )
        {
            containerHost.setPeer( getPeerManager().getPeer( containerHost.getPeerId() ) );
            containerHost.setDataService( environmentContainerDataService );
        }
        return result;
    }


    @Override
    public UUID saveBuildProcess( final TopologyData topologyData ) throws EnvironmentManagerException
    {
        EnvironmentBuildProcessBuilder builder = BuildProcessFactory.newBuilder( topologyData, this );
        try
        {
            EnvironmentBuildProcess process = builder.prepareBuildProcess( topologyData );
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
    public void createLocalContainer( final Environment environment, final String templateName,
                                      final String nodeGroupName, ResourceHost resourceHost )
            throws EnvironmentBuildException
    {
        Preconditions.checkNotNull( environment );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateName ) );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( nodeGroupName ) );
        Preconditions.checkNotNull( resourceHost );


        //obtain free name
        String containerName = peerManager.getLocalPeer().getFreeHostName( templateName );

        //clone container
        try
        {
            resourceHost.cloneContainer( templateName, containerName );
        }
        catch ( ResourceHostException e )
        {
            throw new EnvironmentBuildException( e.getMessage() );
        }

        //wait container
        int timeout = 180;
        long start = System.currentTimeMillis();
        ContainerHost containerHost = null;
        while ( start + timeout * 1000 > System.currentTimeMillis() && containerHost == null )
        {
            try
            {
                Thread.sleep( 100 );
            }
            catch ( InterruptedException ignore )
            {

            }

            containerHost = resourceHost.getContainerHostByName( containerName );
        }

        //container connection timed out
        if ( containerHost == null )
        {
            throw new EnvironmentBuildException( "Container has not connected within wait interval" );
        }

        //construct host entity
        HostInfoModel hostInfoModel = new HostInfoModel( containerHost );
        EnvironmentContainerImpl environmentContainer =
                new EnvironmentContainerImpl( peerManager.getLocalPeer().getId(), nodeGroupName, hostInfoModel );

        //add container to environment
        environment.addContainer( environmentContainer );

        //save environment
        saveEnvironment( environment );
    }


    @Override
    public void createAdditionalContainers( final UUID id, final String ngJson, final Peer peer )
            throws EnvironmentBuildException
    {
        createAdditionalContainers( id, GSON.fromJson( ngJson, NodeGroup.class ), peer );
    }


    @Override
    public void createAdditionalContainers( final UUID id, final NodeGroup nodeGroup, final Peer peer )
            throws EnvironmentBuildException
    {
        Environment environment = null;
        try
        {
            environment = findEnvironmentByID( id );
        }
        catch ( EnvironmentManagerException e )
        {
            throw new EnvironmentBuildException( e.getMessage() );
        }

        List<Template> templatesData = Lists.newArrayList();

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
            Set<HostInfoModel> hostInfoModels = peerManager.getPeer( peer.getId() ).
                    scheduleCloneContainers( peerId, templatesData, nodeGroup.getNumberOfNodes(),
                            nodeGroup.getPlacementStrategy().getStrategyId(),
                            nodeGroup.getPlacementStrategy().getCriteriaAsList() );
            if ( !hostInfoModels.isEmpty() )
            {
                for ( HostInfoModel hostInfoModel : hostInfoModels )
                {
                    EnvironmentContainerImpl environmentContainer =
                            new EnvironmentContainerImpl( peer.getId(), nodeGroup.getName(), hostInfoModel );
                    environment.addContainer( environmentContainer );
                }
            }
            else
            {
                throw new EnvironmentBuildException( String.format( "FAILED create container on %s", peer.getId() ) );
            }
            saveEnvironment( environment );
        }
        catch ( PeerException | EnvironmentBuildException e )
        {
            throw new EnvironmentBuildException( e.getMessage() );
        }
    }


    @Override
    public UUID addContainer( final UUID environmentId, final String template, PlacementStrategy strategy,
                              String nodeGroupName, final Peer peer ) throws EnvironmentManagerException
    {
        EnvironmentBuildProcessBuilder builder = new Node2PeerBuilder( this );
        try
        {

            List<Template> templates = builder.fetchRequiredTemplates( peer.getId(), template );

            Set<HostInfoModel> hosts = peerManager.getPeer( peer.getId() )
                                                  .scheduleCloneContainers( peerManager.getLocalPeer().getId(),
                                                          templates, 1, strategy.getStrategyId(),
                                                          strategy.getCriteriaAsList() );

            if ( hosts.isEmpty() )
            {
                throw new EnvironmentManagerException( "Containers not created" );
            }
            else
            {
                EnvironmentContainerImpl newHost =
                        new EnvironmentContainerImpl( peer.getId(), nodeGroupName, hosts.iterator().next() );
                Environment environment = getEnvironmentByUUID( environmentId );
                environment.addContainer( newHost );
                saveEnvironment( environment );
                return newHost.getId();
            }
        }
        catch ( ProcessBuilderException | PeerException e )
        {
            LOG.error( e.getMessage(), e );
            throw new EnvironmentManagerException( e.getMessage() );
        }
    }


    @Override
    public void destroyContainer( final UUID containerId ) throws EnvironmentManagerException
    {
        ContainerHost targetHost = null;
        Environment targetEnvironment = null;
        search:
        for ( Environment environment : getEnvironments() )
        {
            for ( ContainerHost containerHost : environment.getContainerHosts() )
            {
                if ( containerHost.getId().equals( containerId ) )
                {
                    targetEnvironment = environment;
                    targetHost = containerHost;
                    break search;
                }
            }
        }

        if ( targetHost == null )
        {
            throw new EnvironmentManagerException( "Container not found" );
        }
        try
        {
            targetHost.dispose();
            targetEnvironment.removeContainer( targetHost );
            saveEnvironment( targetEnvironment );
        }
        catch ( PeerException e )
        {
            throw new EnvironmentManagerException( e.getMessage() );
        }
    }


    @Override
    public void removeContainer( final UUID environmentId, final UUID hostId ) throws EnvironmentManagerException
    {
        Environment environment = getEnvironmentByUUID( environmentId );
        ContainerHost host = environment.getContainerHostById( hostId );
        try
        {
            host.dispose();
            environment.removeContainer( host );
            saveEnvironment( environment );
        }
        catch ( PeerException e )
        {
            throw new EnvironmentManagerException( e.getMessage() );
        }
    }


    public EnvironmentContainerDataService getEnvironmentContainerDataService()
    {
        return environmentContainerDataService;
    }


    private void setupNetwork( EnvironmentBuildProcess process, Environment env )
    {
        NetworkSetup net = new NetworkSetup( process );
        net.setEnvironmentManager( this );
        net.setNetworkManager( networkManager );
        net.setPeerManager( peerManager );

        try
        {
            net.setupN2Nconnections( process.getN2nConnection(), process.getKeyFilePath() );
            net.setupTunnels( process.getTunnel().getTunnelName() );
            net.setupGateways( env );
            net.setupVniVlanMappings( process.getTunnel().getTunnelName(), env );
            net.setupGatewaysOnContainers( env );
            net.setupContainerIpAddresses( env );
        }
        catch ( NetworkManagerException ex )
        {
            LOG.error( "[*] Networking setup failed!", ex );
        }
    }


    public void setDaoManager( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    public void setTemplateRegistry( final TemplateRegistry templateRegistry )
    {
        this.templateRegistry = templateRegistry;
    }


    public void setSecurityManager( final SecurityManager securityManager )
    {
        this.securityManager = securityManager;
    }


    public void setPeerManager( PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    public void setNetworkManager( NetworkManager networkManager )
    {
        this.networkManager = networkManager;
    }


    public void setEnvironmentDAO( final EnvironmentDAO environmentDAO )
    {
        this.environmentDAO = environmentDAO;
    }
}

