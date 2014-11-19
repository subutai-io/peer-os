package org.safehaus.subutai.core.peer.impl.dao;


import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.safehaus.subutai.common.util.DbUtil;
import org.safehaus.subutai.common.util.GsonInterfaceAdapter;
import org.safehaus.subutai.core.peer.api.ManagementHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;


/**
 * PluginDAO is used to manage cluster configuration information in database
 */
public class PeerDAO
{

    private static final Logger LOG = LoggerFactory.getLogger( PeerDAO.class.getName() );
    private Gson gson;
    protected DbUtil dbUtil;


    public PeerDAO( final DataSource dataSource ) throws SQLException
    {
        Preconditions.checkNotNull( dataSource, "DataSource is null" );
        this.dbUtil = new DbUtil( dataSource );

        GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping();
        gsonBuilder.registerTypeAdapter( ManagementHost.class, new GsonInterfaceAdapter<ManagementHost>() ).create();
        gson = gsonBuilder.create();
        setupDb();
    }


    protected void setupDb() throws SQLException
    {

        String sql1 = "create table if not exists peer (source varchar(100), id uuid, info clob, PRIMARY KEY (source, "
                + "id));";
        String sql2 =
                "create table if not exists peer_group (source varchar(100), id uuid, info clob, PRIMARY KEY (source, "
                        + "id));";

        dbUtil.update( sql1 );
        dbUtil.update( sql2 );
    }


    public boolean saveInfo( String source, String key, Object info )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( source ), "Source is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( key ), "Key is null or empty" );
        Preconditions.checkNotNull( info, "Info is null" );

        try
        {
            String json = gson.toJson( info );
            dbUtil.update( "merge into peer (source, id, info) values (?, ? ,?)", source, UUID.fromString( key ),
                    json );
            return true;
        }
        catch ( SQLException e )
        {
            LOG.error( e.getMessage() );
        }
        catch ( Exception e )
        {
            LOG.error( e.toString() );
        }
        return false;
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
        Preconditions.checkArgument( !Strings.isNullOrEmpty( source ), "Source is null or empty" );
        Preconditions.checkNotNull( clazz, "Class is null" );

        List<T> list = new ArrayList<>();
        try
        {
            ResultSet rs = dbUtil.select( "select info from peer where source = ?", source );
            while ( rs != null && rs.next() )
            {
                Clob infoClob = rs.getClob( "info" );
                if ( infoClob != null && infoClob.length() > 0 )
                {
                    String info = infoClob.getSubString( 1, ( int ) infoClob.length() );
                    list.add( gson.fromJson( info, clazz ) );
                }
            }
        }
        catch ( JsonSyntaxException | SQLException e )
        {
            LOG.error( e.getMessage() );
        }
        return list;
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
        Preconditions.checkArgument( !Strings.isNullOrEmpty( source ), "Source is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( key ), "Key is null or empty" );
        Preconditions.checkNotNull( clazz, "Class is null" );

        try
        {

            ResultSet rs = dbUtil.select( "select info from peer where source = ? and id = ?", source,
                    UUID.fromString( key ) );
            if ( rs != null && rs.next() )
            {
                Clob infoClob = rs.getClob( "info" );
                if ( infoClob != null && infoClob.length() > 0 )
                {
                    String info = infoClob.getSubString( 1, ( int ) infoClob.length() );
                    return gson.fromJson( info, clazz );
                }
            }
        }
        catch ( JsonSyntaxException | SQLException e )
        {
            LOG.error( e.getMessage() );
        }
        return null;
    }


    /**
     * deletes POJO from DB
     *
     * @param source - source key
     * @param key - POJO key
     */
    public boolean deleteInfo( String source, String key )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( source ), "Source is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( key ), "Key is null or empty" );

        try
        {
            dbUtil.update( "delete from peer where source = ? and id = ?", source, UUID.fromString( key ) );
            return true;
        }
        catch ( SQLException e )
        {
            LOG.error( e.getMessage(), e );
        }

        return false;
    }


    public boolean updateInfo( String source, String key, Object info )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( source ), "Source is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( key ), "Key is null or empty" );
        Preconditions.checkNotNull( info, "Info is null" );

        try
        {
            dbUtil.update( "merge into peer (source, id, info) values (?, ? ,?)", source, UUID.fromString( key ),
                    gson.toJson( info ) );
            return true;
        }
        catch ( SQLException e )
        {
            LOG.error( e.getMessage() );
        }
        return false;
    }
}
