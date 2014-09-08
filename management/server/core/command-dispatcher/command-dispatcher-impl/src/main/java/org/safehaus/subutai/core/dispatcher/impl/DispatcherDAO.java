package org.safehaus.subutai.core.dispatcher.impl;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final DbManager dbManager;


    public DispatcherDAO( final DbManager dbManager ) {
        Preconditions.checkNotNull( dbManager, "Db Manager is null" );

        this.dbManager = dbManager;
    }


    public RemoteResponse getRemoteResponse( UUID ownerId, UUID commandId ) throws DBException {
        Preconditions.checkNotNull( ownerId, "Owner Id is null" );
        Preconditions.checkNotNull( commandId, "Command Id is null" );

        ResultSet rs = dbManager
                .executeQuery2( "select info from remote_responses where ownerId = ? and commandId = ?", ownerId,
                        commandId );

        if ( rs != null ) {
            Row row = rs.one();
            if ( row != null ) {

                String info = row.getString( "info" );
                try {
                    return gson.fromJson( info, RemoteResponse.class );
                }
                catch ( JsonSyntaxException ex ) {
                    throw new DBException( ex.getMessage() );
                }
            }
        }

        return null;
    }


    public List<RemoteResponse> getRemoteResponses( int limit ) throws DBException {
        List<RemoteResponse> responses = new ArrayList<>();

        try {
            ResultSet rs = dbManager.executeQuery2( "select info from remote_responses limit ?", limit );
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

        dbManager.executeUpdate2( "insert into remote_responses(ownerId,commandId,info) values (?,?,?)",
                remoteResponse.getOwnerId(), remoteResponse.getCommandId(), gson.toJson( remoteResponse ) );
    }


    public void deleteRemoteResponse( RemoteResponse remoteResponse ) throws DBException {
        Preconditions.checkNotNull( remoteResponse, "Remote response is null" );

        dbManager.executeUpdate2( "delete from remote_responses where ownerId = ? and commandId = ?",
                remoteResponse.getOwnerId(), remoteResponse.getCommandId() );
    }
}
