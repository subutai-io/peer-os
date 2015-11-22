package io.subutai.core.tracker.rest.ui;


import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.subutai.common.tracker.TrackerOperationView;
import io.subutai.core.tracker.api.Tracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;


/**
 *
 */

public class RestServiceImpl implements RestService
{

    private static final Logger LOG = LoggerFactory.getLogger( RestServiceImpl.class.getName() );

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Tracker tracker;


    public RestServiceImpl( final Tracker tracker )
    {
        Preconditions.checkNotNull( tracker, "Tracker is null" );

        this.tracker = tracker;
    }


    @Override
    public Response getTrackerOperation( final String source, final String uuid )
    {
        try
        {
            UUID poUUID = UUID.fromString( uuid );

            TrackerOperationView trackerOperationView = tracker.getTrackerOperation( source, poUUID );

            if ( trackerOperationView != null )
            {
                return Response.ok().entity( GSON.toJson( trackerOperationView ) ).build();
            }
            else
            {
                return Response.status( Response.Status.NOT_FOUND ).build();
            }
        }
        catch ( NullPointerException | IllegalArgumentException e )
        {
            LOG.error( "Error in getTrackerOperation", e );
            return Response.status( Response.Status.BAD_REQUEST ).entity( e.getMessage() ).build();
        }
    }


    @Override
    public Response getTrackerOperations( final String source, final String fromDate, final String toDate,
                                          final int limit )
    {
        try
        {
            SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
            Date fromDat = df.parse( fromDate + " 00:00:00" );
            Date toDat = df.parse( toDate + " 23:59:59" );

            List<TrackerOperationView> pos = tracker.getTrackerOperations( source, fromDat, toDat, limit );

            return Response.ok().entity( GSON.toJson( pos ) ).build();
        }
        catch ( ParseException e )
        {
            LOG.error( "Error in getTrackerOperations", e );
            return Response.status( Response.Status.BAD_REQUEST ).entity( e.getMessage() ).build();
        }
    }


    @Override
    public Response getTrackerOperationSources()
    {
        return Response.ok().entity( GSON.toJson( tracker.getTrackerOperationSources() ) ).build();
    }
}
