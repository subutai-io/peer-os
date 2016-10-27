package io.subutai.core.security.impl.dao;


import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import com.google.common.collect.Lists;

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
    SecurityKeyDAO( DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    /******************************************
     * Get Security KeyId from DB
     */
    SecurityKey find( String identityId )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {

            return em.find( SecurityKeyEntity.class, identityId );
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
    SecurityKey findByFingerprint( String fingerprint )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();
        SecurityKey key = null;
        try
        {
            TypedQuery<SecurityKeyEntity> qr = em.createQuery(
                    "select ss from SecurityKeyEntity AS ss" + " where ss.publicKeyFingerprint=:publicKeyFingerprint",
                    SecurityKeyEntity.class );
            qr.setParameter( "publicKeyFingerprint", fingerprint );
            List<SecurityKeyEntity> result = qr.getResultList();

            if ( !result.isEmpty() )
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


    /* *************************************************
     *
     */
    List<SecurityKey> findByType( int keyType )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();

        List<SecurityKey> result = Lists.newArrayList();
        try
        {
            TypedQuery<SecurityKey> qr =
                    em.createQuery( "select h from SecurityKeyEntity h where h.type=:keyType", SecurityKey.class );

            qr.setParameter( "keyType", keyType );
            result = qr.getResultList();
        }
        catch ( Exception e )
        {
            //ignore
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
        return result;
    }


    /******************************************
     *
     */
    void persist( SecurityKey SecurityKey )
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
    void update( SecurityKey SecurityKey )
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
    void remove( String identityId )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            daoManager.startTransaction( em );

            Query qr = em.createQuery( "delete from SecurityKeyEntity AS ss where ss.identityId=:identityId" );
            qr.setParameter( "identityId", identityId );
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
