package org.safehaus.subutai.impl.manager.dao;


import java.util.Set;

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


    public Set<Environment> getEnvironments() {
        //TODO call database manager to retvieve list of environments data

        return null;
    }


    public Environment getEnvironment( final String environmentName ) {

        //TODO call database manager to retrive environment by it's name
        return null;
    }


    public boolean saveEnvironment( final Environment environment ) {
        //TODO call database manager to save environment into cassandra
        dbManager.saveEnvironmentInfo( "ENV", environment.getName(), environment );
        return true;
    }
}
