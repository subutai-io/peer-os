package org.safehaus.subutai.common;


import java.util.ArrayList;
import java.util.List;

import org.safehaus.subutai.api.dbmanager.DBException;
import org.safehaus.subutai.api.dbmanager.DbManager;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;


/**
 * PluginDAO is used to manage cluster configuration information in database
 */
public class PluginDAO {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final DbManager dbManager;


    public PluginDAO( final DbManager dbManager ) {
        Preconditions.checkNotNull( dbManager, "Db Manager is null" );
        this.dbManager = dbManager;
    }


    public void saveInfo( String source, String key, Object info ) throws DBException {
        dbManager.executeUpdate2( "insert into product_info(source,key,info) values (?,?,?)", source, key,
                gson.toJson( info ) );
    }


    /**
     * Returns all POJOs from DB identified by source key
     *
     * @param source - source key
     * @param clazz - class of POJO
     *
     * @return - list of POJOs
     */
    public <T> List<T> getInfo( String source, Class<T> clazz ) throws DBException {
        List<T> list = new ArrayList<>();
        try {
            ResultSet rs = dbManager.executeQuery2( "select info from product_info where source = ?", source );
            if ( rs != null ) {
                for ( Row row : rs ) {
                    String info = row.getString( "info" );
                    list.add( gson.fromJson( info, clazz ) );
                }
            }
        }
        catch ( JsonSyntaxException ex ) {
            throw new DBException( ex.getMessage() );
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
    public <T> T getInfo( String source, String key, Class<T> clazz ) throws DBException {
        try {

            ResultSet rs = dbManager
                    .executeQuery2( "select info from product_info where source = ? and key = ?", source, key );
            if ( rs != null ) {
                Row row = rs.one();
                if ( row != null ) {

                    String info = row.getString( "info" );
                    return gson.fromJson( info, clazz );
                }
            }
        }
        catch ( JsonSyntaxException ex ) {
            throw new DBException( ex.getMessage() );
        }
        return null;
    }


    /**
     * deletes POJO from DB
     *
     * @param source - source key
     * @param key - POJO key
     */
    public void deleteInfo( String source, String key ) throws DBException {
        dbManager.executeUpdate2( "delete from product_info where source = ? and key = ?", source, key );
    }
}
