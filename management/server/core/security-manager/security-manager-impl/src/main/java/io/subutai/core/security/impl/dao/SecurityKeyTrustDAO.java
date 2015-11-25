package io.subutai.core.security.impl.dao;


import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.security.api.model.SecurityKeyTrust;
import io.subutai.core.security.impl.model.SecurityKeyTrustEntity;


/**
 *
 */
class SecurityKeyTrustDAO
{
    private DaoManager daoManager = null;

    public SecurityKeyTrustDAO(DaoManager daoManager)
    {
        this.daoManager = daoManager;
    }

    public SecurityKeyTrust find( long id )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            SecurityKeyTrust SecurityKeyTrust = em.find( SecurityKeyTrustEntity.class, id );

            return SecurityKeyTrust;
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
    public List<SecurityKeyTrust> findBySourceId( String fingerprint)
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            Query qr = em.createQuery( "select st from SecurityKeyTrustEntity AS st where st.sourceId=:sourceId" );
            qr.setParameter( "sourceId",fingerprint );
            List<SecurityKeyTrust> trusts = qr.getResultList();

            return trusts;
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
    public void persist(SecurityKeyTrust SecurityKeyTrust )
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
    public void update(SecurityKeyTrust SecurityKeyTrust )
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
    public void remove( long id )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            daoManager.startTransaction( em );

            Query qr = em.createQuery( "delete from SecurityKeyTrustEntity AS ss where ss.id=:id" );
            qr.setParameter( "id",id );
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
    public void removeBySourceId( String sourceId)
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            daoManager.startTransaction( em );

            Query qr = em.createQuery( "delete from SecurityKeyTrustEntity AS ss where ss.sourceId=:sourceId" );
            qr.setParameter( "sourceId",sourceId );
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
    public void removeBySourceId( String sourceId, String targetId)
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            daoManager.startTransaction( em );

            Query qr = em.createQuery( "delete from SecurityKeyTrustEntity AS ss where ss.sourceId=:sourceId and "
                    + " ss.targetId=:targetId " );
            qr.setParameter( "sourceId",sourceId );
            qr.setParameter( "targetId",targetId );
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
