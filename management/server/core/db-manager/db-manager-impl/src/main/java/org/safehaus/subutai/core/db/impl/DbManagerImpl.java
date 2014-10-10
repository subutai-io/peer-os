/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.db.impl;


import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.core.db.api.DbManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger LOG = LoggerFactory.getLogger( DbManagerImpl.class.getName() );
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


    DataSource dataSource;


    public void setDataSource( DataSource dataSource )
    {
        this.dataSource = dataSource;
    }


    public void test() throws Exception
    {
        Connection con = dataSource.getConnection();
        Statement stmt = null;
        DatabaseMetaData dbMeta = con.getMetaData();
        System.out.println( "Using datasource " + dbMeta.getDatabaseProductName() + ", URL " + dbMeta.getURL() );
        try
        {
            stmt = con.createStatement();
            try
            {
                stmt.execute( "drop table person" );
            }
            catch ( Exception e )
            {
                // Ignore as it will fail the first time
            }
            stmt.execute( "create table person (name varchar(100), twittername varchar(100))" );
            stmt.execute( "insert into person (name, twittername) values ('Christian Schneider', '@schneider_chris')" );
            java.sql.ResultSet rs = stmt.executeQuery( "select * from person" );
            ResultSetMetaData meta = rs.getMetaData();
            while ( rs.next() )
            {
                for ( int c = 1; c <= meta.getColumnCount(); c++ )
                {
                    System.out.print( rs.getString( c ) + ", " );
                }
                System.out.println();
            }
        }
        catch ( Exception e )
        {
            System.out.println( e.getMessage() );
            throw e;
        }
        finally
        {
            if ( stmt != null )
            {
                stmt.close();
            }
            if ( con != null )
            {
                con.close();
            }
        }
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
            LOG.info( "DbManager started" );
        }
        catch ( Exception ex )
        {
            LOG.error( "Error in init", ex );
        }

        try
        {
            test();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
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
        catch ( Exception ignore )
        {
        }
        try
        {
            cluster.close();
        }
        catch ( Exception ignore )
        {
        }
        LOG.info( "DbManager stopped" );
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
            LOG.error( "Error in executeQuery", ex );
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
            LOG.error( "Error in executeQuery2", ex );
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
            LOG.error( "Error in executeUpdate2", ex );
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
            LOG.error( "Error in executeUpdate", ex );
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
            List<T> list = getListFromResultSet( rs, clazz );
            if ( !list.isEmpty() )
            {
                return list.iterator().next();
            }
        }
        catch ( JsonSyntaxException ex )
        {
            LOG.error( "Error in T getInfo", ex );
        }
        return null;
    }


    public <T> T getEnvironmentInfo( String source, String key, Class<T> clazz )
    {
        try
        {

            ResultSet rs =
                    executeQuery( "select info from environment_info where source = ? and key = ?", source, key );
            List<T> list = getListFromResultSet( rs, clazz );
            if ( !list.isEmpty() )
            {
                return list.iterator().next();
            }
        }
        catch ( JsonSyntaxException ex )
        {
            LOG.error( "Error in T getInfo", ex );
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
        try
        {
            ResultSet rs = executeQuery( "select info from product_info where source = ?", source );
            return getListFromResultSet( rs, clazz );
        }
        catch ( JsonSyntaxException ex )
        {
            LOG.error( "Error in List<T> getInfo", ex );
        }
        return Collections.emptyList();
    }


    public <T> List<T> getEnvironmentInfo( String source, Class<T> clazz )
    {
        try
        {
            ResultSet rs = executeQuery( "select info from environment_info where source = ?", source );
            return getListFromResultSet( rs, clazz );
        }
        catch ( JsonSyntaxException ex )
        {
            LOG.error( "Error in List<T> getInfo", ex );
        }
        return Collections.emptyList();
    }


    private <T> List<T> getListFromResultSet( ResultSet rs, Class<T> clazz )
    {
        List<T> list = new ArrayList<>();
        if ( rs != null )
        {
            for ( Row row : rs )
            {
                String info = row.getString( "info" );
                list.add( GSON.fromJson( info, clazz ) );
            }
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