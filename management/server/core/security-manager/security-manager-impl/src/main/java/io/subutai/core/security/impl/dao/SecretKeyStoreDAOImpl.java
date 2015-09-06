package io.subutai.core.security.impl.dao;


import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.security.api.dao.SecretKeyStoreDAO;
import io.subutai.core.security.api.model.SecretKeyStore;
import io.subutai.core.security.api.model.SecurityKeyIdentity;
import io.subutai.core.security.impl.model.SecretKeyStoreEntity;
import io.subutai.core.security.impl.model.SecurityKeyIdentityEntity;


/**
 * Implementation of SecretKeyStore DAO
 */
public class SecretKeyStoreDAOImpl implements SecretKeyStoreDAO
{
    private static final Logger LOG = LoggerFactory.getLogger( SecurityManagerDAOImpl.class );

    private DaoManager daoManager = null;

    /******************************************
     *
     */
    public SecretKeyStoreDAOImpl( DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }

    /******************************************
     *
     */
    @Override
    public void saveSecretKeyRing(SecretKeyStore secretKeyStore)
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            daoManager.startTransaction( em );

            em.merge( secretKeyStore );

            daoManager.commitTransaction( em );

        }
        catch ( Exception ex )
        {
            daoManager.rollBackTransaction( em );
            LOG.error( "SecretKeyDAOImpl saveKey:" + ex.toString() );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    /******************************************
     *
     */
    @Override
    public void saveSecretKeyRing(String fingerprint,byte[] data, String pwd,short type)
    {
        SecretKeyStore secretKeyStore = new SecretKeyStoreEntity();

        secretKeyStore.setKeyFingerprint( fingerprint );
        secretKeyStore.setData( data );
        secretKeyStore.setPwd( pwd );
        secretKeyStore.setType( type );

        saveSecretKeyRing( secretKeyStore);
    }


    /******************************************
     *
     */
    @Override
    public void removeSecretKeyRing( String fingerprint )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            daoManager.startTransaction( em );

            em.remove( fingerprint);

            daoManager.commitTransaction( em );
        }
        catch ( Exception ex )
        {
            daoManager.rollBackTransaction( em );
            LOG.error( "SecretKeyStoreDAOImpl removeKey:" + ex.toString() );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    /******************************************
     * Get Secret KeyId from DB
     */
    @Override
    public SecretKeyStore getSecretKeyData( String fingerprint )
    {
        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();

        try
        {
            SecretKeyStore secretKeyStore = em.find( SecretKeyStoreEntity.class, fingerprint );

            return secretKeyStore;
        }
        catch ( Exception ex )
        {
            LOG.error( "SecretKeyDAOImpl getSecurityKeyId:" + ex.toString() );
            return null;
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }
}
