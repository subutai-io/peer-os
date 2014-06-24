package org.safehaus.subutai.impl.manager.dao;


import java.util.List;

import org.safehaus.subutai.api.dbmanager.DbManager;
import org.safehaus.subutai.api.manager.helper.Environment;


/**
 * Created by bahadyr on 6/24/14.
 */
public class EnvironmentDAO {

    DbManager dbManager;


    public EnvironmentDAO( final DbManager dbManager ) {
        this.dbManager = dbManager;
    }


    public List<Environment> getEnvironments() {
        List<Environment> environments = dbManager.getEnvironmentInfo( "ENV", Environment.class );
        return environments;
    }


    public Environment getEnvironment( final String environmentName ) {
        Environment environment = dbManager.getEnvironmentInfo( "ENV", environmentName, Environment.class );
        return environment;
    }


    public boolean saveEnvironment( final Environment environment ) {
        dbManager.saveEnvironmentInfo( "ENV", environment.getName(), environment );
        return true;
    }
}
