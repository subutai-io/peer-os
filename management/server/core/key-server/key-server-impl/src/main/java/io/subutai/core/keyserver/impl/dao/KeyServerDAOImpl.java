package io.subutai.core.keyserver.impl.dao;


import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.keyserver.api.dao.KeyServerDAO;
import io.subutai.core.keyserver.api.model.SecurityKey;
import io.subutai.core.keyserver.impl.model.SecurityKeyEntity;


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
    public SecurityKey findByFingerprint( final String fingerprint )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            Query query = em.createQuery( "select SK from SecurityKeyEntity as SK where SK.fingerprint=:fingerprint" );
            query.setParameter( "fingerprint", fingerprint);
            SecurityKey securityKey  = (SecurityKey)query.getSingleResult();

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
    public SecurityKey find( final String keyId )
    {
        return findByKeyId( keyId );
    }


    /********************************
     *
     */
    @Override
    public SecurityKey findByKeyId( final String keyId )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            SecurityKey securityKey  = em.find(SecurityKeyEntity.class,keyId);

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
    public List<SecurityKey> findAll()
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            Query query = em.createQuery( "select SK from SecurityKeyEntity as SK" );
            List<SecurityKey> securityKeyList  = query.getResultList();

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
    public void save( final SecurityKey securityKey )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            daoManager.startTransaction( em );
            em.persist( securityKey );
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
    public void delete( final SecurityKey securityKey )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            daoManager.startTransaction( em );
            em.remove ( securityKey );
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

            Query query = em.createQuery( "delete from SecurityKeyEntity as SK where SK.keyId=:keyId" );
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
