package io.subutai.core.tracker.impl;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.tracker.OperationState;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.tracker.TrackerOperationView;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.tracker.api.Tracker;
import io.subutai.core.tracker.impl.dao.TrackerOperationDataService;


/**
 * This is an implementation of Tracker
 */
public class TrackerImpl implements Tracker
{

    /**
     * Used to serialize/deserialize product operation to/from json format
     */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Logger LOG = LoggerFactory.getLogger( TrackerImpl.class.getName() );
    private static final String SOURCE_IS_EMPTY_MSG = "Source is null or empty";
    protected TrackerOperationDataService dataService;
    private DaoManager daoManager;
    private IdentityManager identityManager;


    /**
     * Get view of product operation by operation id
     *
     * @param source - source of product operation, usually this is a module name
     * @param operationTrackId - id of operation
     *
     * @return - product operation view
     */
    public TrackerOperationView getTrackerOperation( String source, UUID operationTrackId )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( source ), SOURCE_IS_EMPTY_MSG );
        Preconditions.checkNotNull( operationTrackId, "Operation track id is null" );

        // @todo add enums instead of values (check for admin)
        if( identityManager.getActiveUser().getType() == 2 && identityManager.getActiveUser().getTrustLevel() == 3 )
            return dataService.getTrackerOperation( source, operationTrackId );


        return dataService.getTrackerUserOperation( source, operationTrackId, identityManager.getActiveUser().getId() );
    }


    /**
     * Saves product operation o DB
     *
     * @param source - source of product operation, usually this is a module
     * @param po - product operation
     *
     * @return - true if all went well, false otherwise
     */
    boolean saveTrackerOperation( String source, TrackerOperationImpl po )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( source ), SOURCE_IS_EMPTY_MSG );
        Preconditions.checkNotNull( po, "Tracker operation is null" );

        try
        {
            dataService.saveTrackerOperation( source, po, identityManager.getActiveUser().getId() );
            return true;
        }
        catch ( Exception e )
        {
            LOG.error( "Error in saveTrackerOperation", e );
        }

        return false;
    }


    /**
     * Creates product operation and save it to DB
     *
     * @param source - source of product operation, usually this is a module
     * @param description - description of operation
     *
     * @return - returns created product operation
     */
    public TrackerOperation createTrackerOperation( String source, String description )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( source ), SOURCE_IS_EMPTY_MSG );
        Preconditions.checkNotNull( !Strings.isNullOrEmpty( description ), "Description is null or empty" );

        TrackerOperationImpl po = new TrackerOperationImpl( source.toLowerCase(), description, this );
        if ( saveTrackerOperation( source, po ) )
        {
            return po;
        }
        return null;
    }


    /**
     * Returns list of product operations (views) filtering them by date interval
     *
     * @param source - source of product operation, usually this is a module
     * @param fromDate - beginning date of filter
     * @param toDate - ending date of filter
     * @param limit - limit of records to return
     *
     * @return - list of product operation views
     */
    public List<TrackerOperationView> getTrackerOperations( String source, Date fromDate, Date toDate, int limit )
    {
        Preconditions.checkArgument( limit > 0, "Limit must be greater than 0" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( source ), SOURCE_IS_EMPTY_MSG );
        Preconditions.checkNotNull( fromDate, "From Date is null" );
        Preconditions.checkNotNull( toDate, "To Date is null" );

        List<TrackerOperationView> list = new ArrayList<>();

        try
        {
            // @todo add enums instead of values (check for admin)
            if( identityManager.getActiveUser().getType() == 2 && identityManager.getActiveUser().getTrustLevel() == 3 )
            {
                list = dataService.getTrackerOperations( source, fromDate, toDate, limit );
            }
            else
            {
                list = dataService.getRecentUserOperations( source, fromDate, toDate, limit, identityManager.getActiveUser().getId() );
            }

        }
        catch ( SQLException | JsonSyntaxException ex )
        {
            LOG.error( "Error in getTrackerOperations", ex );
        }
        return list;
    }


    /**
     * Returns list of all sources of product operations for which product operations exist in DB
     *
     * @return list of product operation sources
     */
    public List<String> getTrackerOperationSources()
    {
        List<String> sources = new ArrayList<>();
        try
        {
            sources = dataService.getTrackerOperationSources();
        }
        catch ( SQLException e )
        {
            LOG.error( "Error in getTrackerOperationSources", e );
        }

        return sources;
    }


    /**
     * Prints log of product operation to std out stream
     *
     * @param operationTrackId - id of operation
     * @param maxOperationDurationMs - max operation duration timeout after which printing ceases
     */
    @Override
    public void printOperationLog( String source, UUID operationTrackId, long maxOperationDurationMs )
    {
        int logSize = 0;
        long startedTs = System.currentTimeMillis();
        while ( !Thread.interrupted() )
        {
            TrackerOperationView po = getTrackerOperation( source.toLowerCase(), operationTrackId );
            if ( po != null )
            {
                //print log if anything new is appended to it
                if ( logSize != po.getLog().length() )
                {
                    System.out.println( po.getLog().substring( logSize, po.getLog().length() ) );
                    logSize = po.getLog().length();
                }
                //return if operation is completed
                //or if time limit is reached
                long ts = System.currentTimeMillis() - startedTs;
                if ( po.getState() != OperationState.RUNNING || ts > maxOperationDurationMs )
                {
                    return;
                }

                try
                {
                    Thread.sleep( 100 );
                }
                catch ( InterruptedException e )
                {
                    return;
                }
            }
            else
            {
                LOG.warn( "Tracker operation not found" );
                return;
            }
        }
    }


    @Override
    public void setOperationViewState( String source, UUID operationId, boolean viewed ) throws SQLException
    {
        dataService.setOperationViewState( source, operationId, viewed );
    }

    @Override
    public void setOperationsViewStates( boolean viewed ) throws SQLException
    {
        dataService.setOperationsViewState( viewed, identityManager.getActiveUser().getId() );
    }

    @Override
    public List<TrackerOperationView> getNotifications() throws SQLException
    {
        return dataService.getNewOperations( identityManager.getActiveUser().getId() );
    }



    public void init()
    {
        dataService = new TrackerOperationDataService( daoManager.getEntityManagerFactory() );
    }


    public void setDaoManager( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }

    public void setIdentityManager( final IdentityManager identityManager )
    {
        this.identityManager = identityManager;
    }
}