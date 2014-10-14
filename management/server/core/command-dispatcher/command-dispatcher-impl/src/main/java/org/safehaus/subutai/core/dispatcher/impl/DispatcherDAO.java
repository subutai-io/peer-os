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
 *
 * TODO - delete requests & responses with exceeded attempts
 */

public class DispatcherDAO
{
    private static final Logger LOG = LoggerFactory.getLogger( DispatcherDAO.class.getName() );

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String COMMAND_ID_IS_NULL_MSG = "Command id is null";
    protected DbUtil dbUtil;


    public DispatcherDAO( final DataSource dataSource ) throws DaoException
    {
        Preconditions.checkNotNull( dataSource, "Data source is null" );

        this.dbUtil = new DbUtil( dataSource );

        setupDb();
    }


    private void setupDb() throws DaoException
    {

        String sql =
                "create table if not exists remote_responses(commandId uuid, agentId uuid, responseNumber smallint, " +
                        "info varchar(2000), PRIMARY KEY (commandId, agentId, responseNumber));"
                        + "create table if not exists remote_requests(commandId uuid, attempts smallint, " +
                        "info varchar(1000), PRIMARY KEY (commandId));";
        try
        {
            dbUtil.update( sql );
        }
        catch ( SQLException e )
        {
            throw new DaoException( e );
        }
    }


    public Set<RemoteResponse> getRemoteResponses( UUID commandId ) throws DaoException
    {
        Preconditions.checkNotNull( commandId, COMMAND_ID_IS_NULL_MSG );

        Set<RemoteResponse> responses = new LinkedHashSet<>();

        try
        {
            ResultSet rs = dbUtil.select( "select info from remote_responses where commandId = ?", commandId );
            RemoteResponse response = getResponse( rs );
            while ( response != null )
            {
                responses.add( response );
                response = getResponse( rs );
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
            dbUtil.update( "merge into remote_responses(commandId, agentId, responseNumber, info) values (?,?,?,?)",
                    remoteResponse.getCommandId(), remoteResponse.getResponse().getUuid(),
                    remoteResponse.getResponse().getResponseSequenceNumber(), GSON.toJson( remoteResponse ) );
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
            dbUtil.update( "delete from remote_responses where commandId = ?", commandId );
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
            dbUtil.update( "delete from remote_responses where commandId = ? and agentId = ? and responseNumber = ?",
                    remoteResponse.getCommandId(), remoteResponse.getResponse().getUuid(),
                    remoteResponse.getResponse().getResponseSequenceNumber() );
        }
        catch ( SQLException e )
        {
            throw new DaoException( e );
        }
    }


    public void saveRemoteRequest( RemoteRequest remoteRequest ) throws DaoException
    {
        Preconditions.checkNotNull( remoteRequest, "Remote request is null" );

        try
        {
            dbUtil.update( "merge into remote_requests(commandId,attempts,info) values (?,?,?)",
                    remoteRequest.getCommandId(), remoteRequest.getAttempts(), GSON.toJson( remoteRequest ) );
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
            dbUtil.update( "delete from remote_requests where commandId = ?", commandId );
        }
        catch ( SQLException e )
        {
            throw new DaoException( e );
        }
    }


    public void deleteRequestsWithExceededAttempts( int attempts ) throws DaoException
    {
        try
        {
            dbUtil.update( "delete from remote_requests where attempts >= ?", attempts );
        }
        catch ( SQLException e )
        {
            throw new DaoException( e );
        }
    }


    public void deleteOrphanResponses() throws DaoException
    {
        try
        {
            dbUtil.update( "delete from remote_responses res where not exists "
                    + "(select 1 from remote_requests req where req.commandId = res.commandId)" );
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
            ResultSet rs = dbUtil.select( "select info from remote_requests where commandId = ?", commandId );

            return getRequest( rs );
        }
        catch ( SQLException | JsonSyntaxException ex )
        {
            LOG.error( "Error in getRemoteRequest", ex );

            throw new DaoException( ex );
        }
    }


    public Set<RemoteRequest> getRemoteRequests( int attempts, int limit ) throws DaoException
    {
        Preconditions.checkArgument( attempts > 0, "Attempts must be greater than 0" );
        Preconditions.checkArgument( limit > 0, "Limit must be greater than 0" );

        Set<RemoteRequest> remoteRequests = new LinkedHashSet<>();

        try
        {
            ResultSet rs =
                    dbUtil.select( "select info from remote_requests where attempts < ? limit ?", attempts, limit );

            RemoteRequest remoteRequest = getRequest( rs );
            while ( remoteRequest != null )
            {
                remoteRequests.add( remoteRequest );
                remoteRequest = getRequest( rs );
            }
        }
        catch ( SQLException | JsonSyntaxException ex )
        {
            LOG.error( "Error in getRemoteRequests", ex );

            throw new DaoException( ex );
        }

        return remoteRequests;
    }


    private RemoteResponse getResponse( ResultSet rs ) throws SQLException
    {
        if ( rs != null && rs.next() )
        {
            return GSON.fromJson( rs.getString( "info" ), RemoteResponse.class );
        }
        return null;
    }


    private RemoteRequest getRequest( ResultSet rs ) throws SQLException
    {
        if ( rs != null && rs.next() )
        {
            return GSON.fromJson( rs.getString( "info" ), RemoteRequest.class );
        }
        return null;
    }
}
