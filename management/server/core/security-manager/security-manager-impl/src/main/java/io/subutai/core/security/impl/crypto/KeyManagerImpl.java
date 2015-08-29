package io.subutai.core.security.impl.crypto;


import java.io.InputStream;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Strings;
import io.subutai.common.security.crypto.pgp.PGPEncryptionUtil;
import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.core.keyserver.api.KeyServer;
import io.subutai.core.security.api.crypto.KeyManager;
import io.subutai.core.security.api.dao.SecurityManagerDAO;
import io.subutai.core.security.impl.model.SecurityKeyData;


/**
 * Implementation of KeyManager API
 */
public class KeyManagerImpl implements KeyManager
{
    private static final Logger LOG = LoggerFactory.getLogger( KeyManagerImpl.class );

    private SecurityManagerDAO securityManagerDAO = null;
    private KeyServer keyServer = null;
    private SecurityKeyData keyData = null;


    /* *****************************
     *
     */
    public KeyManagerImpl( SecurityManagerDAO securityManagerDAO, KeyServer keyServer, SecurityKeyData securityKeyData )
    {
        this.keyData = securityKeyData;
        this.securityManagerDAO = securityManagerDAO;
        this.keyServer = keyServer;

        // Create Key Identity Record , save Public key in the KeyStore.
        init();
    }


    /* *****************************
     *
     */
    public KeyManagerImpl( SecurityManagerDAO securityManagerDAO, KeyServer keyServer, String publicKeyringFile,
                           String secretKeyringFile, String secretKeyringPwd, String manHostId,
                           String manHostKeyFingerprint )
    {
        this.securityManagerDAO = securityManagerDAO;
        this.keyServer = keyServer;

        keyData = new SecurityKeyData();
        keyData.setPublicKeyringFile( publicKeyringFile );
        keyData.setSecretKeyringFile( secretKeyringFile );
        keyData.setSecretKeyringPwd( secretKeyringPwd );
        keyData.setManHostId( manHostId );
        keyData.setManHostKeyFingerprint( manHostKeyFingerprint );

        // Create Key Identity Record , save Public key in the KeyStore.
        init();
    }


    /* *****************************
     *
     */
    private void init()
    {
        InputStream instrSecRing = PGPEncryptionUtil.getFileInputStream( keyData.getSecretKeyringFile());
        InputStream instrPubRing = PGPEncryptionUtil.getFileInputStream( keyData.getPublicKeyringFile() );

        if ( instrSecRing == null || instrPubRing == null )
        {
            LOG.error( " **** Error! Cannot find localHost KeyRing **** " );
        }
        else
        {
            try
            {
                // Insert Local PeerId and KeyId

                LOG.info( "******** Creating Key record for localhost *******" );

                PGPPublicKeyRing publicKeyRing = PGPKeyUtil.readPublicKeyRing( instrPubRing );

                if ( publicKeyRing == null )
                {
                    LOG.error( " **** Error! Cannot extract PGPPublicKeyRing **** " );
                }
                else
                {
                    savePublicKeyRing( keyData.getManHostId(), publicKeyRing );
                }
            }
            catch ( Exception ex )
            {
                LOG.error( " **** Error, Loading PGPPublicKeyRing **** :" + ex.toString() );
            }
        }
    }


    /* *****************************
     *
     */
    @Override
    public void savePublicKeyRing( String hostId, String keyringAsASCII )
    {
        try
        {
            PGPPublicKeyRing pgpPublicKeyRing = PGPKeyUtil.readPublicKeyRing( keyringAsASCII );

            if ( pgpPublicKeyRing != null )
            {
                savePublicKeyRing( hostId, pgpPublicKeyRing );
            }
        }
        catch ( Exception ex )
        {
            LOG.error( "Error storing Public key:" + ex.toString() );
        }
    }


    /* *****************************
     *
     */
    @Override
    public void savePublicKeyRing( String hostId, PGPPublicKeyRing publicKeyRing )
    {
        try
        {
            PGPPublicKey publicKey = PGPKeyUtil.readPublicKey( publicKeyRing );

            if ( publicKey != null )
            {
                // Store public key in the KeyServer
                keyServer.addSecurityKey( publicKeyRing );

                String fingerprint = PGPKeyUtil.getFingerprint( publicKey.getFingerprint() );
                securityManagerDAO.saveKeyIdentityData( hostId, fingerprint, ( short ) 2 );
            }
        }
        catch ( Exception ex )
        {
            LOG.error( "Error storing Public key:" + ex.toString() );
        }
    }


