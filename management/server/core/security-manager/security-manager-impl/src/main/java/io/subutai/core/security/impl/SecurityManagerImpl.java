package io.subutai.core.security.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.keyserver.api.KeyServer;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.EncryptionTool;
import io.subutai.core.security.api.crypto.KeyManager;
import io.subutai.core.security.api.dao.SecurityManagerDAO;
import io.subutai.core.security.impl.crypto.EncryptionToolImpl;
import io.subutai.core.security.impl.crypto.KeyManagerImpl;
import io.subutai.core.security.impl.dao.SecurityManagerDAOImpl;
import io.subutai.core.security.impl.model.SecurityKeyData;


/**
 * Implementation of SecurityManager
 */
public class SecurityManagerImpl implements SecurityManager
{
    private static final Logger LOG = LoggerFactory.getLogger( SecurityManagerImpl.class );

    private KeyManager keyManager = null;
    private DaoManager daoManager = null;
    private EncryptionTool encryptionTool = null;
    private SecurityManagerDAO securityManagerDAO = null;
    private KeyServer keyServer = null;
    private PeerManager peerManager = null;
    private SecurityKeyData keyData= null;

    /* *****************************
     *
     */
    public SecurityManagerImpl()
    {

    }

    /* *****************************
     *
     */
    public SecurityManagerImpl( String secretKeyringFile,
                                String publicKeyringFile,
                                String secretKeyringPwd,
                                String manHostKeyFingerprint)
    {
        keyData = new SecurityKeyData();

        keyData.setManHostKeyFingerprint( manHostKeyFingerprint );
        keyData.setSecretKeyringFile( secretKeyringFile );
        keyData.setPublicKeyringFile( publicKeyringFile);
        keyData.setSecretKeyringPwd( secretKeyringPwd );
    }


    /* *****************************
     *
     */
    public void init()
    {
        keyData.setManHostId( peerManager.getLocalPeerInfo().getId().toString());

        securityManagerDAO = new SecurityManagerDAOImpl( daoManager );
        keyManager = new KeyManagerImpl( securityManagerDAO, keyServer, keyData );
        encryptionTool = new EncryptionToolImpl( ( KeyManagerImpl ) keyManager );
    }


    /* *****************************
     *
     */
    public void destroy()
    {

    }


    /**
     * *****************************
     */
    @Override
    public KeyManager getKeyManager()
    {
        return keyManager;
    }


    /**
     * *****************************
     */
    public void setKeyManager( KeyManager keyManager )
    {
        this.keyManager = keyManager;
    }


    /**
     * *****************************
     */
    public DaoManager getDaoManager()
    {
        return daoManager;
    }


    /* ****************************
     *
     */
    public void setDaoManager( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    /* *****************************
     *
     */
    public SecurityManagerDAO getSecurityManagerDAO()
    {
        return securityManagerDAO;
    }


    /* *****************************
     *
     */
    public void setSecurityManagerDAO( final SecurityManagerDAO securityManagerDAO )
    {
        this.securityManagerDAO = securityManagerDAO;
    }


    /* *****************************
     *
     */
    public KeyServer getKeyServer()
    {
        return keyServer;
    }


    /* *****************************
     *
     */
    public void setKeyServer( final KeyServer keyServer )
    {
        this.keyServer = keyServer;
    }


    /**
     * *****************************
     */
    public EncryptionTool getEncryptionTool()
    {
        return encryptionTool;
    }


    /* *****************************
     *
     */
    public void setEncryptionTool( final EncryptionTool encryptionTool )
    {
        this.encryptionTool = encryptionTool;
    }


    /* *****************************
     *
     */
    public SecurityKeyData getSecurityKeyData()
    {
        return keyData;
    }


    /* *****************************
     *
     */
    public PeerManager getPeerManager()
    {
        return peerManager;
    }


    /* *****************************
     *
     */
    public void setPeerManager( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }
}
