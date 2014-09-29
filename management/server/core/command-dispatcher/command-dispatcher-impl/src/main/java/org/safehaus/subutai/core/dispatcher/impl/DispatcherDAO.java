package org.safehaus.subutai.core.dispatcher.impl;


import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.core.db.api.DbManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;


/**
 * DAO for Command Dispatcher
 */
public class DispatcherDAO
{
    private static final Logger LOG = LoggerFactory.getLogger( DispatcherDAO.class.getName() );

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String COMMAND_ID_IS_NULL_MSG = "Command id is null";
    private final DbManager dbManager;


    public DispatcherDAO( final DbManager dbManager )
    {
        Preconditions.checkNotNull( dbManager, "Db Manager is null" );

        this.dbManager = dbManager;
    }


    /*
     * table remote_responses
     *
     CREATE TABLE remote_responses (
       commandid text,
       responsenumber int,
       info text,
       PRIMARY KEY (commandid, responsenumber)
     )
     *
     */


    public Set<RemoteResponse> getRemoteResponses( UUID commandId ) throws DBException
    {
        Preconditions.checkNotNull( commandId, COMMAND_ID_IS_NULL_MSG );

        Set<RemoteResponse> responses = new LinkedHashSet<>();

        try
        {
            ResultSet rs = dbManager
                    .executeQuery2( "select info from remote_responses where commandId = ?", commandId.toString() );
            if ( rs != null )
            {
                for ( Row row : rs )
                {
                    String info = row.getString( "info" );
                    responses.add( GSON.fromJson( info, RemoteResponse.class ) );
                }
            }
        }
        catch ( JsonSyntaxException ex )
        {
            LOG.error( "Error in getRemoteResponses", ex );
            throw new DBException( ex.getMessage() );
        }
        return responses;
    }


    public void saveRemoteResponse( RemoteResponse remoteResponse ) throws DBException
    {
        Preconditions.checkNotNull( remoteResponse, "Remote response is null" );

        dbManager.executeUpdate2( "insert into remote_responses(commandId, responseNumber, info) values (?,?,?)",
                remoteResponse.getCommandId().toString(),
                String.format( "%s_%s", remoteResponse.getResponse().getUuid(),
                        remoteResponse.getResponse().getResponseSequenceNumber() ), GSON.toJson( remoteResponse ) );
    }


    public void deleteRemoteResponses( UUID commandId ) throws DBException
    {
        Preconditions.checkNotNull( commandId, COMMAND_ID_IS_NULL_MSG );

        dbManager.executeUpdate2( "delete from remote_responses where commandId = ?", commandId.toString() );
    }


    public void deleteRemoteResponse( RemoteResponse remoteResponse ) throws DBException
    {
        Preconditions.checkNotNull( remoteResponse, "Remote response is null" );

        dbManager.executeUpdate2( "delete from remote_responses where commandId = ? and responseNumber = ?",
                remoteResponse.getCommandId().toString(),
                String.format( "%s_%s", remoteResponse.getResponse().getUuid(),
                        remoteResponse.getResponse().getResponseSequenceNumber() ) );
    }


    /* table remote_requests
     *
     CREATE TABLE remote_requests (
       commandid text,
       attempts int,
       info text,
       PRIMARY KEY (commandid, attempts)
     )
     *
     */


    public void saveRemoteRequest( RemoteRequest remoteRequest ) throws DBException
    {
        Preconditions.checkNotNull( remoteRequest, "Remote request is null" );

        dbManager.executeUpdate2( "insert into remote_requests(commandId,attempts,info) values (?,?,?)",
                remoteRequest.getCommandId().toString(), remoteRequest.getAttempts(), GSON.toJson( remoteRequest ) );
    }


    //workaround until we change Cassandra to another DB
    public void deleteRemoteRequest( UUID commandId, int attempts ) throws DBException
    {
        Preconditions.checkArgument( attempts >= 0, "Attempts < 0" );
        Preconditions.checkNotNull( commandId, COMMAND_ID_IS_NULL_MSG );

        dbManager.executeUpdate2( "delete from remote_requests where commandId = ? and attempts = ?",
                commandId.toString(), attempts );
    }


    public void deleteRemoteRequest( UUID commandId ) throws DBException
    {
        Preconditions.checkNotNull( commandId, COMMAND_ID_IS_NULL_MSG );

        dbManager.executeUpdate2( "delete from remote_requests where commandId = ?", commandId.toString() );
    }


    public RemoteRequest getRemoteRequest( UUID commandId ) throws DBException
    {
        Preconditions.checkNotNull( commandId, COMMAND_ID_IS_NULL_MSG );

        ResultSet rs =
                dbManager.executeQuery2( "select info from remote_requests where commandId = ?", commandId.toString() );

        if ( rs != null )
        {
            Row row = rs.one();
            if ( row != null )
            {

                String info = row.getString( "info" );
                try
                {
                    return GSON.fromJson( info, RemoteRequest.class );
                }
                catch ( JsonSyntaxException ex )
                {
                    LOG.error( "Error in getRemoteRequest", ex );

                    throw new DBException( ex.getMessage() );
                }
            }
        }

        return null;
    }


    public Set<RemoteRequest> getRemoteRequests( int attempts, int limit ) throws DBException
    {
        Preconditions.checkArgument( attempts > 0, "Attempts must be greater than 0" );
        Preconditions.checkArgument( limit > 0, "Limit must be greater than 0" );

        Set<RemoteRequest> remoteRequests = new LinkedHashSet<>();

        try
        {
            ResultSet rs = dbManager
                    .executeQuery2( "select info from remote_requests where attempts < ? limit ? allow filtering",
                            attempts, limit );
            if ( rs != null )
            {
                for ( Row row : rs )
                {
                    String info = row.getString( "info" );
                    remoteRequests.add( GSON.fromJson( info, RemoteRequest.class ) );
                }
            }
        }
        catch ( JsonSyntaxException ex )
        {
            LOG.error( "Error in getRemoteRequests", ex );

            throw new DBException( ex.getMessage() );
        }
        return remoteRequests;
    }
}
