package io.subutai.core.plugincommon.impl;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import io.subutai.core.plugincommon.api.PluginDAO;


/**
 * PluginDAO is used to manage cluster configuration information in database
 */
public class PluginDAOImpl implements PluginDAO
{

    private static final Logger LOG = LoggerFactory.getLogger( PluginDAOImpl.class.getName() );
    private Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();
    private PluginDataService dataService;
    private EntityManagerFactory entityManagerFactory = null;

    private static final ReentrantLock lock = new ReentrantLock( true );


    /* *******************************************************************
     *
     */
    public PluginDAOImpl( DataSource dataSource ) throws SQLException
    {
    }


    public PluginDAOImpl() throws SQLException
    {
    }


    public PluginDAOImpl( final DataSource dataSource, final GsonBuilder gsonBuilder ) throws SQLException
    {
        Preconditions.checkNotNull( dataSource, "GsonBuilder is null" );
    }


    /* *******************************************************************
     *
     */
    public void init() throws SQLException
    {
        this.dataService = new PluginDataService( entityManagerFactory );
    }



    /* *******************************************************************
     *
     */
    @Override
    public boolean saveInfo( String source, String key, Object info )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( source ), "Source is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( key ), "Key is null or empty" );
        Preconditions.checkNotNull( info, "Info is null" );

        try
        {
            lock.lock();

            dataService.update( source, key, info );

            return true;
        }
        catch ( SQLException e )
        {
            LOG.error( e.getMessage(), e );
        }
        finally
        {
            lock.unlock();
        }
        return false;
    }


    @Override
    public boolean saveInfo( String source, String key, String info )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( source ), "Source is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( key ), "Key is null or empty" );
        Preconditions.checkNotNull( info, "Info is null" );


        try
        {
            lock.lock();

            dataService.update( source, key, info );

            return true;
        }
        catch ( SQLException e )
        {
            LOG.error( e.getMessage(), e );
        }
        finally
        {
            lock.unlock();
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
    @Override
    public <T> List<T> getInfo( String source, Class<T> clazz )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( source ), "Source is null or empty" );
        Preconditions.checkNotNull( clazz, "Class is null" );

        List<T> list = new ArrayList<>();
        try
        {
            lock.lock();
            list = dataService.getInfo( source, clazz );
        }
        catch ( JsonSyntaxException | SQLException e )
        {
            LOG.error( e.getMessage(), e );
        }
        finally
        {
            lock.unlock();
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
    @Override
    public <T> T getInfo( String source, String key, Class<T> clazz )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( source ), "Source is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( key ), "Key is null or empty" );
        Preconditions.checkNotNull( clazz, "Class is null" );

        try
        {
            lock.lock();

            return dataService.getInfo( source, key, clazz );
        }
        catch ( JsonSyntaxException | SQLException e )
        {
            LOG.error( e.getMessage(), e );
        }
        finally
        {
            lock.unlock();
        }
        return null;
    }


    /**
     * Returns all POJOs from DB identified by source key
     *
     * @param source - source key
     *
     * @return - list of Json String
     */
    @Override
    public List<String> getInfo( String source )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( source ), "Source is null or empty" );

        List<String> list = new ArrayList<>();
        try
        {
            lock.lock();
            list = dataService.getInfo( source );
        }
        catch ( JsonSyntaxException | SQLException e )
        {
            LOG.error( e.getMessage(), e );
        }
        finally
        {
            lock.unlock();
        }
        return list;
    }


    /**
     * Returns POJO from DB
     *
     * @param source - source key
     * @param key - pojo key
     *
     * @return - POJO
     */
    @Override
    public String getInfo( String source, String key )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( source ), "Source is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( key ), "Key is null or empty" );

        try
        {
            lock.lock();
            return dataService.getInfo( source, key );
        }
        catch ( JsonSyntaxException | SQLException e )
        {
            LOG.error( e.getMessage(), e );
        }
        finally
        {
            lock.unlock();
        }
        return null;
    }


    /**
     * deletes POJO from DB
     *
     * @param source - source key
     * @param key - POJO key
     */
    @Override
    public boolean deleteInfo( String source, String key )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( source ), "Source is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( key ), "Key is null or empty" );

        try
        {
            lock.lock();
            dataService.remove( source, key );
            return true;
        }
        catch ( SQLException e )
        {
            LOG.error( e.getMessage(), e );
        }
        finally
        {
            lock.unlock();
        }
        return false;
    }


    public EntityManagerFactory getEntityManagerFactory()
    {
        return entityManagerFactory;
    }


    public void setEntityManagerFactory( final EntityManagerFactory entityManagerFactory )
    {
        this.entityManagerFactory = entityManagerFactory;

        if ( entityManagerFactory != null )
        {
            entityManagerFactory.createEntityManager();
        }
    }
}