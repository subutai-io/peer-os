package io.subutai.core.pluginmanager.impl.dao;


import java.io.File;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.pluginmanager.api.dao.ConfigDataService;
import io.subutai.core.pluginmanager.api.model.PluginDetails;
import io.subutai.core.pluginmanager.impl.model.PluginDetailsEntity;


public class ConfigDataServiceImpl implements ConfigDataService
{
    private static final Logger LOG = LoggerFactory.getLogger( ConfigDataServiceImpl.class );
    private DaoManager daoManager;


    public ConfigDataServiceImpl( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    @Override
    public void saveDetails( final String name, final String version, final String pathToKar/*, final Long userId,
    final Long roleId, final String token */ )
    {
        PluginDetails pluginDetails = new PluginDetailsEntity();
        pluginDetails.setName( name );
        pluginDetails.setVersion( version );
        pluginDetails.setKar( pathToKar );

        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            daoManager.startTransaction( em );
            em.merge( pluginDetails );
            daoManager.commitTransaction( em );
        }
        catch ( Exception e )
        {
            daoManager.rollBackTransaction( em );
            LOG.error( "ConfigDataService saveProfile:" + e.toString() );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    @Override
    public List<PluginDetails> getInstalledPlugins()
    {
        List<PluginDetails> result = Lists.newArrayList();

        EntityManager em = daoManager.getEntityManagerFromFactory();
        Query query;
        try
        {
            query = em.createQuery( "select h from PluginDetailsEntity h" );
            result = ( List<PluginDetails> ) query.getResultList();
        }
        catch ( Exception e )
        {
            LOG.error( e.toString(), e );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
        return result;
    }


    @Override
    public void deleteDetails( final Long pluginId )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();

        try
        {
            daoManager.startTransaction( em );
            PluginDetailsEntity entity = em.find( PluginDetailsEntity.class, pluginId );


            File karFile = new File( entity.getKar() );

            if ( karFile.delete() )
            {
                em.remove( entity );
                em.flush();
                daoManager.commitTransaction( em );
            }
        }
        catch ( Exception ex )
        {
            daoManager.rollBackTransaction( em );
            LOG.error( "ConfigDataService deleteOperation:" + ex.toString() );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    @Override
    public PluginDetails getPluginDetails( final Long pluginId )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            daoManager.startTransaction( em );
            PluginDetailsEntity entity = em.find( PluginDetailsEntity.class, pluginId );
            return entity;
        }
        catch ( Exception e )
        {
            daoManager.closeEntityManager( em );
            return null;
        }
    }
}
