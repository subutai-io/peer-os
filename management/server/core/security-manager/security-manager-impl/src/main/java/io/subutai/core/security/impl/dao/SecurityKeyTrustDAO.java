package io.subutai.core.security.impl.dao;


import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.security.api.model.SecurityKeyTrust;
import io.subutai.core.security.impl.model.SecurityKeyTrustEntity;


/**
 *
 */
class SecurityKeyTrustDAO
{
    private DaoManager daoManager = null;


    SecurityKeyTrustDAO( DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    SecurityKeyTrust find( long id )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {

            return em.find( SecurityKeyTrustEntity.class, id );
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


    /******************************************
     *
     */
    List<SecurityKeyTrust> findBySourceId( String fingerprint )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            TypedQuery<SecurityKeyTrust> qr =
                    em.createQuery( "select st from SecurityKeyTrustEntity AS st where st.sourceFingerprint=:sourceId",
                            SecurityKeyTrust.class );
            qr.setParameter( "sourceId", fingerprint );

            return qr.getResultList();
        }
        catch ( Exception ex )
        {
            return Collections.emptyList();
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    /******************************************
     *
     */
    SecurityKeyTrust findBySourceId( String sourceId, String targetId )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            TypedQuery<SecurityKeyTrust> qr = em.createQuery( "select st from SecurityKeyTrustEntity AS st where "
                    + "st.sourceFingerprint=:sourceId and st.targetFingerprint=:targetId", SecurityKeyTrust.class );
            qr.setParameter( "sourceId", sourceId );
            qr.setParameter( "targetId", targetId );

            List<SecurityKeyTrust> trusts = qr.getResultList();

            return trusts.get( 0 );
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


    /******************************************
     *
     */
    void persist( SecurityKeyTrust SecurityKeyTrust )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            daoManager.startTransaction( em );
            em.persist( SecurityKeyTrust );
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


    /******************************************
     *
     */
    void update( SecurityKeyTrust SecurityKeyTrust )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            daoManager.startTransaction( em );
            em.merge( SecurityKeyTrust );
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


    /******************************************
     *
     */
    void remove( long id )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            daoManager.startTransaction( em );

            Query qr = em.createQuery( "delete from SecurityKeyTrustEntity AS ss where ss.id=:id" );
            qr.setParameter( "id", id );
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


    /******************************************
     *
     */
    void removeAll( String fingerprint )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            daoManager.startTransaction( em );

            Query qr = em.createQuery( "delete from SecurityKeyTrustEntity AS ss where "
                    + " ss.sourceFingerprint=:Fingerprint or ss.targetFingerprint=:Fingerprint " );
            qr.setParameter( "Fingerprint", fingerprint );
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


    /******************************************
     *
     */
    void removeBySourceId( String sourceId )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            daoManager.startTransaction( em );

            Query qr =
                    em.createQuery( "delete from SecurityKeyTrustEntity AS ss where ss.sourceFingerprint=:sourceId" );
            qr.setParameter( "sourceId", sourceId );
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


    /******************************************
     *
     */
    void removeBySourceId( String sourceId, String targetId )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            daoManager.startTransaction( em );

            Query qr = em.createQuery(
                    "delete from SecurityKeyTrustEntity AS ss where ss.sourceFingerprint=:sourceId and "
                            + " ss.targetFingerprint=:targetId" );
            qr.setParameter( "sourceId", sourceId );
            qr.setParameter( "targetId", targetId );
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
}
