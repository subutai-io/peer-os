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
    private String publicKeyringFile;
    private String secretKeyringFile;
    private String secretKeyringPwd;
    private String manHostId;
    private String manHostKeyFingerprint;
    private PeerManager peerManager;


    /* *****************************
     *
     */
    public void init()
    {
        manHostId = peerManager.getLocalPeerInfo().getId().toString();
        securityManagerDAO = new SecurityManagerDAOImpl( daoManager );

        keyManager = new KeyManagerImpl( securityManagerDAO, keyServer, publicKeyringFile, secretKeyringFile,
                secretKeyringPwd, manHostId, manHostKeyFingerprint );

        encryptionTool = new EncryptionToolImpl( ( KeyManagerImpl ) keyManager );
    }


    /**
     * *****************************
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


    /**
     * *****************************
     */
    public void setDaoManager( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    /**
     * *****************************
     */
    public SecurityManagerDAO getSecurityManagerDAO()
    {
        return securityManagerDAO;
    }


    /**
     * *****************************
     */
    public void setSecurityManagerDAO( final SecurityManagerDAO securityManagerDAO )
    {
        this.securityManagerDAO = securityManagerDAO;
    }


    /**
     * *****************************
     */
    public KeyServer getKeyServer()
    {
        return keyServer;
    }


    /**
     * *****************************
     */
    public void setKeyServer( final KeyServer keyServer )
    {
        this.keyServer = keyServer;
    }


    /**
     * *****************************
     */
    public String getSecretKeyringFile()
    {
        return secretKeyringFile;
    }


    /**
     * *****************************
     */
    public void setSecretKeyringFile( final String secretKeyringFile )
    {
        this.secretKeyringFile = secretKeyringFile;
    }


    /**
     * *****************************
     */
    public String getSecretKeyringPwd()
    {
        return secretKeyringPwd;
    }


    /**
     * *****************************
     */
    public void setSecretKeyringPwd( final String secretKeyringPwd )
    {
        this.secretKeyringPwd = secretKeyringPwd;
    }


    /**
     * *****************************
     */
    public EncryptionTool getEncryptionTool()
    {
        return encryptionTool;
    }


    /**
     * *****************************
     */
    public void setEncryptionTool( final EncryptionTool encryptionTool )
    {
        this.encryptionTool = encryptionTool;
    }


    /**
     * *****************************
     */
    public String getManHostId()
    {
        return manHostId;
    }


    /**
     * *****************************
     */
    public void setManHostId( final String manHostId )
    {
        this.manHostId = manHostId;
    }


    /**
     * *****************************
     */
    public PeerManager getPeerManager()
    {
        return peerManager;
    }


    /**
     * *****************************
     */
    public void setPeerManager( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    /**
     * *****************************
     */
    public String getManHostKeyFingerprint()
    {
        return manHostKeyFingerprint;
    }


    /**
     * *****************************
     */

    public void setManHostKeyFingerprint( final String manHostKeyFingerprint )
    {
        this.manHostKeyFingerprint = manHostKeyFingerprint;
    }


    public void setPublicKeyringFile( final String publicKeyringFile )
    {
        this.publicKeyringFile = publicKeyringFile;
    }
}
