package io.subutai.core.hubmanager.impl.adapter;


import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.dao.DaoManager;


class DaoHelper
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private final DaoManager daoManager;


    DaoHelper( DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    String getPeerOwnerId( String peerId )
    {
        return executeQuery( " select user_id from h_config where peer_id = ? ", peerId );
    }


    boolean isPeerRegisteredToHub( String peerId )
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

            return (T) q.getSingleResult();
        }
        catch ( Exception e )
        {
            log.error( "Error to execute query: ", e );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }

        return null;
    }

}
