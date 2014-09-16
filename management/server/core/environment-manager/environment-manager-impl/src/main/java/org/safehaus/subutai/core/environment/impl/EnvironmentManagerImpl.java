/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.environment.impl;


import java.util.List;
import java.util.logging.Logger;

import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.EnvironmentBuildTask;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.container.api.container.ContainerManager;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentDestroyException;
import org.safehaus.subutai.core.environment.api.helper.BuildProcess;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.environment.api.helper.LxcBuildMessage;
import org.safehaus.subutai.core.environment.impl.builder.EnvironmentBuilder;
import org.safehaus.subutai.core.environment.impl.dao.EnvironmentDAO;
import org.safehaus.subutai.core.environment.impl.util.BlueprintParser;
import org.safehaus.subutai.core.network.api.NetworkManager;
import org.safehaus.subutai.core.registry.api.TemplateRegistryManager;

import com.google.common.base.Strings;
import com.google.gson.JsonSyntaxException;


/**
 * This is an implementation of EnvironmentManager
 */
public class EnvironmentManagerImpl implements EnvironmentManager {

    private final Logger LOG = Logger.getLogger( EnvironmentManagerImpl.class.getName() );

    private String ENVIRONMENT = "ENVIRONMENT";
    private String BLUEPRINT = "BLUEPRINT";

    private EnvironmentDAO environmentDAO;
    private EnvironmentBuilder environmentBuilder;
    private BlueprintParser blueprintParser;
    private ContainerManager containerManager;
    private TemplateRegistryManager templateRegistryManager;
    private AgentManager agentManager;
    private NetworkManager networkManager;
    private DbManager dbManager;


    public EnvironmentManagerImpl() {
    }


    public void init() {
        this.blueprintParser = new BlueprintParser();
        this.environmentDAO = new EnvironmentDAO( dbManager );
        environmentBuilder = new EnvironmentBuilder( templateRegistryManager, agentManager, networkManager );
    }


    public void destroy() {
        this.environmentDAO = null;
        this.environmentBuilder = null;
        this.blueprintParser = null;
        this.containerManager = null;
        this.templateRegistryManager = null;
        this.agentManager = null;
        this.networkManager = null;
        this.dbManager = null;
    }


    public EnvironmentDAO getEnvironmentDAO() {
        return environmentDAO;
    }


    public void setEnvironmentDAO( final EnvironmentDAO environmentDAO ) {
        this.environmentDAO = environmentDAO;
    }


    public EnvironmentBuilder getEnvironmentBuilder() {
        return environmentBuilder;
    }


    public void setEnvironmentBuilder( final EnvironmentBuilder environmentBuilder ) {
        this.environmentBuilder = environmentBuilder;
    }


    public BlueprintParser getBlueprintParser() {
        return blueprintParser;
    }


    public void setBlueprintParser( final BlueprintParser blueprintParser ) {
        this.blueprintParser = blueprintParser;
    }


    public ContainerManager getContainerManager() {
        return containerManager;
    }


    public void setContainerManager( final ContainerManager containerManager ) {
        this.containerManager = containerManager;
    }


    public TemplateRegistryManager getTemplateRegistryManager() {
        return templateRegistryManager;
    }


    public void setTemplateRegistryManager( final TemplateRegistryManager templateRegistryManager ) {
        this.templateRegistryManager = templateRegistryManager;
    }


    public AgentManager getAgentManager() {
        return agentManager;
    }


    public void setAgentManager( final AgentManager agentManager ) {
        this.agentManager = agentManager;
    }


    public NetworkManager getNetworkManager() {
        return networkManager;
    }


    public void setNetworkManager( final NetworkManager networkManager ) {
        this.networkManager = networkManager;
    }


    public DbManager getDbManager() {
        return dbManager;
    }


    public void setDbManager( final DbManager dbManager ) {
        this.dbManager = dbManager;
    }


    public boolean buildEnvironment( EnvironmentBuildTask environmentBuildTask ) {
        LOG.info( "saved to " );
        //        return build( environmentBuildTask );
        //TODO build environment in background
        return true;
    }


    @Override
    public Environment buildEnvironmentAndReturn( final EnvironmentBuildTask environmentBuildTask )
            throws EnvironmentBuildException {

        return environmentBuilder.build( environmentBuildTask, containerManager );
    }


    @Override
    public List<Environment> getEnvironments() {
        return environmentDAO.getInfo( ENVIRONMENT, Environment.class );
    }


    @Override
    public Environment getEnvironmentInfo( final String uuid ) {
        return environmentDAO.getInfo( ENVIRONMENT, uuid, Environment.class );
    }


    @Override
    public boolean destroyEnvironment( final String uuid ) {
        Environment environment = getEnvironmentInfo( uuid );
        try
        {
            environmentBuilder.destroy( environment );
            return environmentDAO.deleteInfo( ENVIRONMENT, uuid );
        }
        catch ( EnvironmentDestroyException e )
        {
            LOG.info( e.getMessage() );
        }
        return false;
    }


    @Override
    public boolean saveBlueprint( String blueprintStr ) {
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
            LOG.info( e.getMessage() );
        }
        return false;
    }


    @Override
    public List<EnvironmentBuildTask> getBlueprints() {
        return environmentDAO.getInfo( BLUEPRINT, EnvironmentBuildTask.class );
    }


    @Override
    public boolean deleteBlueprint( String uuid ) {
        return environmentDAO.deleteInfo( BLUEPRINT, uuid );
    }


    @Override
    public String parseBlueprint( final EnvironmentBlueprint blueprint ) {
        return blueprintParser.parseEnvironmentBlueprint( blueprint );
    }


    @Override
    public boolean saveBuildProcess( final BuildProcess buildProgress ) {
        return environmentDAO.saveInfo( "PROCESS", buildProgress.getUuid().toString(), buildProgress );
    }


    @Override
    public List<BuildProcess> getBuildProcesses() {
        return environmentDAO.getInfo( "PROCESS", BuildProcess.class );
    }


    @Override
    public void createContainers( final LxcBuildMessage lxcBuildMessage ) {
        //        UUID uuid = lxcBuildMessage.getEnvironmentId();
        //        String templateName = lxcBuildMessage.getTemplateName();
        //        int numberOfContainers = lxcBuildMessage.getNumberOfContainers();
        //        String strategyName = lxcBuildMessage.getStrategyName();
        //        this.containerManager.clone( uuid, templateName, numberOfContainers, strategyName );
    }


    @Override
    public void saveEnvironment( final Environment environment ) {
        environmentDAO.saveInfo( ENVIRONMENT, environment.getUuid().toString(), environment );
    }


    private boolean build( EnvironmentBuildTask environmentBuildTask ) {

        if ( environmentBuildTask.getEnvironmentBlueprint().getName() != null && !Strings
                .isNullOrEmpty( environmentBuildTask.getEnvironmentBlueprint().getName() ) )
        {
            try
            {
                Environment environment = environmentBuilder.build( environmentBuildTask, containerManager );
                return environmentDAO.saveInfo( ENVIRONMENT, environment.getUuid().toString(), environment );
            }
            catch ( EnvironmentBuildException e )
            {
                LOG.info( e.getMessage() );
            }
        }
        return false;
    }
}
