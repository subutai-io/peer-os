package org.safehaus.subutai.accumulo.services;


import org.safehaus.subutai.api.accumulo.Accumulo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * Created by bahadyr on 5/6/14.
 */

public class RestServiceImpl implements RestService {

    private static final Gson gson =
            new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
    private Accumulo accumuloManager;


    public void setAccumuloManager( final Accumulo accumuloManager ) {
        this.accumuloManager = accumuloManager;
    }


    @Override
    public String listClusters() {
        return gson.toJson( accumuloManager.getClusters() );
    }
}