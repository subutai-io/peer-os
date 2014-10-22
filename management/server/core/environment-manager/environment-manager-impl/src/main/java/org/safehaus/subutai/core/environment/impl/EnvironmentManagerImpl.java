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

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.CloneContainersMessage;
import org.safehaus.subutai.common.protocol.Container;
import org.safehaus.subutai.common.protocol.DefaultCommandMessage;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.EnvironmentBuildTask;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.common.protocol.PeerCommandMessage;
import org.safehaus.subutai.common.protocol.PeerCommandType;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.container.api.container.ContainerManager;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.TopologyEnum;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentPersistenceException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentContainer;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentStatusEnum;
import org.safehaus.subutai.core.environment.impl.builder.EnvironmentBuilder;
import org.safehaus.subutai.core.environment.impl.builder.TopologyBuilder;
import org.safehaus.subutai.core.environment.impl.dao.EnvironmentDAO;
import org.safehaus.subutai.core.network.api.NetworkManager;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerContainer;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.command.dispatcher.api.PeerCommandDispatcher;
import org.safehaus.subutai.core.peer.command.dispatcher.api.PeerCommandException;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;


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
    private static final long TIMEOUT = 1000 * 15;

    private EnvironmentDAO environmentDAO;
    private EnvironmentBuilder environmentBuilder;
    private ContainerManager containerManager;
    private TemplateRegistry templateRegistry;
    private AgentManager agentManager;
    private NetworkManager networkManager;
    private PeerCommandDispatcher peerCommandDispatcher;
    private Tracker tracker;
    private DataSource dataSource;


    public EnvironmentManagerImpl( final DataSource dataSource ) throws SQLException
    {
        Preconditions.checkNotNull( dataSource, "Data source is null" );
        this.dataSource = dataSource;
    }


    public Tracker getTracker()
    {
        return tracker;
    }


    public void setTracker( final Tracker tracker )
    {
        this.tracker = tracker;
    }


    public PeerCommandDispatcher getPeerCommandDispatcher()
    {
        return peerCommandDispatcher;
    }


    public void setPeerCommandDispatcher( final PeerCommandDispatcher peerCommandDispatcher )
    {
        this.peerCommandDispatcher = peerCommandDispatcher;
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
        environmentBuilder = new EnvironmentBuilder( templateRegistry, agentManager, networkManager, containerManager );
    }


    public void destroy()
    {
        this.environmentDAO = null;
        this.environmentBuilder = null;
        this.containerManager = null;
        this.templateRegistry = null;
        this.agentManager = null;
        this.networkManager = null;
    }


    public EnvironmentDAO getEnvironmentDAO()
    {
        return environmentDAO;
    }


    public void setEnvironmentDAO( final EnvironmentDAO environmentDAO )
    {
        this.environmentDAO = environmentDAO;
    }


    public EnvironmentBuilder getEnvironmentBuilder()
    {
        return environmentBuilder;
    }


    public void setEnvironmentBuilder( final EnvironmentBuilder environmentBuilder )
    {
        this.environmentBuilder = environmentBuilder;
    }


    public ContainerManager getContainerManager()
    {
        return containerManager;
    }


    public void setContainerManager( final ContainerManager containerManager )
    {
        this.containerManager = containerManager;
    }


    public TemplateRegistry getTemplateRegistry()
    {
        return templateRegistry;
    }


    public void setTemplateRegistry( final TemplateRegistry templateRegistry )
    {
        this.templateRegistry = templateRegistry;
    }


    public AgentManager getAgentManager()
    {
        return agentManager;
    }


    public void setAgentManager( final AgentManager agentManager )
    {
        this.agentManager = agentManager;
    }


    public NetworkManager getNetworkManager()
    {
        return networkManager;
    }


    public void setNetworkManager( final NetworkManager networkManager )
    {
        this.networkManager = networkManager;
    }


    /**
     * This method should be deprecated soon
     */
    @Override
    public Environment buildEnvironment( final EnvironmentBuildTask environmentBuildTask )
            throws EnvironmentBuildException
    {
        return environmentBuilder.build( environmentBuildTask );
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
    public boolean destroyEnvironment( final String uuid )
    {
        Environment environment = getEnvironment( uuid );
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
        return environmentDAO.deleteInfo( ENVIRONMENT, uuid );
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
        environmentDAO.saveInfo( ENVIRONMENT, environment.getUuid().toString(), environment );
    }


    @Override
    public boolean saveBuildProcess( final EnvironmentBuildProcess buildProgress )
    {
        return environmentDAO.saveInfo( PROCESS, buildProgress.getUuid().toString(), buildProgress );
    }


    @Override
    public List<EnvironmentBuildProcess> getBuildProcesses()
    {
        return environmentDAO.getInfo( PROCESS, EnvironmentBuildProcess.class );
    }


    @Override
    public void buildEnvironment( final EnvironmentBuildProcess process ) throws EnvironmentBuildException
    {
        try
        {
            EnvironmentBlueprint blueprint = environmentDAO.getBlueprint( process.getBlueprintUUID() );

            Environment environment = new Environment( blueprint.getName() );
            saveEnvironment( environment );
            TrackerOperation operation = tracker.createTrackerOperation( environment.getName(), environment.getName() );

            int containerCount = 0;
            long timeout = 1000 * 60;
            for ( String key : process.getMessageMap().keySet() )
            {
                CloneContainersMessage ccm = process.getMessageMap().get( key );
                ccm.setEnvId( environment.getUuid() );

                containerCount = containerCount + ccm.getNumberOfNodes();
                timeout = 1000 * 30 * ccm.getNumberOfNodes();

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

                UUID peerId = getPeerId();
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
                    operation.addLog( String.format( "sending clone command: %s", ccm.toString() ) );
                    peerCommandDispatcher.invoke( ccm, timeout );
                }
                catch ( PeerCommandException e )
                {
                    LOG.error( e.getMessage(), e );
                    operation.addLogFailed( "Error occured while invoking command." );
                    environment.setStatus( EnvironmentStatusEnum.BROKEN );
                    saveEnvironment( environment );
                    throw new EnvironmentBuildException( e.getMessage() );
                }

                boolean result = ccm.isSuccess();
                if ( result )
                {
                    Set<Agent> agents = ( Set<Agent> ) ccm.getResult();
                    if ( !agents.isEmpty() )
                    {
                        for ( Agent agent : agents )
                        {
                            EnvironmentContainer container = new EnvironmentContainer();

                            container.setPeerId( agent.getSiteId() );
                            container.setAgentId( agent.getUuid() );
                            container.setIps( agent.getListIP() );
                            container.setHostname( agent.getHostname() );
                            container.setDescription( ccm.getTemplate() + " agent " + agent.getEnvironmentId() );
                            container.setName( agent.getHostname() );
                            container.setTemplateName( ccm.getTemplate() );

                            environment.addContainer( container );
                        }
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

            if ( environment.getContainers().isEmpty() )
            {
                environment.setStatus( EnvironmentStatusEnum.EMPTY );
                saveEnvironment( environment );
                throw new EnvironmentBuildException( "No containers assigned to the Environment" );
            }
            else
            {
                Set<Container> containers = Sets.newHashSet();
                containers.addAll( environment.getContainers() );

                if ( blueprint.isExchangeSshKeys() )
                {
                    networkManager.configSsh( containers );
                }
                if ( blueprint.isLinkHosts() )
                {
                    networkManager.configHosts( blueprint.getDomainName(), containers );
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
                operation.addLogDone( "Complete" );
            }
        }
        catch ( EnvironmentPersistenceException e )
        {
            throw new EnvironmentBuildException( e.getMessage() );
        }
    }


    // TODO: Implement it via PCD
    private UUID getPeerId()
    {

        try
        {
            PeerManager peerManager = ServiceLocator.getServiceNoCache( PeerManager.class );
            return peerManager.getSiteId();
        }
        catch ( NamingException e )
        {
            LOG.error( e.getMessage(), e );
        }
        return null;
    }


    @Override
    public void deleteBuildProcess( final EnvironmentBuildProcess environmentBuildProcess )
    {
        environmentDAO.deleteInfo( PROCESS, environmentBuildProcess.getUuid().toString() );
    }


    @Override
    public void invoke( final PeerCommandMessage commandMessage )
    {
        peerCommandDispatcher.invoke( commandMessage );
    }


    @Override
    public void invoke( final PeerCommandMessage commandMessage, long timeout )
    {
        peerCommandDispatcher.invoke( commandMessage, timeout );
    }


    @Override
    public Set<EnvironmentContainer> getConnectedContainers( final Environment environment )
    {

        Set<UUID> peers = new HashSet<>();
        for ( EnvironmentContainer ec : environment.getContainers() )
        {
            peers.add( ec.getPeerId() );
        }

        Set<EnvironmentContainer> freshContainers = new HashSet<>();
        for ( UUID peerId : peers )
        {
            PeerCommandMessage cmd =
                    new DefaultCommandMessage( PeerCommandType.GET_CONNECTED_CONTAINERS, environment.getUuid(), peerId,
                            null );

            peerCommandDispatcher.invoke( cmd, TIMEOUT );

            Set<PeerContainer> containers =
                    JsonUtil.fromJson( ( String ) cmd.getResult(), new TypeToken<Set<PeerContainer>>()
                    {
                    }.getType() );

            if ( cmd.isSuccess() && containers != null )
            {
                for ( Container c : containers )
                {
                    EnvironmentContainer ec = new EnvironmentContainer();
                    ec.setEnvironmentId( environment.getUuid() );

                    ec.setAgentId( c.getAgentId() );
                    ec.setPeerId( c.getPeerId() );
                    freshContainers.add( ec );
                }
            }
        }
        return freshContainers;
    }


    @Override
    public Environment getEnvironmentByUUID( final UUID environmentId )
    {
        Environment environment = environmentDAO.getInfo( ENVIRONMENT, environmentId.toString(), Environment.class );

        environmentBuilder.convertEnvironmentContainersToNodes( environment );

        return environment;
    }


    @Override
    public boolean saveBuildProcess( final UUID blueprintId, final Map<Object, Peer> topology,
                                     final Map<Object, NodeGroup> map, TopologyEnum topologyEnum )
    {
        EnvironmentBuildProcess process = null;
        TopologyBuilder topologyBuilder = new TopologyBuilder();
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
            return environmentDAO.saveInfo( PROCESS, process.getUuid().toString(), process );
        }
        else
        {
            return false;
        }
    }
}
