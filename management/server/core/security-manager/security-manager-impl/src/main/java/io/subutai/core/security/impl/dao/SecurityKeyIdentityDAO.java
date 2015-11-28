package io.subutai.core.security.impl.dao;


import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.security.api.model.SecurityKeyIdentity;
import io.subutai.core.security.impl.model.SecurityKeyIdentityEntity;


/**
 *
 */
class SecurityKeyIdentityDAO
{
    private DaoManager daoManager = null;

    /******************************************
     *
     */
    public SecurityKeyIdentityDAO( DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    /******************************************
     * Get Security KeyId from DB
     */
    public SecurityKeyIdentity find( String identityId )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            SecurityKeyIdentity securityKeyIdentity = em.find( SecurityKeyIdentityEntity.class, identityId );

            return securityKeyIdentity;
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
    public SecurityKeyIdentity findByFingerprint( String fingerprint )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();
        SecurityKeyIdentity key = null;
        try
        {
            Query qr = em.createQuery( "select ss from SecurityKeyIdentityEntity AS ss"
                            + " where ss.publicKeyFingerprint=:publicKeyFingerprint" );
            qr.setParameter( "publicKeyFingerprint", fingerprint );
            List<SecurityKeyIdentityEntity> result = qr.getResultList();

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
    public void persist(SecurityKeyIdentity securityKeyIdentity )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            daoManager.startTransaction( em );
            em.persist( securityKeyIdentity );
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
    public void update(SecurityKeyIdentity securityKeyIdentity )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            daoManager.startTransaction( em );
            em.merge( securityKeyIdentity );
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

            Query qr = em.createQuery( "delete from SecurityKeyIdentityEntity AS ss where ss.identityId=:identityId" );
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
