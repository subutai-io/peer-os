/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.environment.impl;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.safehaus.subutai.common.protocol.CloneContainersMessage;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.EnvironmentBuildTask;
import org.safehaus.subutai.common.protocol.PeerCommand;
import org.safehaus.subutai.common.protocol.PeerCommandMessage;
import org.safehaus.subutai.common.protocol.PeerCommandType;
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
import org.safehaus.subutai.core.environment.impl.util.BlueprintParser;
import org.safehaus.subutai.core.network.api.NetworkManager;
import org.safehaus.subutai.core.peer.command.dispatcher.api.PeerCommandDispatcher;
import org.safehaus.subutai.core.peer.command.dispatcher.api.PeerCommandException;
import org.safehaus.subutai.core.registry.api.TemplateRegistryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;


/**
 * This is an implementation of EnvironmentManager
 */
public class EnvironmentManagerImpl implements EnvironmentManager
{

    private static final Logger LOG = LoggerFactory.getLogger( EnvironmentManagerImpl.class.getName() );
    private static final String ENVIRONMENT = "ENVIRONMENT";
    private static final String PROCESS = "PROCESS";
    private static final String BLUEPRINT = "BLUEPRINT";
    private EnvironmentDAO environmentDAO;
    private EnvironmentBuilder environmentBuilder;
    private BlueprintParser blueprintParser;
    private ContainerManager containerManager;
    private TemplateRegistryManager templateRegistryManager;
    private AgentManager agentManager;
    private NetworkManager networkManager;
    private DbManager dbManager;
    private PeerCommandDispatcher peerCommandDispatcher;
    private Set<EnvironmentContainer> containers = new HashSet<>();


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
        this.blueprintParser = new BlueprintParser();
        this.environmentDAO = new EnvironmentDAO( dbManager );
        environmentBuilder = new EnvironmentBuilder( templateRegistryManager, agentManager, networkManager );
    }


    public void destroy()
    {
        this.environmentDAO = null;
        this.environmentBuilder = null;
        this.blueprintParser = null;
        this.containerManager = null;
        this.templateRegistryManager = null;
        this.agentManager = null;
        this.networkManager = null;
        this.dbManager = null;
        //        this.peerManager = null;
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


    public BlueprintParser getBlueprintParser()
    {
        return blueprintParser;
    }


    public void setBlueprintParser( final BlueprintParser blueprintParser )
    {
        this.blueprintParser = blueprintParser;
    }


    public ContainerManager getContainerManager()
    {
        return containerManager;
    }


    public void setContainerManager( final ContainerManager containerManager )
    {
        this.containerManager = containerManager;
    }


    public TemplateRegistryManager getTemplateRegistryManager()
    {
        return templateRegistryManager;
    }


    public void setTemplateRegistryManager( final TemplateRegistryManager templateRegistryManager )
    {
        this.templateRegistryManager = templateRegistryManager;
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
            EnvironmentBlueprint blueprint = blueprintParser.parseEnvironmentBlueprintText( blueprintStr );

            EnvironmentBuildTask environmentBuildTask = new EnvironmentBuildTask();
            environmentBuildTask.setEnvironmentBlueprint( blueprint );

            return environmentDAO
                    .saveInfo( BLUEPRINT, environmentBuildTask.getUuid().toString(), environmentBuildTask );
        }
        catch ( JsonSyntaxException e )
        {
            LOG.error( e.getMessage(), e );
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
    public String parseBlueprint( final EnvironmentBlueprint blueprint )
    {
        return blueprintParser.parseEnvironmentBlueprint( blueprint );
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


        Environment environment = new Environment( "environment", environmentBuildProcess.getUuid() );
        for ( CloneContainersMessage ccm : environmentBuildProcess.getCloneContainersMessages() )
        {

            PeerCommand peerCommand = new PeerCommand( PeerCommandType.CLONE, ccm );
            try
            {
                boolean result = peerCommandDispatcher.invoke( peerCommand );
                if ( result )
                {

                    //TODO: Assign data from set of agents received

                    EnvironmentContainer container = new EnvironmentContainer();
                    container.setPeerId( ccm.getPeerId() );
                    //                    container.setAgentId(  );
                    //                    container.setHostname(  );
                    container.setDescription( ccm.getTemplate() );
                    container.setName( ccm.getTemplate() );
                    environment.addContainer( container );
                }
            }
            catch ( PeerCommandException e )
            {
                LOG.error( e.getMessage(), e );
                throw new EnvironmentBuildException( e.getMessage() );
            }
            saveEnvironment( environment );
        }
    }


    @Override
    public void deleteBuildProcess( final EnvironmentBuildProcess environmentBuildProcess )
    {
        environmentDAO.deleteInfo( PROCESS, environmentBuildProcess.getUuid().toString() );
    }


    @Override
    public Set<EnvironmentContainer> getContainers()
    {
        return containers;
    }


    @Override
    public void addContainer( final EnvironmentContainer container )
    {
        if ( container == null )
        {
            throw new IllegalArgumentException( "Environment container could not be null." );
        }

        container.setEnvironmentManager( this );
        containers.add( container );
    }


    @Override
    public boolean startContainer( final EnvironmentContainer container )
    {
        PeerCommand peerCommand = new PeerCommand( PeerCommandType.START,
                new PeerCommandMessage( container.getPeerId(), container.getAgentId() ) );
        return peerCommandDispatcher.invoke( peerCommand );
    }


    @Override
    public boolean stopContainer( final EnvironmentContainer container )
    {
        PeerCommand peerCommand = new PeerCommand( PeerCommandType.STOP,
                new PeerCommandMessage( container.getPeerId(), container.getAgentId() ) );
        return peerCommandDispatcher.invoke( peerCommand );
    }


    @Override
    public boolean isContainerConnected( final EnvironmentContainer container )
    {
        PeerCommand peerCommand = new PeerCommand( PeerCommandType.ISCONNECTED,
                new PeerCommandMessage( container.getPeerId(), container.getAgentId() ) );
        return peerCommandDispatcher.invoke( peerCommand );
    }
}
