package org.safehaus.subuta.environment.rest;


import java.util.logging.Logger;

import org.safehaus.subutai.core.environment.api.EnvironmentManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * Created by bahadyr on 5/6/14.
 */

public class RestServiceImpl implements RestService {

    public final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Logger LOG = Logger.getLogger( RestServiceImpl.class.getName() );
    private EnvironmentManager environmentManager;


    public RestServiceImpl() {
    }


    public EnvironmentManager getEnvironmentManager() {
        return environmentManager;
    }


    public void setEnvironmentManager( final EnvironmentManager environmentManager ) {
        this.environmentManager = environmentManager;
    }


    @Override
    public String buildNodeGroup( final String peer ) {
        return null;
    }
}