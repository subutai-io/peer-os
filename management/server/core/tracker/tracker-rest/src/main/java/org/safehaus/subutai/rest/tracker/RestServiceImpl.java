package org.safehaus.subutai.rest.tracker;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.shared.operation.ProductOperationView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 *
 */

public class RestServiceImpl implements RestService {

    private static final Logger LOG = Logger.getLogger( RestServiceImpl.class.getName() );

    private static final Gson gson =
            new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
    private Tracker tracker;


    public void setTracker( Tracker tracker ) {
        this.tracker = tracker;
    }


    private Map serializeProductOperation( ProductOperationView po ) {
        Map map = new HashMap();

        map.put( "Id", po.getId() );
        map.put( "Date", po.getCreateDate() );
        map.put( "Description", po.getDescription() );
        map.put( "State", po.getState() );
        map.put( "Log", po.getLog() );

        return map;
    }


    @Override
    public String getProductOperation( final String source, final String uuid ) {
        UUID poUUID = UUID.fromString( uuid );

        ProductOperationView productOperationView = tracker.getProductOperation( source, poUUID );

        if ( productOperationView != null ) {
            return gson.toJson( serializeProductOperation( productOperationView ) );
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

            List<ProductOperationView> pos = tracker.getProductOperations( source, fromDat, toDat, limit );
            List<Map> serializedPOs = new ArrayList<>();

            for ( ProductOperationView po : pos ) {
                serializedPOs.add( serializeProductOperation( po ) );
            }

            return gson.toJson( serializedPOs );
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
