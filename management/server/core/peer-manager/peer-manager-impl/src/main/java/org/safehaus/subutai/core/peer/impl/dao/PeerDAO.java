package org.safehaus.subutai.core.peer.impl.dao;


import java.util.ArrayList;
import java.util.List;

import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.core.db.api.DbManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
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
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final DbManager dbManager;


    public PeerDAO( final DbManager dbManager )
    {
        Preconditions.checkNotNull( dbManager, "Db Manager is null" );
        this.dbManager = dbManager;
    }


    public boolean saveInfo( String source, String key, Object info )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( source ), "Source is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( key ), "Key is null or empty" );
        Preconditions.checkNotNull( info, "Info is null" );

        try
        {
            dbManager.executeUpdate2( "insert into peer_info(source,key,info) values (?,?,?)", source.toLowerCase(),
                    key.toLowerCase(), gson.toJson( info ) );
            return true;
        }
        catch ( DBException e )
        {
            LOG.error( e.getMessage() );
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
            ResultSet rs =
                    dbManager.executeQuery2( "select info from peer_info where source = ?", source.toLowerCase() );
            if ( rs != null )
            {
                for ( Row row : rs )
                {
                    String info = row.getString( "info" );
                    list.add( gson.fromJson( info, clazz ) );
                }
            }
        }
        catch ( JsonSyntaxException | DBException e )
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

            ResultSet rs = dbManager
                    .executeQuery2( "select info from peer_info where source = ? and key = ?", source.toLowerCase(),
                            key.toLowerCase() );
            if ( rs != null )
            {
                Row row = rs.one();
                if ( row != null )
                {

                    String info = row.getString( "info" );
                    return gson.fromJson( info, clazz );
                }
            }
        }
        catch ( JsonSyntaxException | DBException e )
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
            dbManager.executeUpdate2( "delete from peer_info where source = ? and key = ?", source.toLowerCase(),
                    key.toLowerCase() );
            return true;
        }
        catch ( DBException e )
        {
            LOG.error( e.getMessage(), e );
        }

        return false;
    }
}
