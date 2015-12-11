package io.subutai.core.identity.impl.dao;


import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.identity.api.model.Relation;
import io.subutai.core.identity.api.model.RelationLink;
import io.subutai.core.identity.impl.model.RelationImpl;
import io.subutai.core.identity.impl.model.RelationLinkImpl;


/**
 * Created by talas on 12/8/15.
 */
public class RelationDAO
{
    private static final Logger logger = LoggerFactory.getLogger( RelationDAO.class );
    private DaoManager daoManager = null;

    //CRUD


    public RelationDAO( final DaoManager daoManager )
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

            Query qr = em.createQuery( "DELETE FROM RelationImpl AS ss where ss.id=:id" );
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


    public List<Relation> findBySource( final RelationLinkImpl source )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();
        List<Relation> result = Lists.newArrayList();
        try
        {
            Query qr = em.createQuery( "select ss from RelationImpl AS ss" + " where ss.source=:source" );
            qr.setParameter( "source", source );
            result.addAll( qr.getResultList() );
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


    public List<Relation> findByTarget( final RelationLinkImpl target )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();
        List<Relation> result = Lists.newArrayList();
        try
        {
            Query qr = em.createQuery( "select ss from RelationImpl AS ss" + " where ss.target=:target" );
            qr.setParameter( "target", target );
            result.addAll( qr.getResultList() );
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


    public void findAllRelationships()
    {
    }


    public List<Relation> findByObject( final RelationLinkImpl object )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();
        List<Relation> result = Lists.newArrayList();
        try
        {
            Query qr = em.createQuery( "select ss from RelationImpl AS ss" + " where ss.trustedObject=:trustedObject" );
            qr.setParameter( "trustedObject", object );
            result.addAll( qr.getResultList() );
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


    public Relation findBySourceAndObject( final RelationLinkImpl source, final RelationLinkImpl object )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();
        Relation result = null;
        try
        {
            Query qr = em.createQuery( "select ss from RelationImpl AS ss"
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


    public List<Relation> relationsBySourceAndObject( final RelationLinkImpl source, final RelationLinkImpl object )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();
        List<Relation> result = Lists.newArrayList();
        try
        {
            Query qr = em.createQuery( "select ss from RelationImpl AS ss"
                    + " where ss.source=:source AND ss.trustedObject=:trustedObject" );
            qr.setParameter( "source", source );
            qr.setParameter( "trustedObject", object );
            result.addAll( qr.getResultList() );
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


    public Relation relationByTargetAndObject( final RelationLinkImpl target, final RelationLinkImpl object )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();
        Relation result = null;
        try
        {
            Query qr = em.createQuery( "select ss from RelationImpl AS ss"
                    + " where ss.target=:target AND ss.trustedObject=:trustedObject" );
            qr.setParameter( "target", target );
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


    public List<Relation> relationsByTargetAndObject( final RelationLinkImpl target, final RelationLinkImpl object )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();
        List<Relation> result = Lists.newArrayList();
        try
        {
            Query qr = em.createQuery( "select ss from RelationImpl AS ss"
                    + " where ss.target=:target AND ss.trustedObject=:trustedObject" );
            qr.setParameter( "target", target );
            qr.setParameter( "trustedObject", object );
            result.addAll( qr.getResultList() );
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


    public RelationLink findRelationLink( final String uniqueIdentifier, final String classPath )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();
        RelationLink result = null;
        try
        {
            Query qr = em.createQuery( "select ss from RelationLinkImpl AS ss"
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
