package org.safehaus.subutai.core.tracker.rest;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.safehaus.subutai.common.tracker.ProductOperationView;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


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
    public Response getProductOperation( final String source, final String uuid )
    {
        try
        {
            UUID poUUID = UUID.fromString( uuid );

            ProductOperationView productOperationView = tracker.getProductOperation( source, poUUID );

            if ( productOperationView != null )
            {
                return Response.ok().entity( GSON.toJson( productOperationView ) ).build();
            }
            else
            {
                return Response.status( Response.Status.NOT_FOUND ).build();
            }
        }
        catch ( NullPointerException | IllegalArgumentException e )
        {
            LOG.error( "Error in getProductOperation", e );
            return Response.status( Response.Status.BAD_REQUEST ).entity( e.getMessage() ).build();
        }
    }


    @Override
    public Response getProductOperations( final String source, final String fromDate, final String toDate,
                                          final int limit )
    {
        try
        {
            SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
            Date fromDat = df.parse( fromDate + " 00:00:00" );
            Date toDat = df.parse( toDate + " 23:59:59" );

            List<ProductOperationView> pos = tracker.getProductOperations( source, fromDat, toDat, limit );

            return Response.ok().entity( GSON.toJson( pos ) ).build();
        }
        catch ( ParseException e )
        {
            LOG.error( "Error in getProductOperations", e );
            return Response.status( Response.Status.BAD_REQUEST ).entity( e.getMessage() ).build();
        }
    }


    @Override
    public Response getProductOperationSources()
    {
        return Response.ok().entity( GSON.toJson( tracker.getProductOperationSources() ) ).build();
    }
}
