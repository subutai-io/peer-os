package io.subutai.core.keyserver.impl.dao;


import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.keyserver.api.dao.KeyServerDAO;
import io.subutai.core.keyserver.api.model.PublicKeyStore;

/**
 * Manages Data Storage
 */

public class KeyServerDAOImpl implements KeyServerDAO
{

    private static final Logger LOG = LoggerFactory.getLogger( KeyServerDAOImpl.class );

    private DaoManager daoManager = null;


    /********************************
     *
     */
    public KeyServerDAOImpl( DaoManager daoManager)
    {
        this.daoManager = daoManager;
    }

    /********************************
     *
     */
    @Override
    public PublicKeyStore findByFingerprint( final String fingerprint )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            Query query = em.createQuery( "select SK from PublicKeyStoreEntity as SK where SK.fingerprint=:fingerprint" );
            query.setParameter( "fingerprint", fingerprint);
            PublicKeyStore securityKey  = (PublicKeyStore)query.getSingleResult();

            return securityKey;
        }
        catch(Exception ex)
        {
            LOG.error( "KeyManagerDAOImpl findByFingerprint:"+ex.toString() );
            return null;
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }

    /********************************
     *
     */
    @Override
    public PublicKeyStore findByShortKeyId( final String shortKeyId )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            Query query = em.createQuery( "select SK from PublicKeyStoreEntity as SK where SK.shortKeyId=:shortKeyId" );
            query.setParameter( "shortKeyId", shortKeyId);
            List<PublicKeyStore> securityKeyList  = query.getResultList();

            if(securityKeyList!=null)
            {
                return securityKeyList.get( 0 );
            }
            else
            {
                return null;
            }
        }
        catch(Exception ex)
        {
            LOG.error( "KeyManagerDAOImpl findByShortKeyId:"+ex.toString() );
            return null;
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    /********************************
     *
     */
    @Override
    public PublicKeyStore find( final String keyId )
    {
        return findByKeyId( keyId );
    }


    /********************************
     *
     */
    @Override
    public PublicKeyStore findByKeyId( final String keyId )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            PublicKeyStore securityKey  = em.find(PublicKeyStore.class,keyId);

            return securityKey;
        }
        catch(Exception ex)
        {
            LOG.error( "KeyManagerDAOImpl findByKeyId:"+ex.toString() );
            return null;
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    /********************************
     *
     */
    @Override
    public List<PublicKeyStore> findAll()
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            Query query = em.createQuery( "select SK from PublicKeyStoreEntity as SK" );
            List<PublicKeyStore> securityKeyList  = query.getResultList();

            return securityKeyList;
        }
        catch(Exception ex)
        {
            LOG.error( "KeyManagerDAOImpl findByAll:"+ex.toString() );
            return null;
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    /********************************
     *
     */
    @Override
    public void save( final PublicKeyStore keyStore )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            daoManager.startTransaction( em );
            em.persist( keyStore );
            daoManager.commitTransaction( em );

        }
        catch(Exception ex)
        {
            LOG.error( "KeyManagerDAOImpl save:"+ex.toString() );
            daoManager.rollBackTransaction( em );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    /********************************
     *
     */
    @Override
    public void update( final PublicKeyStore keyStore )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            daoManager.startTransaction( em );
            em.merge( keyStore );
            daoManager.commitTransaction( em );

        }
        catch(Exception ex)
        {
            LOG.error( "KeyManagerDAOImpl save:"+ex.toString() );
            daoManager.rollBackTransaction( em );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    /********************************
     *
     */
    @Override
    public void delete( final PublicKeyStore keyStore )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            daoManager.startTransaction( em );
            em.remove ( keyStore );
            daoManager.commitTransaction( em );

        }
        catch(Exception ex)
        {
            LOG.error( "KeyManagerDAOImpl delete:"+ex.toString() );
            daoManager.rollBackTransaction( em );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    /********************************
     *
     */
    @Override
    public void deleteByKeyId( final String keyId )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            daoManager.startTransaction( em );

            Query query = em.createQuery( "delete from PublicKeyStoreEntity as SK where SK.keyId=:keyId" );
            query.setParameter( "keyId", keyId );
            query.executeUpdate();

            daoManager.commitTransaction( em );

        }
        catch(Exception ex)
        {
            LOG.error( "KeyManagerDAOImpl deleteById:"+ex.toString() );
            daoManager.rollBackTransaction( em );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    /********************************
     *
     */
    public DaoManager getDaoManager()
    {
        return daoManager;
    }


    /********************************
     *
     */
    public void setDaoManager( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }
}
