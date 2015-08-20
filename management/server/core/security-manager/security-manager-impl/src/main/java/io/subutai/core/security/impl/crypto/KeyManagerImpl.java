package io.subutai.core.security.impl.crypto;


import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.security.crypto.pgp.PGPEncryptionUtil;
import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.core.keyserver.api.KeyServer;
import io.subutai.core.security.api.crypto.KeyManager;
import io.subutai.core.security.api.dao.SecurityManagerDAO;


/**
 * Implementation of KeyManager API
 */
public class KeyManagerImpl implements KeyManager
{
    private static final Logger LOG = LoggerFactory.getLogger( KeyManagerImpl.class );

    private SecurityManagerDAO securityManagerDAO = null;
    private KeyServer keyServer = null;
    private String secretKeyring;
    private String secretKeyringPwd;


    /********************************
     *
     */
    public KeyManagerImpl( SecurityManagerDAO securityManagerDAO, KeyServer keyServer, String secretKeyring,
                           String secretKeyringPwd )
    {
        this.securityManagerDAO = securityManagerDAO;
        this.keyServer = keyServer;
        this.secretKeyring = secretKeyring;
        this.secretKeyringPwd = secretKeyringPwd;
    }


    /********************************
     *
     */
    @Override
    public void savePublicKey( String hostId, String keyAsASCII)
    {
        try
        {
            // Store public key in the KeyServer
            PGPPublicKey publicKey = keyServer.addPublicKey( keyAsASCII );

            if(publicKey!=null)
            {
                String keyIdStr = PGPKeyUtil.encodeNumericKeyId( publicKey.getKeyID() );
                securityManagerDAO.saveKey( hostId, keyIdStr,(short)2);
            }

        }
        catch(Exception ex)
        {
            LOG.error( "Error storing Public key:" + ex.toString() );
        }
    }


    /********************************
     *
     */
    @Override
    public void savePublicKey( String hostId, PGPPublicKey publicKey)
    {
        try
        {
            if(publicKey!=null)
            {
                // Store public key in the KeyServer
                keyServer.addSecurityKey ( publicKey );

                String keyIdStr = PGPKeyUtil.encodeNumericKeyId( publicKey.getKeyID() );
                securityManagerDAO.saveKey( hostId, keyIdStr,(short)2);
            }
        }
        catch(Exception ex)
        {
            LOG.error( "Error storing Public key:" + ex.toString() );
        }
    }


    /********************************
     *
     */
    @Override
    public void removePublicKey( String hostId)
    {
        try
        {
            securityManagerDAO.removeKey( hostId );

            //Remove from KeyStore
            //Currently not supported
        }
        catch(Exception ex)
        {
            LOG.error( "Error removing Public key:" + ex.toString() );
        }
    }


    /********************************
     *
     */
    @Override
    public String getPublicKeyAsASCII( String hostId )
    {
        try
        {
            String keyId = securityManagerDAO.getKeyId( hostId );
            return keyServer.getSecurityKeyAsASCII( keyId );
        }
        catch ( Exception ex )
        {
            LOG.error( "Error getting Public key:" + ex.toString() );
            return "";
        }
    }


    /********************************
     *
     */
    @Override
    public PGPPublicKey getPublicKey( String hostId )
    {
        PGPPublicKey publicKey = null;

        try
        {
            String keyId = securityManagerDAO.getKeyId( hostId );

            publicKey = keyServer.convertKey( keyServer.getSecurityKey( keyId ) );

            return publicKey;
        }
        catch ( Exception ex )
        {
            LOG.error( "Error getting Public key:" + ex.toString() );
            return publicKey;
        }
    }


    /********************************
     *
     */
    @Override
    public PGPSecretKey getSecretKey( String hostId )
    {
        PGPSecretKey secretKey = null;

        try
        {
            String keyId = securityManagerDAO.getKeyId( hostId );


            secretKey = PGPEncryptionUtil.findSecretKeyById( PGPEncryptionUtil.loadKeyring( secretKeyring ), keyId );

            return secretKey;
        }
        catch ( Exception ex )
        {
            LOG.error( "Error getting Secret key:" + ex.toString() );
            return secretKey;
        }
    }

    /********************************
     *
     */
    @Override
    public PGPPrivateKey getPrivateKey( String hostId )
    {
        PGPPrivateKey privateKey = null;

        try
        {
            PGPSecretKey secretKey = getSecretKey( hostId );

            if(secretKey!=null)
            {
                privateKey = PGPEncryptionUtil.getPrivateKey( secretKey, secretKeyringPwd );

                return privateKey;
            }
            else
            {
                return null;
            }

        }
        catch ( Exception ex )
        {
            LOG.error( "Error getting Private key:" + ex.toString() );
            return privateKey;
        }
    }

}
