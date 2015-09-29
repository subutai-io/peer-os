package io.subutai.core.security.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.keyserver.api.KeyServer;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.CertificateManager;
import io.subutai.core.security.api.crypto.EncryptionTool;
import io.subutai.core.security.api.crypto.KeyManager;
import io.subutai.core.security.api.crypto.KeyStoreManager;
import io.subutai.core.security.api.dao.SecretKeyStoreDAO;
import io.subutai.core.security.api.dao.SecurityManagerDAO;
import io.subutai.core.security.impl.crypto.CertificateManagerImpl;
import io.subutai.core.security.impl.crypto.EncryptionToolImpl;
import io.subutai.core.security.impl.crypto.KeyManagerImpl;
import io.subutai.core.security.impl.crypto.KeyStoreManagerImpl;
import io.subutai.core.security.impl.dao.SecretKeyStoreDAOImpl;
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
    private SecurityKeyData keyData = null;
    private SecretKeyStoreDAO secretKeyStoreDAO = null;
    private KeyStoreManager keyStoreManager = null;
    private CertificateManager certificateManager = null;


    /* *****************************
     *
     */
    public SecurityManagerImpl()
    {

    }


    /* *****************************
     *
     */
    public SecurityManagerImpl( String ownerPublicKeyringFile, String secretKeyringFile, String publicKeyringFile,
                                String secretKeyringPwd )
    {
        keyData = new SecurityKeyData();

        keyData.setOwnerPublicKeyringFile( ownerPublicKeyringFile );
        keyData.setSecretKeyringFile( secretKeyringFile );
        keyData.setPublicKeyringFile( publicKeyringFile );
        keyData.setSecretKeyringPwd( secretKeyringPwd );
    }


    /* *****************************
     *
     */
    public void init()
    {
        securityManagerDAO = new SecurityManagerDAOImpl( daoManager );
        secretKeyStoreDAO = new SecretKeyStoreDAOImpl( daoManager );
        keyManager = new KeyManagerImpl( securityManagerDAO, secretKeyStoreDAO, keyServer, keyData );
        encryptionTool = new EncryptionToolImpl( ( KeyManagerImpl ) keyManager );
        keyStoreManager = new KeyStoreManagerImpl();
        certificateManager = new CertificateManagerImpl();
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
    @Override
    public KeyStoreManager getKeyStoreManager()
    {
        return keyStoreManager;
    }


    /* *****************************
     *
     */
    public void setKeyStoreManager( final KeyStoreManager keyStoreManager )
    {
        this.keyStoreManager = keyStoreManager;
    }


    /* *****************************
     *
     */
    @Override
    public CertificateManager getCertificateManager()
    {
        return certificateManager;
    }


    /* *****************************
     *
     */
    public void setCertificateManager( final CertificateManager certificateManager )
    {
        this.certificateManager = certificateManager;
    }
}
