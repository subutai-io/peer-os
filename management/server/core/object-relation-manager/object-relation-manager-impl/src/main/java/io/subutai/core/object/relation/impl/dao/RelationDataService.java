package io.subutai.core.object.relation.impl.dao;


import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.security.objects.Ownership;
import io.subutai.common.security.relation.RelationLink;
import io.subutai.common.security.relation.model.Relation;
import io.subutai.core.object.relation.impl.model.RelationChallengeImpl;
import io.subutai.core.object.relation.impl.model.RelationImpl;
import io.subutai.core.object.relation.impl.model.RelationLinkImpl;


@SuppressWarnings( "JpaQlInspection" )
public class RelationDataService
{
    private static final Logger logger = LoggerFactory.getLogger( RelationDataService.class );
    private DaoManager daoManager = null;


    public RelationDataService( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    public void save( Object relationLink )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            daoManager.startTransaction( em );
            em.persist( relationLink );
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


    public void update( Object relationLink )
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


    public void updateBatch( Set<Object> relationLinks )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            daoManager.startTransaction( em );

            for ( Object relationLink : relationLinks )
            {
                em.merge( relationLink );
            }

            daoManager.commitTransaction( em );
        }
        catch ( Exception ex )
        {
            daoManager.rollBackTransaction( em );

            logger.error( "Error updating relations", ex );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    public void saveBatch( List<Object> relationLinks )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            daoManager.startTransaction( em );

            for ( Object relationLink : relationLinks )
            {
                em.persist( relationLink );
            }

            daoManager.commitTransaction( em );
        }
        catch ( Exception ex )
        {
            daoManager.rollBackTransaction( em );

            logger.error( "Error updating relations", ex );
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


    public void removeAllRelationsWithLink( RelationLink link )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            daoManager.startTransaction( em );

            Query qr = em.createQuery( "DELETE FROM RelationImpl AS rln"
                    + " WHERE rln.source.uniqueIdentifier=:id"
                    + " OR rln.target.uniqueIdentifier=:id"
                    + " OR rln.trustedObject.uniqueIdentifier=:id" );
            qr.setParameter( "id", link.getUniqueIdentifier() );
            qr.executeUpdate();

            qr = em.createQuery( "DELETE FROM RelationLinkImpl AS link"
                    + " WHERE link.uniqueIdentifier=:id" );
            qr.setParameter( "id", link.getUniqueIdentifier() );
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


    public List<Relation> findBySource( final RelationLink source )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();
        List<Relation> result = Lists.newArrayList();
        try
        {
            Query qr = em.createQuery( "select ss from RelationImpl AS ss"
                    + " where ss.source.uniqueIdentifier=:source ORDER BY ss.relationStatus DESC" );
            qr.setParameter( "source", source.getUniqueIdentifier() );
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


    public List<Relation> findByTarget( final RelationLink target )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();
        List<Relation> result = Lists.newArrayList();
        try
        {
            Query qr = em.createQuery( "SELECT ss FROM RelationImpl AS ss"
                    + " WHERE ss.target.uniqueIdentifier=:target ORDER BY ss.relationStatus DESC" );
            qr.setParameter( "target", target.getUniqueIdentifier() );
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


    public List<Relation> getTrustedRelationsByOwnership( final RelationLink trustedObject, Ownership ownership )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();
        List<Relation> result = Lists.newArrayList();
        try
        {
            Query qr = em.createQuery(
                    "select ss from RelationImpl AS ss" + " where ss.trustedObject.uniqueIdentifier=:trustedObject "
                            + "and ss.relationInfo.ownershipLevel=:ownershipLevel ORDER BY ss.relationStatus DESC" );
            qr.setParameter( "trustedObject", trustedObject.getUniqueIdentifier() );
            qr.setParameter( "ownershipLevel", ownership.getLevel() );
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


    public List<Relation> getAllRelations()
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();
        List<Relation> result = Lists.newArrayList();
        try
        {
            Query qr = em.createQuery( "SELECT ss FROM RelationImpl AS ss ORDER BY ss.relationStatus DESC" );
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


    public List<Relation> findByObject( final RelationLink object )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();
        List<Relation> result = Lists.newArrayList();
        try
        {
            Query qr = em.createQuery( "SELECT ss FROM RelationImpl AS ss"
                    + " WHERE ss.trustedObject.uniqueIdentifier=:trustedObject ORDER BY ss.relationStatus DESC" );
            qr.setParameter( "trustedObject", object.getUniqueIdentifier() );
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
            Query qr = em.createQuery( "SELECT ss FROM RelationImpl AS ss"
                    + " where ss.source.uniqueIdentifier=:source AND ss.trustedObject.uniqueIdentifier=:trustedObject" );
            qr.setParameter( "source", source.getUniqueIdentifier() );
            qr.setParameter( "trustedObject", object.getUniqueIdentifier() );
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


    public Relation findBySourceTargetObject( final RelationLink source, final RelationLink target,
                                              final RelationLink object )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();
        Relation result = null;
        try
        {
            Query qr = em.createQuery( "select ss from RelationImpl AS ss"
                    + " WHERE ss.source.uniqueIdentifier=:source"
                    + " AND ss.target.uniqueIdentifier=:target"
                    + " AND ss.trustedObject.uniqueIdentifier=:trustedObject" );
            qr.setParameter( "source", source.getUniqueIdentifier() );
            qr.setParameter( "target", target.getUniqueIdentifier() );
            qr.setParameter( "trustedObject", object.getUniqueIdentifier() );
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
            Query qr = em.createQuery( "SELECT ss FROM RelationImpl AS ss"
                    + " WHERE ss.source.uniqueIdentifier=:source"
                    + " AND ss.trustedObject.uniqueIdentifier=:trustedObject"
                    + " ORDER BY ss.relationStatus DESC" );
            qr.setParameter( "source", source.getUniqueIdentifier() );
            qr.setParameter( "trustedObject", object.getUniqueIdentifier() );
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
            Query qr = em.createQuery( "SELECT ss FROM RelationImpl AS ss"
                    + " WHERE ss.target.uniqueIdentifier=:target"
                    + " AND ss.trustedObject.uniqueIdentifier=:trustedObject" );
            qr.setParameter( "target", target.getUniqueIdentifier() );
            qr.setParameter( "trustedObject", object.getUniqueIdentifier() );
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
            Query qr = em.createQuery( "SELECT ss FROM RelationImpl AS ss"
                    + " WHERE ss.target.uniqueIdentifier=:target"
                    + " AND ss.trustedObject.uniqueIdentifier=:trustedObject"
                    + " ORDER BY ss.relationStatus DESC" );
            qr.setParameter( "target", target.getUniqueIdentifier() );
            qr.setParameter( "trustedObject", object.getUniqueIdentifier() );
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


    public RelationLink findRelationLinkByIdClass( final RelationLink relationLink )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();
        RelationLink result = null;
        try
        {
            Query qr = em.createQuery( "SELECT ss FROM RelationLinkImpl AS ss"
                    + " WHERE ss.uniqueIdentifier=:uniqueIdentifier"
                    + " AND ss.classPath=:classPath" );
            qr.setParameter( "uniqueIdentifier", relationLink.getUniqueIdentifier() );
            qr.setParameter( "classPath", relationLink.getClassPath() );
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


    public RelationLink getRelationLinkByUniqueId( final String uniqueIdentifier )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();
        RelationLink result = null;
        try
        {
            Query qr = em.createQuery(
                    "SELECT ss FROM RelationLinkImpl AS ss" + " WHERE ss.uniqueIdentifier=:uniqueIdentifier" );
            qr.setParameter( "uniqueIdentifier", uniqueIdentifier );
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


    public RelationChallengeImpl getRelationToken( final String token )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();
        RelationChallengeImpl result = null;
        try
        {
            TypedQuery<RelationChallengeImpl> qr =
                    em.createQuery( "SELECT rt FROM RelationChallengeImpl AS rt" + " where rt" + ".token=:token",
                            RelationChallengeImpl.class );
            qr.setParameter( "token", token );
            List<RelationChallengeImpl> list = qr.getResultList();

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
