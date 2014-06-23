/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.impl.manager;


import java.util.Set;

import org.safehaus.subutai.api.manager.helper.Blueprint;
import org.safehaus.subutai.api.manager.util.BlueprintParser;
import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.api.manager.EnvironmentManager;
import org.safehaus.subutai.impl.manager.exception.EnvironmentBuildException;


/**
 * This is an implementation of LxcManager
 */
public class EnvironmentManagerImpl implements EnvironmentManager {

    EnvironmentDAO environmentDAO = new EnvironmentDAO();
    EnvironmentBuilder environmentBuilder = new EnvironmentBuilder();


    @Override
    public boolean buildEnvironment( String blueprintStr ) {

        Blueprint blueprint = new BlueprintParser().parseBlueprint( blueprintStr );

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
            e.printStackTrace();
        }
        finally {
            return false;
        }
    }


    @Override
    public Set<Environment> getEnvironments() {
        Set<Environment> environments = environmentDAO.getEnvironments();
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
        boolean destroyResult;
        if ( environmentBuilder.destroy( environment ) ) {
            destroyResult = true;
        }
        else {
            destroyResult = false;
        }
        return true;
    }
}
