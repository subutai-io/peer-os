package io.subutai.core.bazaarmanager.impl.dao;


import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.bazaarmanager.api.dao.ConfigDataService;
import io.subutai.core.bazaarmanager.api.model.Config;
import io.subutai.core.bazaarmanager.impl.model.ConfigEntity;


public class ConfigDataServiceImpl implements ConfigDataService
{
    private static final Logger LOG = LoggerFactory.getLogger( ConfigDataServiceImpl.class );
    private DaoManager daoManager;


    public ConfigDataServiceImpl( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    public String getPeerOwnerId( String peerId )
    {
        return executeQuery( " select user_id from h_config where peer_id = ? ", peerId );
    }


    public boolean isPeerRegisteredToBazaar( String peerId )
    {
        Integer count = executeQuery( " select count(*) from h_config where peer_id = ? ", peerId );

        return count != null && count > 0;
    }


    private <T> T executeQuery( String sql, String peerId )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            Query q = em.createNativeQuery( sql );

            q.setParameter( 1, peerId );

            return ( T ) q.getSingleResult();
        }
        catch ( Exception e )
        {
            LOG.error( "Error to execute query: ", e );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }

        return null;
    }


    @Override
    public void saveBazaarConfig( final Config config )
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
            LOG.error( "ConfigDataService saveConfig: {}", ex.getMessage() );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    @Override
    public ConfigEntity getBazaarConfig( String peerId )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            return em.find( ConfigEntity.class, peerId );
        }
        catch ( Exception ex )
        {
            LOG.error( "ConfigDataService getConfig: {}", ex.getMessage() );
            return null;
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    @Override
    public void deleteConfig( final String peerId )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();

        try
        {
            daoManager.startTransaction( em );
            ConfigEntity entity = em.find( ConfigEntity.class, peerId );
            em.remove( entity );
            em.flush();
            daoManager.commitTransaction( em );
        }
        catch ( Exception ex )
        {
            daoManager.rollBackTransaction( em );
            LOG.error( "ConfigDataService deleteOperation: {}", ex.getMessage() );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }
}
