package io.subutai.core.security.impl.dao;


import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.security.api.model.SecurityKey;
import io.subutai.core.security.impl.model.SecurityKeyEntity;


/**
 *
 */
class SecurityKeyDAO
{
    private DaoManager daoManager = null;

    /******************************************
     *
     */
    public SecurityKeyDAO( DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    /******************************************
     * Get Security KeyId from DB
     */
    public SecurityKey find( String identityId )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            SecurityKey SecurityKey = em.find( SecurityKeyEntity.class, identityId );

            return SecurityKey;
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
    public SecurityKey findByFingerprint( String fingerprint )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();
        SecurityKey key = null;
        try
        {
            Query qr = em.createQuery( "select ss from SecurityKeyEntity AS ss"
                            + " where ss.publicKeyFingerprint=:publicKeyFingerprint" );
            qr.setParameter( "publicKeyFingerprint", fingerprint );
            List<SecurityKeyEntity> result = qr.getResultList();

            if ( result.size() > 0 )
            {
                key = result.get( 0 );
            }
        }
        catch ( Exception ex )
        {
            //ignore
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
        return key;
    }



    /******************************************
     *
     */
    public void persist(SecurityKey SecurityKey )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            daoManager.startTransaction( em );
            em.persist( SecurityKey );
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
    public void update(SecurityKey SecurityKey )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            daoManager.startTransaction( em );
            em.merge( SecurityKey );
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
    public void remove( String identityId )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            daoManager.startTransaction( em );

            Query qr = em.createQuery( "delete from SecurityKeyEntity AS ss where ss.identityId=:identityId" );
            qr.setParameter( "identityId",identityId );
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
