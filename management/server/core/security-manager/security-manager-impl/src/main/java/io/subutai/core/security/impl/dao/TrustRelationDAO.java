package io.subutai.core.security.impl.dao;


import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.security.api.model.TrustItem;
import io.subutai.core.security.api.model.TrustRelation;
import io.subutai.core.security.impl.model.TrustItemImpl;
import io.subutai.core.security.impl.model.TrustRelationImpl;


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


    public void persist( TrustRelationImpl trustRelation )
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


    public void update( TrustRelationImpl trustRelation )
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


    public void update( TrustItemImpl trustItem )
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


    public TrustRelationImpl find( long trustRelationId )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            return em.find( TrustRelationImpl.class, trustRelationId );
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


    public void findByObject()
    {
    }


    public TrustRelation findBySourceAndObject( final TrustItemImpl source, final TrustItemImpl object )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();
        TrustRelation result = null;
        try
        {
            Query qr = em.createQuery( "select ss from TrustRelationImpl AS ss"
                    + " where ss.source=:source AND ss.trustedObject=:trustedObject" );
            qr.setParameter( "source", source );
            qr.setParameter( "trustedObject", object );
            List<TrustRelation> list = qr.getResultList();

            if ( list.size() > 0 )
            {
                result = list.get( 0 );
            }
        }
        catch ( Exception ex )
        {
            logger.warn( "Error querying for trust relation.", ex );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
        return result;
    }


    public void findByTargetAndObject()
    {
    }


    public TrustItem findTrustItem( final String uniqueIdentifier, final String classPath )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();
        TrustItem result = null;
        try
        {
            Query qr = em.createQuery( "select ss from TrustItemImpl AS ss"
                    + " where ss.uniqueIdentifier=:uniqueIdentifier AND ss.classPath=:classPath" );
            qr.setParameter( "uniqueIdentifier", uniqueIdentifier );
            qr.setParameter( "classPath", classPath );
            List<TrustItem> list = qr.getResultList();

            if ( list.size() > 0 )
            {
                result = list.get( 0 );
            }
        }
        catch ( Exception ex )
        {
            logger.warn( "Error querying for trust item.", ex );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
        return result;
    }
}
