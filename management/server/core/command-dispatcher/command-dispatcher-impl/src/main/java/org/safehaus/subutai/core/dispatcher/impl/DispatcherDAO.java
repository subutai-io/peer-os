package org.safehaus.subutai.core.dispatcher.impl;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.core.db.api.DbManager;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;


/**
 * DAO for Command Dispatcher
 */
public class DispatcherDAO {
    private static final Logger LOG = Logger.getLogger( DispatcherDAO.class.getName() );

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final DbManager dbManager;


    public DispatcherDAO( final DbManager dbManager ) {
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


    public List<RemoteResponse> getRemoteResponses( UUID commandId ) throws DBException {
        Preconditions.checkNotNull( commandId, "Command Id is null" );

        List<RemoteResponse> responses = new ArrayList<>();

        try {
            ResultSet rs = dbManager
                    .executeQuery2( "select info from remote_responses where commandId = ? order by responseNumber asc",
                            commandId.toString() );
            if ( rs != null ) {
                for ( Row row : rs ) {
                    String info = row.getString( "info" );
                    responses.add( gson.fromJson( info, RemoteResponse.class ) );
                }
            }
        }
        catch ( JsonSyntaxException ex ) {
            throw new DBException( ex.getMessage() );
        }
        return responses;
    }


    public void saveRemoteResponse( RemoteResponse remoteResponse ) throws DBException {
        Preconditions.checkNotNull( remoteResponse, "Remote response is null" );

        dbManager.executeUpdate2( "insert into remote_responses(commandId,responseNumber, info) values (?,?,?)",
                remoteResponse.getCommandId().toString(), remoteResponse.getResponse().getResponseSequenceNumber(),
                gson.toJson( remoteResponse ) );
    }


    public void deleteRemoteResponse( RemoteResponse remoteResponse ) throws DBException {
        Preconditions.checkNotNull( remoteResponse, "Remote response is null" );

        dbManager.executeUpdate2( "delete from remote_responses where commandId = ? and responseNumber = ?",
                remoteResponse.getCommandId().toString(), remoteResponse.getResponse().getResponseSequenceNumber() );
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


    public void saveRemoteRequest( RemoteRequest remoteRequest ) throws DBException {
        Preconditions.checkNotNull( remoteRequest, "Remote request is null" );

        dbManager.executeUpdate2( "insert into remote_requests(commandId,attempts,info) values (?,?,?)",
                remoteRequest.getCommandId().toString(), remoteRequest.getAttempts(), gson.toJson( remoteRequest ) );
    }


    public void deleteRemoteRequest( RemoteRequest remoteRequest ) throws DBException {
        Preconditions.checkNotNull( remoteRequest, "Remote request is null" );

        dbManager.executeUpdate2( "delete from remote_requests where commandId = ? and attempts = ?",
                remoteRequest.getCommandId().toString(), remoteRequest.getAttempts() );
    }


    public RemoteRequest getRemoteRequest( UUID commandId ) throws DBException {
        Preconditions.checkNotNull( commandId, "Command Id is null" );

        ResultSet rs =
                dbManager.executeQuery2( "select info from remote_requests where commandId = ?", commandId.toString() );

        if ( rs != null ) {
            Row row = rs.one();
            if ( row != null ) {

                String info = row.getString( "info" );
                try {
                    return gson.fromJson( info, RemoteRequest.class );
                }
                catch ( JsonSyntaxException ex ) {
                    throw new DBException( ex.getMessage() );
                }
            }
        }

        return null;
    }


    public List<RemoteRequest> getRemoteRequests( int attempts, int limit ) throws DBException {
        Preconditions.checkArgument( attempts > 0, "Attempts must be greater than 0" );
        Preconditions.checkArgument( limit > 0, "Limit must be greater than 0" );

        List<RemoteRequest> remoteRequests = new ArrayList<>();

        try {
            ResultSet rs = dbManager
                    .executeQuery2( "select info from remote_requests where attempts < ? limit ? allow filtering",
                            attempts, limit );
            if ( rs != null ) {
                for ( Row row : rs ) {
                    String info = row.getString( "info" );
                    remoteRequests.add( gson.fromJson( info, RemoteRequest.class ) );
                }
            }
        }
        catch ( JsonSyntaxException ex ) {
            throw new DBException( ex.getMessage() );
        }
        return remoteRequests;
    }
}
