/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.environment.impl;


import java.util.List;

import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.container.api.container.ContainerManager;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentDestroyException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.environment.impl.builder.EnvironmentBuilder;
import org.safehaus.subutai.core.environment.impl.dao.EnvironmentDAO;
import org.safehaus.subutai.core.environment.impl.util.BlueprintParser;
import org.safehaus.subutai.core.network.api.NetworkManager;
import org.safehaus.subutai.core.registry.api.TemplateRegistryManager;

import com.google.common.base.Strings;


/**
 * This is an implementation of EnvironmentManager
 */
public class EnvironmentManagerImpl implements EnvironmentManager {

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


    public EnvironmentManagerImpl(){
    }

    public void init() {
        this.blueprintParser = new BlueprintParser();
        this.environmentDAO = new EnvironmentDAO( dbManager );
        environmentBuilder = new EnvironmentBuilder( templateRegistryManager, agentManager, networkManager );
    }

    public void destroy() {

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


    /**
     * Builds an environment by provided blueprint description
     */
    @Override
    public boolean buildEnvironment( String blueprintStr ) {

        EnvironmentBlueprint blueprint = blueprintParser.parseEnvironmentBlueprintText( blueprintStr );
        return build( blueprint );
    }


    public boolean buildEnvironment( EnvironmentBlueprint blueprint ) {
        return build( blueprint );
    }


    @Override
    public Environment buildEnvironmentAndReturn( final EnvironmentBlueprint blueprint )
            throws EnvironmentBuildException {

        return environmentBuilder.build( blueprint, containerManager );
    }


    @Override
    public List<Environment> getEnvironments() {
        try {
            return environmentDAO.getInfo( ENVIRONMENT, Environment.class );
        }
        catch ( DBException e ) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public Environment getEnvironmentInfo( final String environmentName ) {
        try {
            return environmentDAO.getInfo( ENVIRONMENT, environmentName, Environment.class );
        }
        catch ( DBException e ) {
            e.printStackTrace();
        }
        return null;
        //        .getEnvironment( environmentName );
    }


    @Override
    public boolean destroyEnvironment( final String environmentName ) {
        Environment environment = getEnvironmentInfo( environmentName );
        try {
            environmentBuilder.destroy( environment );
            environmentDAO.deleteInfo( ENVIRONMENT, environmentName );
            return true;
        }
        catch ( EnvironmentDestroyException e ) {
            e.printStackTrace();
        }
        catch ( DBException e ) {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public boolean saveBlueprint( String blueprintStr ) {
        EnvironmentBlueprint blueprint = blueprintParser.parseEnvironmentBlueprintText( blueprintStr );
        try {
            environmentDAO.saveInfo( BLUEPRINT, blueprint.getName(), blueprint );
            return true;
        }
        catch ( DBException e ) {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public List<EnvironmentBlueprint> getBlueprints() {
        try {
            return environmentDAO.getInfo( BLUEPRINT, EnvironmentBlueprint.class );
        }
        catch ( DBException e ) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public boolean deleteBlueprint( String blueprintName ) {
        try {
            environmentDAO.deleteInfo( BLUEPRINT, blueprintName );
            return true;
        }
        catch ( DBException e ) {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public String parseBlueprint( final EnvironmentBlueprint blueprint ) {
        return blueprintParser.parseEnvironmentBlueprint( blueprint );
    }


    private boolean build( EnvironmentBlueprint blueprint ) {
        if ( blueprint != null && !Strings.isNullOrEmpty( blueprint.getName() ) ) {
            try {
                Environment environment = environmentBuilder.build( blueprint, containerManager );
                environmentDAO.saveInfo( ENVIRONMENT, environment.getUuid().toString(), environment );
            }
            catch ( EnvironmentBuildException e ) {
                System.out.println( e.getMessage() );
            }
            catch ( DBException e ) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