    /* *****************************
     *
     */
    @Override
    public void removePublicKeyRing( String hostId )
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


    /* *****************************
     *
     */
    @Override
    public PGPPublicKey getPublicKey( String hostId )
    {
        PGPPublicKeyRing publicKeyRing = null;

        publicKeyRing = getPublicKeyRing( hostId );

        if(publicKeyRing!=null)
        {
            return publicKeyRing.getPublicKey();
        }
        else
        {
            LOG.error( "********* Error getting Public key ********" );
            return null;
        }
    }


    /* *****************************
     *
     */
    @Override
    public String getPublicKeyRingAsASCII( String hostId )
    {
        if ( Strings.isNullOrEmpty( hostId ) )
        {
            hostId = keyData.getManHostId();
        }

        try
        {
            String fingerprint = securityManagerDAO.getKeyFingerprint( hostId );

            if ( Strings.isNullOrEmpty( fingerprint ) )
            {
                LOG.error( "Error !Public key not found :" );
                return "";
            }
            else
            {
                byte[] keyData = keyServer.getSecurityKeyByFingerprint( fingerprint ).getKeyData();

                return PGPEncryptionUtil.armorByteArrayToString( keyData );
            }
        }
        catch ( Exception ex )
        {
            LOG.error( "Error getting Public keyRing:" + ex.toString() );
            return "";
        }
    }


    /* *****************************
     *
     */
    @Override
    public PGPPublicKeyRing getPublicKeyRing( String hostId )
    {
        PGPPublicKeyRing publicKeyRing = null;

        if ( Strings.isNullOrEmpty( hostId ) )
        {
            hostId = keyData.getManHostId();
        }

        try
        {
            String fingerprint = securityManagerDAO.getKeyFingerprint( hostId );

            byte[] keyData = keyServer.getSecurityKeyByFingerprint( fingerprint ).getKeyData();

            publicKeyRing = PGPKeyUtil.readPublicKeyRing( keyData);

            return publicKeyRing;
        }
        catch ( Exception ex )
        {
            LOG.error( "Error getting Public key:" + ex.toString() );
            return publicKeyRing;
        }
    }


    /* *****************************
     *
     */
    @Override
    public PGPSecretKey getSecretKey( String hostId )
    {
        PGPSecretKey secretKey = null;

        if ( Strings.isNullOrEmpty( hostId ) )
        {
            hostId = keyData.getManHostId();
        }

        try
        {
            String fingerprint = securityManagerDAO.getKeyFingerprint( hostId );

            secretKey = PGPEncryptionUtil
                    .findSecretKeyByFingerprint( PGPEncryptionUtil.getFileInputStream( keyData.getSecretKeyringFile() ),
                            fingerprint );

            return secretKey;
        }
        catch ( Exception ex )
        {
            LOG.error( "Error getting Secret key:" + ex.toString() );
            return secretKey;
        }
    }


    /* *****************************
     *
     */
    @Override
    public PGPPrivateKey getPrivateKey( String hostId )
    {
        PGPPrivateKey privateKey = null;

        if ( Strings.isNullOrEmpty( hostId ) )
        {
            hostId = keyData.getManHostId();
        }

        try
        {
            PGPSecretKey secretKey = getSecretKey( hostId );

            if ( secretKey != null )
            {
                privateKey = PGPEncryptionUtil.getPrivateKey( secretKey, keyData.getSecretKeyringPwd() );

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


    /* *****************************
     *
     */
    @Override
    public PGPSecretKey getSecretKeyById( String keyId )
    {
        PGPSecretKey secretKey = null;

        try
        {
            secretKey = PGPEncryptionUtil
                    .findSecretKeyById( PGPEncryptionUtil.getFileInputStream( keyData.getSecretKeyringFile() ), keyId );
        }
        catch ( Exception ex )
        {
            LOG.error( "Error getting Secret key:" + ex.toString() );
        }

        return secretKey;
    }


    /* *****************************
     *
     */
    @Override
    public PGPSecretKey getSecretKeyByFingerprint( String fingerprint )
    {
        PGPSecretKey secretKey = null;

        try
        {
            secretKey = PGPEncryptionUtil
                    .findSecretKeyByFingerprint( PGPEncryptionUtil.getFileInputStream( keyData.getSecretKeyringFile() ),
                            fingerprint );
        }
        catch ( Exception ex )
        {
            LOG.error( "Error getting Secret key:" + ex.toString() );
        }

        return secretKey;
    }


    /* *****************************
     *
     */
    public SecurityKeyData getSecurityKeyData()
    {
        return keyData;
    }



}
