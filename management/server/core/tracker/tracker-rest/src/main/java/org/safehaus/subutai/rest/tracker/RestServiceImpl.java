package org.safehaus.subutai.rest.tracker;


import java.util.Date;
import java.util.UUID;

import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.shared.operation.ProductOperationView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 *
 */

public class RestServiceImpl implements RestService {

    private static final Gson gson =
            new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
    private Tracker tracker;


    public void setTracker( Tracker tracker ) {
        this.tracker = tracker;
    }


    @Override
    public String getProductOperation( final String source, final UUID uuid ) {
        ProductOperationView productOperationView = tracker.getProductOperation( source, uuid );
        if ( productOperationView != null ) {
            return gson.toJson( productOperationView );
        }
        return null;
    }


    @Override
    public String getProductOperations( final String source, final Date fromDate, final Date toDate, final int limit ) {
        return gson.toJson( tracker.getProductOperations( source, fromDate, toDate, limit ) );
    }


    @Override
    public String getProductOperationSources() {
        return gson.toJson( tracker.getProductOperationSources() );
    }
}
