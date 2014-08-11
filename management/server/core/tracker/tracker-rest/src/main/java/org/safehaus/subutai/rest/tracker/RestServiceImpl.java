package org.safehaus.subutai.rest.tracker;


import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    public String getProductOperation( final String source, final String uuid ) {
        UUID poUUID = UUID.fromString( uuid );
        ProductOperationView productOperationView = tracker.getProductOperation( source, poUUID );
        if ( productOperationView != null ) {
            return gson.toJson( productOperationView );
        }
        return null;
    }


    @Override
    public String getProductOperations( final String source, final String fromDate, final String toDate,
                                        final int limit ) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd" );
            Date fromDat = sdf.parse( fromDate );
            Date toDat = sdf.parse( toDate );
            return gson.toJson( tracker.getProductOperations( source, fromDat, toDat, limit ) );
        }
        catch ( ParseException e ) {
            return gson.toJson( e );
        }
    }


    @Override
    public String getProductOperationSources() {
        return gson.toJson( tracker.getProductOperationSources() );
    }
}
