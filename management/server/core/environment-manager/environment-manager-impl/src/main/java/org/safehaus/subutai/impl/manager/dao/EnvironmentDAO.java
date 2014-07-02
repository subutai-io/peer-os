package org.safehaus.subutai.impl.manager.dao;


import java.util.List;

import org.safehaus.subutai.api.dbmanager.DbManager;
import org.safehaus.subutai.api.manager.helper.Blueprint;
import org.safehaus.subutai.api.manager.helper.Environment;


/**
 * Created by bahadyr on 6/24/14.
 */
public class EnvironmentDAO {

    DbManager dbManager;
    private String source = "ENV";


    public EnvironmentDAO( final DbManager dbManager ) {
        this.dbManager = dbManager;
    }


    public List<Environment> getEnvironments() {
        List<Environment> environments = dbManager.getEnvironmentInfo( source, Environment.class );
        return environments;
    }


    public Environment getEnvironment( final String environmentName ) {
        Environment environment = dbManager.getEnvironmentInfo( source, environmentName, Environment.class );
        return environment;
    }


    public boolean saveEnvironment( final Environment environment ) {
        dbManager.saveEnvironmentInfo( source, environment.getName(), environment );
        return true;
    }


    public boolean saveBlueprint( final Blueprint blueprint ) {
        //TODO Create table for blueprint objects
        dbManager.saveEnvironmentInfo( "BLUEPRINT", blueprint.getName(), blueprint );
        //TODO Return proper result
        return true;
    }


    public List<Blueprint> getBlueprints() {
        List<Blueprint> blueprints = dbManager.getEnvironmentInfo( "BLUEPRINT", Blueprint.class );
        return blueprints;
    }


    public boolean deleteBlueprint( final String blueprintName ) {
        dbManager.deleteInfo( "BLUEPRINT", blueprintName );
        //TODO return proper result
        return true;
    }
}
