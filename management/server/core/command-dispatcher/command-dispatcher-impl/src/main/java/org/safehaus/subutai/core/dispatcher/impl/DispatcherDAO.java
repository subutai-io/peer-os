package org.safehaus.subutai.core.dispatcher.impl;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import javax.sql.DataSource;

import org.safehaus.subutai.common.util.DbUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final DataSource dataSource;


    public DispatcherDAO( final DataSource dataSource ) throws DaoException
    {
        Preconditions.checkNotNull( dataSource, "Data source is null" );

        this.dataSource = dataSource;

        setupDb();
    }


    private void setupDb() throws DaoException
    {

        String sql = "create table if not exists remote_responses(commandid varchar(36), responsenumber smallint, " +
                "info clob, PRIMARY KEY (commandid, responsenumber));"
                + "create table if not exists remote_requests(commandid varchar(36), attempts smallint, " +
                "info clob, PRIMARY KEY (commandid, attempts));";
        try
        {
            DbUtil.update( dataSource, sql );
        }
        catch ( SQLException e )
        {
            throw new DaoException( e );
        }
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


    public Set<RemoteResponse> getRemoteResponses( UUID commandId ) throws DaoException
    {
        Preconditions.checkNotNull( commandId, COMMAND_ID_IS_NULL_MSG );

        Set<RemoteResponse> responses = new LinkedHashSet<>();

        try
        {
            ResultSet rs = DbUtil.select( dataSource, "select info from remote_responses where commandId = ?",
                    commandId.toString() );
            if ( rs != null )
            {
                while ( rs.next() )
                {
                    String info = rs.getString( "info" );
                    responses.add( GSON.fromJson( info, RemoteResponse.class ) );
                }
            }
        }
        catch ( SQLException | JsonSyntaxException ex )
        {
            LOG.error( "Error in getRemoteResponses", ex );
            throw new DaoException( ex );
        }

        return responses;
    }


    public void saveRemoteResponse( RemoteResponse remoteResponse ) throws DaoException
    {
        Preconditions.checkNotNull( remoteResponse, "Remote response is null" );

        try
        {
            DbUtil.update( dataSource, "insert into remote_responses(commandId, responseNumber, info) values (?,?,?)",
                    remoteResponse.getCommandId().toString(),
                    String.format( "%s_%s", remoteResponse.getResponse().getUuid(),
                            remoteResponse.getResponse().getResponseSequenceNumber() ), GSON.toJson( remoteResponse ) );
        }
        catch ( SQLException e )
        {
            throw new DaoException( e );
        }
    }


    public void deleteRemoteResponses( UUID commandId ) throws DaoException
    {
        Preconditions.checkNotNull( commandId, COMMAND_ID_IS_NULL_MSG );

        try
        {
            DbUtil.update( dataSource, "delete from remote_responses where commandId = ?", commandId.toString() );
        }
        catch ( SQLException e )
        {
            throw new DaoException( e );
        }
    }


    public void deleteRemoteResponse( RemoteResponse remoteResponse ) throws DaoException
    {
        Preconditions.checkNotNull( remoteResponse, "Remote response is null" );

        try
        {
            DbUtil.update( dataSource, "delete from remote_responses where commandId = ? and responseNumber = ?",
                    remoteResponse.getCommandId().toString(),
                    String.format( "%s_%s", remoteResponse.getResponse().getUuid(),
                            remoteResponse.getResponse().getResponseSequenceNumber() ) );
        }
        catch ( SQLException e )
        {
            throw new DaoException( e );
        }
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


    public void saveRemoteRequest( RemoteRequest remoteRequest ) throws DaoException
    {
        Preconditions.checkNotNull( remoteRequest, "Remote request is null" );

        try
        {
            DbUtil.update( dataSource, "insert into remote_requests(commandId,attempts,info) values (?,?,?)",
                    remoteRequest.getCommandId().toString(), remoteRequest.getAttempts(),
                    GSON.toJson( remoteRequest ) );
        }
        catch ( SQLException e )
        {
            throw new DaoException( e );
        }
    }


    //workaround until we change Cassandra to another DB
    public void deleteRemoteRequestWithAttempts( UUID commandId, int attempts ) throws DaoException
    {
        Preconditions.checkArgument( attempts >= 0, "Attempts < 0" );
        Preconditions.checkNotNull( commandId, COMMAND_ID_IS_NULL_MSG );

        try
        {
            DbUtil.update( dataSource, "delete from remote_requests where commandId = ? and attempts = ?",
                    commandId.toString(), attempts );
        }
        catch ( SQLException e )
        {
            throw new DaoException( e );
        }
    }


    public void deleteRemoteRequest( UUID commandId ) throws DaoException
    {
        Preconditions.checkNotNull( commandId, COMMAND_ID_IS_NULL_MSG );

        try
        {
            DbUtil.update( dataSource, "delete from remote_requests where commandId = ?", commandId.toString() );
        }
        catch ( SQLException e )
        {
            throw new DaoException( e );
        }
    }


    public RemoteRequest getRemoteRequest( UUID commandId ) throws DaoException
    {
        Preconditions.checkNotNull( commandId, COMMAND_ID_IS_NULL_MSG );


        try
        {
            ResultSet rs = DbUtil.select( dataSource, "select info from remote_requests where commandId = ?",
                    commandId.toString() );
            if ( rs != null && rs.next() )
            {

                String info = rs.getString( "info" );
                return GSON.fromJson( info, RemoteRequest.class );
            }
        }
        catch ( SQLException | JsonSyntaxException ex )
        {
            LOG.error( "Error in getRemoteRequest", ex );

            throw new DaoException( ex );
        }

        return null;
    }


    public Set<RemoteRequest> getRemoteRequests( int attempts, int limit ) throws DaoException
    {
        Preconditions.checkArgument( attempts > 0, "Attempts must be greater than 0" );
        Preconditions.checkArgument( limit > 0, "Limit must be greater than 0" );

        Set<RemoteRequest> remoteRequests = new LinkedHashSet<>();

        try
        {
            ResultSet rs = DbUtil.select( dataSource,
                    "select info from remote_requests where attempts < ? limit ? allow filtering", attempts, limit );
            if ( rs != null )
            {
                while ( rs.next() )
                {
                    String info = rs.getString( "info" );
                    remoteRequests.add( GSON.fromJson( info, RemoteRequest.class ) );
                }
            }
        }
        catch ( SQLException | JsonSyntaxException ex )
        {
            LOG.error( "Error in getRemoteRequests", ex );

            throw new DaoException( ex );
        }

        return remoteRequests;
    }
}
