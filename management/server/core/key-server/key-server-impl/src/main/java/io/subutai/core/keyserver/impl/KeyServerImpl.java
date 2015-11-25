package io.subutai.core.keyserver.impl;


import io.subutai.common.dao.DaoManager;
import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.core.keyserver.api.KeyServer;
import io.subutai.core.keyserver.api.dao.KeyServerDAO;
import io.subutai.core.keyserver.api.model.PublicKeyStore;
import io.subutai.core.keyserver.impl.dao.KeyServerDAOImpl;
import io.subutai.core.keyserver.impl.model.PublicKeyStoreEntity;
import io.subutai.core.keyserver.impl.utils.SecurityKeyUtil;

import java.io.IOException;
import java.util.List;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Manages Security key.
 */
public class KeyServerImpl implements KeyServer
{

    private static final Logger LOG = LoggerFactory.getLogger( KeyServerImpl.class );
    private DaoManager daoManager = null;
    private KeyServerDAO keyServerDAO = null;


    /********************************
     *
     */
    public void init()
    {
        keyServerDAO = new KeyServerDAOImpl( daoManager );
    }


    /********************************
     *
     */
    public void destroy()
    {

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


    /********************************
     *
     */
    @Override
    public PublicKeyStore getPublicKeyByFingerprint( String fingerprint )
    {
        return keyServerDAO.findByFingerprint( fingerprint );
    }


    /********************************
     *
     */
    @Override
    public PublicKeyStore getPublicKeyByShortKeyId( String shortKeyId )
    {
        return keyServerDAO.findByShortKeyId( shortKeyId );
    }


    /********************************
     *
     */
    @Override
    public PublicKeyStore getPublicKeyByKeyId( String keyId )
    {
        return keyServerDAO.findByKeyId( keyId );
    }


    /********************************
     *
     */
    @Override
    public PublicKeyStore getPublicKey( String keyId )
    {
        return keyServerDAO.find( keyId );
    }


    /********************************
     *
     */
    @Override
    public List<PublicKeyStore> getPublicKeyList()
    {
        return keyServerDAO.findAll();
    }


    /* *******************************
     *
     */
    @Override
    public void addPublicKey( String key ) throws PGPException, IOException
    {
        PGPPublicKey publicKey  = PGPKeyUtil.readPublicKey( key );
        PublicKeyStore securityKey = SecurityKeyUtil.convert( publicKey );

        keyServerDAO.save( securityKey );
    }


    /********************************
     *
     */
    @Override
    public void addPublicKey( PGPPublicKeyRing publicRing ) throws PGPException, IOException
    {
        PublicKeyStore securityKey = SecurityKeyUtil.convert( publicRing );

        keyServerDAO.update ( securityKey );
    }


    /* *******************************
     *
     */
    @Override
    public PGPPublicKeyRing addPublicKeyRing( String keyRing ) throws PGPException, IOException
    {
        PGPPublicKeyRing publicKeyRing = PGPKeyUtil.readPublicKeyRing( keyRing );
        PublicKeyStore publicKeyStore  = SecurityKeyUtil.convert( publicKeyRing );

        keyServerDAO.save( publicKeyStore );

        return publicKeyRing;
    }


    /********************************
     *
     */
    @Override
    public void savePublicKey( PublicKeyStore publicKey )
    {
        keyServerDAO.save( publicKey );
    }


    /* *******************************
     *
     */
    @Override
    public void savePublicKey( String keyId, String fingerprint, short keyType, byte[] keyData )
    {
        PublicKeyStore publicKeyStore = new PublicKeyStoreEntity();

        publicKeyStore.setKeyId( keyId );
        publicKeyStore.setFingerprint( fingerprint );
        publicKeyStore.setKeyType( keyType );
        publicKeyStore.setKeyData( keyData );

        keyServerDAO.save( publicKeyStore );
    }


    /* *******************************
     *
     */
    @Override
    public void removePublicKey( PublicKeyStore securityKey )
    {
        keyServerDAO.delete( securityKey );
    }


    /* *******************************
     *
     */
    @Override
    public void removePublicKeyByKeyId( String keyId )
    {
        keyServerDAO.deleteByKeyId( keyId );
    }


    /* *******************************
     *
     */
    @Override
    public KeyServerDAO getKeyServerDAO()
    {
        return keyServerDAO;
    }


    /* *******************************
     *
     */
    public void setKeyServerDAO( KeyServerDAO keyServerDAO )
    {
        this.keyServerDAO = keyServerDAO;
    }

}