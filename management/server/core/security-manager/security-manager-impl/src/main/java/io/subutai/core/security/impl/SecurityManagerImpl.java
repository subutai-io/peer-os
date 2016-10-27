package io.subutai.core.security.impl;


import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;

import io.subutai.common.command.EncryptedRequestWrapper;
import io.subutai.common.command.EncryptedResponseWrapper;
import io.subutai.common.dao.DaoManager;
import io.subutai.common.security.crypto.pgp.ContentAndSignatures;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.keyserver.api.KeyServer;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.EncryptionTool;
import io.subutai.core.security.api.crypto.KeyManager;
import io.subutai.core.security.api.crypto.KeyStoreManager;
import io.subutai.core.security.api.dao.SecurityDataService;
import io.subutai.core.security.api.jetty.HttpContextManager;
import io.subutai.core.security.impl.crypto.EncryptionToolImpl;
import io.subutai.core.security.impl.crypto.KeyManagerImpl;
import io.subutai.core.security.impl.crypto.KeyStoreManagerImpl;
import io.subutai.core.security.impl.dao.SecurityDataServiceImpl;
import io.subutai.core.security.impl.jetty.HttpContextManagerImpl;
import io.subutai.core.security.impl.model.SecurityKeyData;


/**
 * Implementation of SecurityManager
 */
public class SecurityManagerImpl implements SecurityManager
{

    private KeyManager keyManager = null;
    private DaoManager daoManager = null;
    private EncryptionTool encryptionTool = null;
    private KeyServer keyServer = null;
    private SecurityKeyData keyData = null;
    private KeyStoreManager keyStoreManager = null;
    private HttpContextManager httpContextManager;


    /* *****************************
     *
     */
    public SecurityManagerImpl()
    {
        keyData = new SecurityKeyData();

        httpContextManager = new HttpContextManagerImpl();
    }


    public void setKeyServer( final KeyServer keyServer )
    {
        this.keyServer = keyServer;
    }


    public void setDaoManager( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    public void init()
    {
        final SecurityDataService securityDataService = new SecurityDataServiceImpl( daoManager );
        keyManager = new KeyManagerImpl( securityDataService, keyServer, keyData );
        encryptionTool = new EncryptionToolImpl( ( KeyManagerImpl ) keyManager );
        keyStoreManager = new KeyStoreManagerImpl();
    }


    @Override
    public KeyManager getKeyManager()
    {
        return keyManager;
    }


    @Override
    public EncryptionTool getEncryptionTool()
    {
        return encryptionTool;
    }


    @Override
    public KeyStoreManager getKeyStoreManager()
    {
        return keyStoreManager;
    }


    @Override
    public HttpContextManager getHttpContextManager()
    {
        return httpContextManager;
    }


    @Override
    public String signNEncryptRequestToHost( final String message, final String hostId ) throws PGPException
    {

        //obtain target host pub key for encrypting
        PGPPublicKey hostKeyForEncrypting = keyManager.getPublicKey( hostId );

        if ( hostKeyForEncrypting == null )
        {
            throw new PGPException( String.format( "Public key not found by host id %s", hostId ) );
        }

        String encryptedRequestString =
                new String( encryptionTool.signAndEncrypt( message.getBytes(), hostKeyForEncrypting, true ) );

        EncryptedRequestWrapper encryptedRequestWrapper = new EncryptedRequestWrapper( encryptedRequestString, hostId );

        return JsonUtil.toJson( encryptedRequestWrapper );
    }


    @Override
    public String decryptNVerifyResponseFromHost( final String message ) throws PGPException
    {

        EncryptedResponseWrapper responseWrapper = JsonUtil.fromJson( message, EncryptedResponseWrapper.class );

        ContentAndSignatures contentAndSignatures =
                encryptionTool.decryptAndReturnSignatures( responseWrapper.getResponse().getBytes() );

        PGPPublicKey hostKeyForVerifying = keyManager.getPublicKey( responseWrapper.getHostId() );

        if ( hostKeyForVerifying == null )
        {
            throw new PGPException(
                    String.format( "Public key not found by host id %s", responseWrapper.getHostId() ) );
        }

        if ( encryptionTool.verifySignature( contentAndSignatures, hostKeyForVerifying ) )
        {
            return new String( contentAndSignatures.getDecryptedContent() );
        }
        else
        {
            throw new IllegalArgumentException( String.format( "Verification failed%nDecrypted Message: %s",
                    new String( contentAndSignatures.getDecryptedContent() ) ) );
        }
    }
}
