package io.subutai.core.security.impl.dao;


import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.security.impl.model.TrustItem;
import io.subutai.core.security.impl.model.TrustRelation;


/**
 * Created by talas on 12/8/15.
 */
public class TrustRelationDAO
{
    private static final Logger logger = LoggerFactory.getLogger( TrustRelationDAO.class );
    private DaoManager daoManager = null;

    //CRUD


    public TrustRelationDAO( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    public void persist( TrustRelation trustRelation )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            daoManager.startTransaction( em );
            em.persist( trustRelation );
            daoManager.commitTransaction( em );
        }
        catch ( Exception ex )
        {
            daoManager.rollBackTransaction( em );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    public void update( TrustRelation trustRelation )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            daoManager.startTransaction( em );
            em.merge( trustRelation );
            daoManager.commitTransaction( em );
        }
        catch ( Exception ex )
        {
            logger.error( "Error persisting object", ex );
            daoManager.rollBackTransaction( em );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    public void update( TrustItem trustItem )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            daoManager.startTransaction( em );
            em.merge( trustItem );
            daoManager.commitTransaction( em );
        }
        catch ( Exception ex )
        {
            logger.error( "Error persisting object", ex );
            daoManager.rollBackTransaction( em );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    public void remove( long trustRelationId )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            daoManager.startTransaction( em );

            Query qr = em.createQuery( "DELETE FROM TrustRelation AS ss where ss.id=:id" );
            qr.setParameter( "id", trustRelationId );
            qr.executeUpdate();

            daoManager.commitTransaction( em );
        }
        catch ( Exception ex )
        {
            daoManager.rollBackTransaction( em );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    public TrustRelation find( long trustRelationId )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            return em.find( TrustRelation.class, trustRelationId );
        }
        catch ( Exception ex )
        {
            return null;
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    public void findBySource()
    {
    }


    public void findByTarget()
    {
    }


    public void findAllRelationships()
    {
    }


    public void findByTrustedItem()
    {
    }


    public void findBySourceAndTrustItem()
    {
    }


    public void findByTargetAndTrustItem()
    {
    }
}
