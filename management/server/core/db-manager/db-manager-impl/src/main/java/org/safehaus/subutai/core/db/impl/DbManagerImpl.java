/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.db.impl;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.core.db.api.DbManager;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;


/**
 * Implementation of DbManager
 */
public class DbManagerImpl implements DbManager
{

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Logger LOG = Logger.getLogger( DbManagerImpl.class.getName() );
    private final Map<String, PreparedStatement> statements = new ConcurrentHashMap<>();
    /**
     * Cassandra cluster
     */
    private Cluster cluster;
    /**
     * Cassandra session
     */
    private Session session;
    /**
     * Cassandra host
     */
    private String cassandraHost;
    /**
     * Cassandra keyspace
     */
    private String cassandraKeyspace;
    /**
     * Cassandra port
     */
    private int cassandraPort;


    public void setCassandraKeyspace( String cassandraKeyspace )
    {
        this.cassandraKeyspace = cassandraKeyspace;
    }


    public void setCassandraHost( String cassandraHost )
    {
        this.cassandraHost = cassandraHost;
    }


    public void setCassandraPort( int cassandraPort )
    {
        this.cassandraPort = cassandraPort;
    }


    /**
     * Initializes db manager
     */
    public void init()
    {

        Preconditions.checkArgument( !Strings.isNullOrEmpty( cassandraKeyspace ), "Keyspace is null or empty" );
        Preconditions.checkArgument( cassandraPort >= 1024 && cassandraPort <= 65536,
                "Port must be n range 1024 and 65536" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( cassandraHost ), "Cassandra host is null or empty" );
        Preconditions.checkArgument( cassandraHost.matches( Common.HOSTNAME_REGEX ), "Invalid cassandra host" );

        try
        {
            cluster = Cluster.builder().withPort( cassandraPort ).addContactPoint( cassandraHost ).build();
            setSession( cluster.connect( cassandraKeyspace ) );
            LOG.log( Level.INFO, "DbManager started" );
        }
        catch ( Exception ex )
        {
            LOG.log( Level.SEVERE, "Error in init", ex );
        }
    }


    public void setSession( Session session )
    {
        Preconditions.checkNotNull( session, "Session is null" );
        this.session = session;
    }


    /**
     * Disposes db manager
     */
    public void destroy()
    {
        try
        {
            session.close();
        }
        catch ( Exception e )
        {
            LOG.log( Level.WARNING, "ignore", e );
        }
        try
        {
            cluster.close();
        }
        catch ( Exception e )
        {
            LOG.log( Level.WARNING, "ignore", e );
        }
        LOG.log( Level.INFO, "DbManager stopped" );
    }


    /**
     * Executes a select query against db
     *
     * @param cql - sql query with placeholders for bind parameters in form of ?
     * @param values - bind parameters
     *
     * @return - resultset
     */
    public ResultSet executeQuery( String cql, Object... values )
    {
        try
        {
            PreparedStatement stmt = statements.get( cql );
            if ( stmt == null )
            {
                stmt = session.prepare( cql );
                statements.put( cql, stmt );
            }
            BoundStatement boundStatement = new BoundStatement( stmt );
            if ( values != null && values.length > 0 )
            {
                boundStatement.bind( values );
            }
            return session.execute( boundStatement );
        }
        catch ( Exception ex )
        {
            LOG.log( Level.SEVERE, "Error in executeQuery", ex );
        }
        return null;
    }


    public ResultSet executeQuery2( String cql, Object... values ) throws DBException
    {

        try
        {
            PreparedStatement stmt = statements.get( cql );
            if ( stmt == null )
            {
                stmt = session.prepare( cql );
                statements.put( cql, stmt );
            }
            BoundStatement boundStatement = new BoundStatement( stmt );
            if ( values != null && values.length > 0 )
            {
                boundStatement.bind( values );
            }
            return session.execute( boundStatement );
        }
        catch ( RuntimeException ex )
        {
            LOG.log( Level.SEVERE, "Error in executeQuery2", ex );
            throw new DBException( ex.getMessage() );
        }
    }


    public void saveInfo2( String source, String key, Object info ) throws DBException
    {
        executeUpdate2( "insert into product_info(source,key,info) values (?,?,?)", source, key, GSON.toJson( info ) );
    }


    public void deleteInfo2( String source, String key ) throws DBException
    {
        executeUpdate2( "delete from product_info where source = ? and key = ?", source, key );
    }


