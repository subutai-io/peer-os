package io.subutai.core.hubmanager.impl.dao;


import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.hubmanager.api.dao.ConfigDataService;
import io.subutai.core.hubmanager.api.model.Config;
import io.subutai.core.hubmanager.impl.model.ConfigEntity;


/**
 * Created by ermek on 10/27/15.
 */
public class ConfigDataServiceImpl implements ConfigDataService
{
    private static final Logger LOG = LoggerFactory.getLogger( ConfigDataServiceImpl.class );
    private DaoManager daoManager;


    public ConfigDataServiceImpl( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    @Override
    public void saveHubConfig( final Config config )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();


        try
        {
            daoManager.startTransaction( em );
            em.merge( config );
            daoManager.commitTransaction( em );
        }
        catch ( Exception ex )
        {
            daoManager.rollBackTransaction( em );
            LOG.error( "ConfigDataService saveConfig:" + ex.toString() );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    @Override
    public ConfigEntity getHubConfig( String peerId )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            ConfigEntity configuration = em.find( ConfigEntity.class, peerId );
            return configuration;
        }
        catch ( Exception ex )
        {
            LOG.error( "ConfigDataService getConfig:" + ex.toString() );
            return null;
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }
}
