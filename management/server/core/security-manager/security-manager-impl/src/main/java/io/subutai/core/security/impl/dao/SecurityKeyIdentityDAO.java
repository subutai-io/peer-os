package io.subutai.core.security.impl.dao;


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
