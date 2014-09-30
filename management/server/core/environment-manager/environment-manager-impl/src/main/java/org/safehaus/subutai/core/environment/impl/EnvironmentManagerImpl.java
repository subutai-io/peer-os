/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.environment.impl;


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.CloneContainersMessage;
import org.safehaus.subutai.common.protocol.Container;
import org.safehaus.subutai.common.protocol.DefaultCommandMessage;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.EnvironmentBuildTask;
import org.safehaus.subutai.common.protocol.PeerCommandMessage;
import org.safehaus.subutai.common.protocol.PeerCommandType;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.container.api.container.ContainerManager;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.core.environment.api.EnvironmentContainer;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentDestroyException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;
import org.safehaus.subutai.core.environment.impl.builder.EnvironmentBuilder;
import org.safehaus.subutai.core.environment.impl.dao.EnvironmentDAO;
import org.safehaus.subutai.core.network.api.NetworkManager;
import org.safehaus.subutai.core.peer.api.PeerContainer;
import org.safehaus.subutai.core.peer.command.dispatcher.api.PeerCommandDispatcher;
import org.safehaus.subutai.core.peer.command.dispatcher.api.PeerCommandException;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private EnvironmentDAO environmentDAO;
    private EnvironmentBuilder environmentBuilder;
    private ContainerManager containerManager;
    private TemplateRegistry templateRegistry;
    private AgentManager agentManager;
    private NetworkManager networkManager;
    private DbManager dbManager;
    private PeerCommandDispatcher peerCommandDispatcher;
    private List<Environment> environments;
    //    private Set<EnvironmentContainer> containers = new HashSet<>();


    public EnvironmentManagerImpl()
    {
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

        this.environmentDAO = new EnvironmentDAO( dbManager );
        environmentBuilder = new EnvironmentBuilder( templateRegistry, agentManager, networkManager );

        this.environments = environmentDAO.getInfo( ENVIRONMENT, Environment.class );
    }


    public void destroy()
    {
        this.environmentDAO = null;
        this.environmentBuilder = null;
        this.containerManager = null;
        this.templateRegistry = null;
        this.agentManager = null;
        this.networkManager = null;
        this.dbManager = null;
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


    public DbManager getDbManager()
    {
        return dbManager;
    }


    public void setDbManager( final DbManager dbManager )
    {
        this.dbManager = dbManager;
    }


    public boolean buildEnvironment( EnvironmentBuildTask environmentBuildTask )
    {
        LOG.info( "saved to " );
        //        return build( environmentBuildTask );
        //TODO build environment in background


        return true;
    }


    @Override
    public Environment buildEnvironmentAndReturn( final EnvironmentBuildTask environmentBuildTask )
            throws EnvironmentBuildException
    {

        return environmentBuilder.build( environmentBuildTask, containerManager );
    }


    @Override
    public List<Environment> getEnvironments()
    {
        //        return environments;
        return environmentDAO.getInfo( ENVIRONMENT, Environment.class );
    }


    @Override
    public Environment getEnvironmentInfo( final String uuid )
    {
        return environmentDAO.getInfo( ENVIRONMENT, uuid, Environment.class );
    }


    @Override
    public boolean destroyEnvironment( final String uuid )
    {
        Environment environment = getEnvironmentInfo( uuid );
        try
        {
            environmentBuilder.destroy( environment );
            return environmentDAO.deleteInfo( ENVIRONMENT, uuid );
        }
        catch ( EnvironmentDestroyException e )
        {
            LOG.error( e.getMessage(), e );
        }
        return false;
    }


    @Override
    public boolean saveBlueprint( String blueprintStr )
    {
        try
        {
            EnvironmentBlueprint environmentBlueprint = GSON.fromJson( blueprintStr, EnvironmentBlueprint.class );
            EnvironmentBuildTask environmentBuildTask = new EnvironmentBuildTask();
            environmentBuildTask.setEnvironmentBlueprint( environmentBlueprint );

            return environmentDAO
                    .saveInfo( BLUEPRINT, environmentBuildTask.getUuid().toString(), environmentBuildTask );
        }
        catch ( JsonParseException e )
        {
            LOG.info( e.getMessage() );
        }
        return false;
    }


    @Override
    public List<EnvironmentBuildTask> getBlueprints()
    {
        return environmentDAO.getInfo( BLUEPRINT, EnvironmentBuildTask.class );
    }


    @Override
    public boolean deleteBlueprint( String uuid )
    {
        return environmentDAO.deleteInfo( BLUEPRINT, uuid );
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
    public void saveEnvironment( final Environment environment )
    {
        environmentDAO.saveInfo( ENVIRONMENT, environment.getUuid().toString(), environment );
    }


    @Override
    public void buildEnvironment( final EnvironmentBuildProcess environmentBuildProcess )
            throws EnvironmentBuildException
    {
        Environment environment = new Environment( environmentBuildProcess.getEnvironmentName() );
        for ( String key : ( Set<String> ) environmentBuildProcess.getMessageMap().keySet() )
        {
            CloneContainersMessage ccm = environmentBuildProcess.getMessageMap().get( key );

            ccm.setType( PeerCommandType.CLONE );
            try
            {
                peerCommandDispatcher.invoke( ccm );

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
                            container.setHostname( agent.getHostname() );
                            container.setDescription( ccm.getTemplate() );
                            container.setName( agent.getHostname() );
                            environment.addContainer( container );
                        }
                    }
                }
            }
            catch ( PeerCommandException e )
            {
                LOG.error( e.getMessage(), e );
                throw new EnvironmentBuildException( e.getMessage() );
            }
        }

        if ( !environment.getContainers().isEmpty() )
        {
            saveEnvironment( environment );
        }
        else
        {
            throw new EnvironmentBuildException( "No containers assigned to the Environment" );
        }
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

            peerCommandDispatcher.invoke( cmd, 1000 * 15 );

            Set<PeerContainer> containers = JsonUtil.fromJson( ( String ) cmd.getResult(), new TypeToken<Set<PeerContainer>>()
            {
            }.getType() );

            if ( cmd.isSuccess() && containers != null )
            {
                for ( Container c : containers )
                {
                    EnvironmentContainer ec = new EnvironmentContainer();
                    ec.setEnvironment( environment );
                    ec.setAgentId( c.getAgentId() );
                    ec.setPeerId( c.getPeerId() );
                    freshContainers.add( ec );
                }
            }
        }
        return freshContainers;
    }
}
