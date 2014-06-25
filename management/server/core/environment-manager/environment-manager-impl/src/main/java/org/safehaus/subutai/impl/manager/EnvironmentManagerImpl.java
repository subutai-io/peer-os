/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.impl.manager;


import java.util.List;

import org.safehaus.subutai.api.dbmanager.DbManager;
import org.safehaus.subutai.api.manager.EnvironmentManager;
import org.safehaus.subutai.api.manager.helper.Blueprint;
import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.api.manager.util.BlueprintParser;
import org.safehaus.subutai.impl.manager.builder.EnvironmentBuilder;
import org.safehaus.subutai.impl.manager.dao.EnvironmentDAO;
import org.safehaus.subutai.impl.manager.exception.EnvironmentBuildException;
import org.safehaus.subutai.impl.manager.exception.EnvironmentInstanceDestroyException;


/**
 * This is an implementation of LxcManager
 */
public class EnvironmentManagerImpl implements EnvironmentManager {

    EnvironmentDAO environmentDAO;
    EnvironmentBuilder environmentBuilder;

    private DbManager dbManager;


    public void setDbManager( final DbManager dbManager ) {
        this.dbManager = dbManager;
        this.environmentDAO = new EnvironmentDAO( dbManager );
    }


    public EnvironmentManagerImpl() {
        this.environmentBuilder = new EnvironmentBuilder();
    }


    /**
     * Builds an environment by provided blueprint description
     */
    @Override
    public boolean buildEnvironment( String blueprintStr ) {

        Blueprint blueprint = new BlueprintParser().parseBlueprint( blueprintStr );
        if ( blueprint != null ) {
            try {
                Environment environment = environmentBuilder.build( blueprint );
                boolean saveResult = environmentDAO.saveEnvironment( environment );
                if ( !saveResult ) {
                    //rollback build action.
                    environmentBuilder.destroy( environment );
                    return false;
                }
                return true;
            }
            catch ( EnvironmentBuildException e ) {
                System.out.println( e.getMessage() );
            }
            finally {
                return false;
            }
        }
        return false;
    }


    @Override
    public List<Environment> getEnvironments() {
        System.out.println( "getEnvironments method executed..." );
        List<Environment> environments = environmentDAO.getEnvironments();
        return environments;
    }


    @Override
    public Environment getEnvironmentInfo( final String environmentName ) {
        Environment environment = environmentDAO.getEnvironment( environmentName );
        return environment;
    }


    @Override
    public boolean destroyEnvironment( final String environmentName ) {
        Environment environment = getEnvironmentInfo( environmentName );
        try {
            environmentBuilder.destroy( environment );
            //TODO environmentDAO.deleteEnvironmentInfo( environment.getName() );
            return true;
        }
        catch ( EnvironmentInstanceDestroyException e ) {
            e.printStackTrace();
        }
        return false;
    }
}
