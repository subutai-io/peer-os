package io.subutai.core.identity.impl.dao;


import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.identity.api.model.Relation;
import io.subutai.core.identity.api.model.RelationLink;
import io.subutai.core.identity.impl.model.RelationImpl;
import io.subutai.core.identity.impl.model.RelationLinkImpl;


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


    public void persist( RelationImpl trustRelation )
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


    public void update( Relation relation )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            daoManager.startTransaction( em );
            em.merge( relation );
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


    public void update( RelationLink relationLink )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            daoManager.startTransaction( em );
            em.merge( relationLink );
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

            Query qr = em.createQuery( "DELETE FROM TrustRelationImpl AS ss where ss.id=:id" );
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


    public RelationImpl find( long trustRelationId )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            return em.find( RelationImpl.class, trustRelationId );
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


    public Relation findBySourceAndObject( final RelationLinkImpl source, final RelationLinkImpl object )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();
        Relation result = null;
        try
        {
            Query qr = em.createQuery( "select ss from TrustRelationImpl AS ss"
                    + " where ss.source=:source AND ss.trustedObject=:trustedObject" );
            qr.setParameter( "source", source );
            qr.setParameter( "trustedObject", object );
            List<Relation> list = qr.getResultList();

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


    public RelationLink findTrustItem( final String uniqueIdentifier, final String classPath )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();
        RelationLink result = null;
        try
        {
            Query qr = em.createQuery( "select ss from TrustItemImpl AS ss"
                    + " where ss.uniqueIdentifier=:uniqueIdentifier AND ss.classPath=:classPath" );
            qr.setParameter( "uniqueIdentifier", uniqueIdentifier );
            qr.setParameter( "classPath", classPath );
            List<RelationLink> list = qr.getResultList();

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
