package io.subutai.core.security.impl.crypto;


import java.io.InputStream;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

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
    private String publicKeyringFile;
    private String secretKeyringFile;
    private String secretKeyringPwd;
    private String manHostId;
    private String manHostKeyFingerprint;


    /**
     * *****************************
     */
    public KeyManagerImpl( SecurityManagerDAO securityManagerDAO, KeyServer keyServer, String publicKeyringFile,
                           String secretKeyringFile, String secretKeyringPwd, String manHostId,
                           String manHostKeyFingerprint )
    {
        this.securityManagerDAO = securityManagerDAO;
        this.keyServer = keyServer;
        this.publicKeyringFile = publicKeyringFile;
        this.secretKeyringFile = secretKeyringFile;
        this.secretKeyringPwd = secretKeyringPwd;
        this.manHostId = manHostId;
        this.manHostKeyFingerprint = manHostKeyFingerprint;

        // Create Key Identity Record , save Public key in the KeyStore.
        init();
    }


    /**
     * *****************************
     */
    private void init()
    {
        InputStream instr = PGPEncryptionUtil.getFileInputStream( secretKeyringFile );

        if ( instr == null )
        {
            LOG.error( " **** Error! Cannot find localHost KeyRing **** " );
        }
        else
        {
            // Insert Local PeerId and KeyId

            LOG.info( "******** Creating Key record for localhost *******" );

            PGPSecretKey secretKey = getSecretKeyByFingerprint( manHostKeyFingerprint );

            if ( secretKey == null )
            {
                LOG.error( " **** Error! Cannot extract SecretKey from KeyRing **** " );
            }
            else
            {
                savePublicKey( manHostId, secretKey.getPublicKey() );
            }
        }
    }


    @Override
    public String getPeerPublicKeyring()
    {
        try
        {
            return PGPEncryptionUtil.getKeyringArmored( publicKeyringFile );
        }
        catch ( PGPException e )
        {
            LOG.error( "Error getting peer public keyring:", e );
            return null;
        }
    }


    /**
     * *****************************
     */
    @Override
    public void savePublicKey( String hostId, String keyAsASCII )
    {
        try
        {
            // Store public key in the KeyServer
            PGPPublicKey publicKey = keyServer.addPublicKey( keyAsASCII );

            if ( publicKey != null )
            {
                String keyIdStr = PGPKeyUtil.getFingerprint( publicKey.getFingerprint() );
                securityManagerDAO.saveKeyIdentityData( hostId, keyIdStr, ( short ) 2 );
            }
        }
        catch ( Exception ex )
        {
            LOG.error( "Error storing Public key:" + ex.toString() );
        }
    }


    /**
     * *****************************
     */
    @Override
    public void savePublicKey( String hostId, PGPPublicKey publicKey )
    {
        try
        {
            if ( publicKey != null )
            {
                // Store public key in the KeyServer
                keyServer.addSecurityKey( publicKey );

                String fingerprint = PGPKeyUtil.getFingerprint( publicKey.getFingerprint() );
                securityManagerDAO.saveKeyIdentityData( hostId, fingerprint, ( short ) 2 );
            }
        }
        catch ( Exception ex )
        {
            LOG.error( "Error storing Public key:" + ex.toString() );
        }
    }


    /**
     * *****************************
     */
    @Override
    public void removePublicKey( String hostId )
    {
        try
        {
            securityManagerDAO.removeKeyIdentityData( hostId );

            //Remove from KeyStore
            //Currently not supported
        }
        catch ( Exception ex )
        {
            LOG.error( "Error removing Public key:" + ex.toString() );
        }
    }


    /**
     * *****************************
     */
    @Override
    public String getPublicKeyAsASCII( String hostId )
    {
        try
        {
            return PGPKeyUtil.exportAscii( getPublicKey( hostId ) );
        }
        catch ( Exception ex )
        {
            LOG.error( "Error getting Public key:" + ex.toString() );
            return "";
        }
    }


    /**
     * *****************************
     */
    @Override
    public PGPPublicKey getPublicKey( String hostId )
    {
        PGPPublicKey publicKey = null;

        if ( Strings.isNullOrEmpty( hostId ) )
        {
            hostId = manHostId;
        }

        try
        {
            String fingerprint = securityManagerDAO.getKeyFingerprint( hostId );

            publicKey = keyServer.convertKey( keyServer.getSecurityKeyByFingerprint( fingerprint ) );

            return publicKey;
        }
        catch ( Exception ex )
        {
            LOG.error( "Error getting Public key:" + ex.toString() );
            return publicKey;
        }
    }


    /**
     * *****************************
     */
    @Override
    public PGPSecretKey getSecretKey( String hostId )
    {
        PGPSecretKey secretKey = null;

        if ( Strings.isNullOrEmpty( hostId ) )
        {
            hostId = manHostId;
        }

        try
        {
            String fingerprint = securityManagerDAO.getKeyFingerprint( hostId );

            secretKey = PGPEncryptionUtil
                    .findSecretKeyByFingerprint( PGPEncryptionUtil.getFileInputStream( secretKeyringFile ),
                            fingerprint );

            return secretKey;
        }
        catch ( Exception ex )
        {
            LOG.error( "Error getting Secret key:" + ex.toString() );
            return secretKey;
        }
    }


    /**
     * *****************************
     */
    @Override
    public PGPPrivateKey getPrivateKey( String hostId )
    {
        PGPPrivateKey privateKey = null;

        if ( Strings.isNullOrEmpty( hostId ) )
        {
            hostId = manHostId;
        }

        try
        {
            PGPSecretKey secretKey = getSecretKey( hostId );

            if ( secretKey != null )
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


    /**
     * *****************************
     */
    @Override
    public PGPSecretKey getSecretKeyById( String keyId )
    {
        PGPSecretKey secretKey = null;

        try
        {
            secretKey = PGPEncryptionUtil
                    .findSecretKeyById( PGPEncryptionUtil.getFileInputStream( secretKeyringFile ), keyId );
        }
        catch ( Exception ex )
        {
            LOG.error( "Error getting Secret key:" + ex.toString() );
        }

        return secretKey;
    }


    /**
     * *****************************
     */
    @Override
    public PGPSecretKey getSecretKeyByFingerprint( String fingerprint )
    {
        PGPSecretKey secretKey = null;

        try
        {
            secretKey = PGPEncryptionUtil
                    .findSecretKeyByFingerprint( PGPEncryptionUtil.getFileInputStream( secretKeyringFile ),
                            fingerprint );
        }
        catch ( Exception ex )
        {
            LOG.error( "Error getting Secret key:" + ex.toString() );
        }

        return secretKey;
    }


    /**
     * *****************************
     */
    @Override
    public String getSecretKeyringFile()
    {
        return secretKeyringFile;
    }


    /**
     * *****************************
     */
    @Override
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
    public String getManagementHostId()
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
}
