package org.safehaus.subutai.plugin.common;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.safehaus.subutai.common.util.DbUtil;
import org.safehaus.subutai.plugin.common.impl.EmfUtil;
import org.safehaus.subutai.plugin.common.impl.PluginDataService;
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
public class PluginDAO
{

    private static final Logger LOG = LoggerFactory.getLogger( PluginDAO.class.getName() );
    private Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();
    //    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    protected DbUtil dbUtil;
    private EmfUtil emfUtil = new EmfUtil();

    private PluginDataService dataService;


    //    public PluginDAO( EntityManagerFactory emf ) throws SQLException
    //    {
    //        EmfUtil emfUtil = new EmfUtil();
    //        Preconditions.checkNotNull( emf, "EntityManagerFactory cannot be null." );
    //        dataService = new PluginDataService( emf );
    //    }
    //
    //    public PluginDAO(EntityManagerFactory emf, GsonBuilder gsonBuilder) throws SQLException
    //    {
    //        Preconditions.checkNotNull( emf, "EntityManagerFactory cannot be null." );
    //        Preconditions.checkNotNull( gsonBuilder, "GsonBuilder cannot be null." );
    //
    //        dataService = new PluginDataService( emf, gsonBuilder );
    //    }


    public PluginDAO( DataSource dataSource ) throws SQLException
    {
        Preconditions.checkNotNull( dataSource, "DataSource is null" );
        //        this.dbUtil = new DbUtil( dataSource );
        this.dataService = new PluginDataService( emfUtil.getEmf() );
        //        setupDb();
    }


    public PluginDAO() throws SQLException
    {
        //        this.dataService = new PluginDataService( emfUtil.getEmf() );
    }


    public PluginDAO( final DataSource dataSource, final GsonBuilder gsonBuilder ) throws SQLException
    {
        Preconditions.checkNotNull( dataSource, "DataSource is null" );
        Preconditions.checkNotNull( dataSource, "GsonBuilder is null" );
        //        this.dbUtil = new DbUtil( dataSource );
        gson = gsonBuilder.setPrettyPrinting().disableHtmlEscaping().create();
        this.dataService = new PluginDataService( emfUtil.getEmf(), gsonBuilder );
        //        this.dataService = new PluginDataService( emfUtil.getEmf(), gsonBuilder );
        //        setupDb();
    }


    protected void setupDb() throws SQLException
    {

        String sql1 = "create table if not exists cluster_data (source varchar(100), id varchar(100), info clob, " +
                "PRIMARY KEY (source, " + "id));";
        dbUtil.update( sql1 );
    }


    public boolean saveInfo( String source, String key, Object info )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( source ), "Source is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( key ), "Key is null or empty" );
        Preconditions.checkNotNull( info, "Info is null" );


        try
        {
            dataService.update( source, key, info );
            //            dbUtil.update( "merge into cluster_data (source, id, info) values (? , ?, ?)", source, key,
            //                    gson.toJson( info ) );

            return true;
        }
        catch ( SQLException e )
        {
            LOG.error( e.getMessage(), e );
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
            list = dataService.getInfo( source, clazz );
            //            ResultSet rs = dbUtil.select( "select info from cluster_data where source = ?", source );
            //            while ( rs != null && rs.next() )
            //            {
            //                Clob infoClob = rs.getClob( "info" );
            //                if ( infoClob != null && infoClob.length() > 0 )
            //                {
            //                    String info = infoClob.getSubString( 1, ( int ) infoClob.length() );
            //                    list.add( gson.fromJson( info, clazz ) );
            //                }
            //            }
        }
        catch ( JsonSyntaxException | SQLException e )
        {
            LOG.error( e.getMessage(), e );
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
            return dataService.getInfo( source, key, clazz );
            //            ResultSet rs = dbUtil.select( "select info from cluster_data where source = ? and id = ?",
            // source, key );
            //            if ( rs != null && rs.next() )
            //            {
            //                Clob infoClob = rs.getClob( "info" );
            //                if ( infoClob != null && infoClob.length() > 0 )
            //                {
            //                    String info = infoClob.getSubString( 1, ( int ) infoClob.length() );
            //                    return gson.fromJson( info, clazz );
            //                }
            //            }
        }
        catch ( JsonSyntaxException | SQLException e )
        {
            LOG.error( e.getMessage(), e );
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
            dataService.remove( source, key );
            //            dbUtil.update( "delete from cluster_data where source = ? and id = ?", source, key );
            return true;
        }
        catch ( SQLException e )
        {
            LOG.error( e.getMessage(), e );
        }
        return false;
    }
}