    public void executeUpdate2( String cql, Object... values ) throws DBException
    {
        try
        {
            PreparedStatement stmt = statements.get( cql );
            if ( stmt == null )
            {
                stmt = session.prepare( cql );
                statements.put( cql, stmt );
            }
            BoundStatement boundStatement = new BoundStatement( stmt );
            if ( values != null && values.length > 0 )
            {
                boundStatement.bind( values );
            }
            session.execute( boundStatement );
        }
        catch ( RuntimeException ex )
        {
            LOG.log( Level.SEVERE, "Error in executeUpdate2", ex );
            throw new DBException( ex.getMessage() );
        }
    }


    /**
     * Executes CUD (insert update delete) query against DB
     *
     * @param cql - sql query with placeholders for bind parameters in form of ?
     * @param values - bind parameters
     *
     * @return true if all went well and false if exception was raised
     */
    public boolean executeUpdate( String cql, Object... values )
    {
        try
        {
            PreparedStatement stmt = statements.get( cql );
            if ( stmt == null )
            {
                stmt = session.prepare( cql );
                statements.put( cql, stmt );
            }
            BoundStatement boundStatement = new BoundStatement( stmt );
            if ( values != null && values.length > 0 )
            {
                boundStatement.bind( values );
            }
            session.execute( boundStatement );
            return true;
        }
        catch ( Exception ex )
        {
            LOG.log( Level.SEVERE, "Error in executeUpdate", ex );
        }
        return false;
    }


    /**
     * Saves POJO to DB
     *
     * @param source - source key
     * @param key - POJO key
     * @param info - custom object
     *
     * @return true if all went well and false if exception was raised
     */
    public boolean saveInfo( String source, String key, Object info )
    {
        return executeUpdate( "insert into product_info(source,key,info) values (?,?,?)", source, key,
                GSON.toJson( info ) );
    }


    public boolean saveEnvironmentInfo( String source, String key, Object info )
    {
        return executeUpdate( "insert into environment_info(source,key,info) values (?,?,?)", source, key,
                GSON.toJson( info ) );
    }


    /**
     * Returns POJO from DB
     *
     * @param source - source key
     * @param key - pojo key
     * @param clazz - class of POJO
     *
     * @return - POJO
     */
    public <T> T getInfo( String source, String key, Class<T> clazz )
    {
        try
        {

            ResultSet rs = executeQuery( "select info from product_info where source = ? and key = ?", source, key );
            if ( rs != null )
            {
                Row row = rs.one();
                if ( row != null )
                {

                    String info = row.getString( "info" );
                    return GSON.fromJson( info, clazz );
                }
            }
        }
        catch ( JsonSyntaxException ex )
        {
            LOG.log( Level.SEVERE, "Error in T getInfo", ex );
        }
        return null;
    }


    public <T> T getEnvironmentInfo( String source, String key, Class<T> clazz )
    {
        try
        {

            ResultSet rs =
                    executeQuery( "select info from environment_info where source = ? and key = ?", source, key );
            if ( rs != null )
            {
                Row row = rs.one();
                if ( row != null )
                {

                    String info = row.getString( "info" );
                    return GSON.fromJson( info, clazz );
                }
            }
        }
        catch ( JsonSyntaxException ex )
        {
            LOG.log( Level.SEVERE, "Error in T getInfo", ex );
        }
        return null;
    }


    /**
     * Returns all POJOs from DB identified by source key
     *
     * @param source - source key
     * @param clazz - class of POJO
     *
     * @return - list of POJOs
     */
    public <T> List<T> getInfo( String source, Class<T> clazz )
    {
        List<T> list = new ArrayList<>();
        try
        {
            ResultSet rs = executeQuery( "select info from product_info where source = ?", source );
            if ( rs != null )
            {
                for ( Row row : rs )
                {
                    String info = row.getString( "info" );
                    list.add( GSON.fromJson( info, clazz ) );
                }
            }
        }
        catch ( JsonSyntaxException ex )
        {
            LOG.log( Level.SEVERE, "Error in List<T> getInfo", ex );
        }
        return list;
    }


    public <T> List<T> getEnvironmentInfo( String source, Class<T> clazz )
    {
        List<T> list = new ArrayList<>();
        try
        {
            ResultSet rs = executeQuery( "select info from environment_info where source = ?", source );
            if ( rs != null )
            {
                for ( Row row : rs )
                {
                    String info = row.getString( "info" );
                    list.add( GSON.fromJson( info, clazz ) );
                }
            }
        }
        catch ( JsonSyntaxException ex )
        {
            LOG.log( Level.SEVERE, "Error in List<T> getInfo", ex );
        }
        return list;
    }


    /**
     * deletes POJO from DB
     *
     * @param source - source key
     * @param key - POJO key
     *
     * @return true if all went well and false if exception was raised
     */
    public boolean deleteInfo( String source, String key )
    {
        return executeUpdate( "delete from product_info where source = ? and key = ?", source, key );
    }
}