package io.subutai.core.security.impl.crypto;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessControlException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import io.subutai.common.peer.PeerInfo;
import io.subutai.common.security.crypto.pgp.KeyPair;
import io.subutai.common.security.crypto.pgp.PGPEncryptionUtil;
import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.common.security.objects.KeyTrustLevel;
import io.subutai.common.security.objects.SecurityKeyType;
import io.subutai.common.settings.ChannelSettings;
import io.subutai.common.util.RestUtil;
import io.subutai.core.keyserver.api.KeyServer;
import io.subutai.core.security.api.crypto.EncryptionTool;
import io.subutai.core.security.api.crypto.KeyManager;
import io.subutai.core.security.api.dao.SecurityDataService;
import io.subutai.core.security.api.model.SecurityKeyIdentity;
import io.subutai.core.security.api.model.SecurityKeyTrust;
import io.subutai.core.security.impl.model.SecurityKeyData;


/**
 * Implementation of KeyManager API
 */
public class KeyManagerImpl implements KeyManager
{
    private static final Logger LOG = LoggerFactory.getLogger( KeyManagerImpl.class );

    private SecurityDataService securityDataService = null;
    private KeyServer keyServer = null;
    private SecurityKeyData keyData = null;
    private EncryptionTool encryptionTool = null;


    /* *****************************
     *
     */
    public KeyManagerImpl( SecurityDataService securityDataService, KeyServer keyServer,
                           SecurityKeyData securityKeyData )
    {
        this.keyData = securityKeyData;
        this.securityDataService = securityDataService;
        this.keyServer = keyServer;

        init();
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
    private void init()
    {
        try
        {
            InputStream ownerPubStream = PGPEncryptionUtil.getFileInputStream( keyData.getOwnerPublicKeyringFile() );
            InputStream peerPubStream = PGPEncryptionUtil.getFileInputStream( keyData.getPublicKeyringFile() );
            InputStream peerSecStream = PGPEncryptionUtil.getFileInputStream( keyData.getSecretKeyringFile() );

            if ( ownerPubStream == null || peerPubStream == null || peerSecStream == null )
            {
                LOG.info( " **** Error loading PGPPublicKeyRing/PGPSecretKeyRing files. Files not found.**** :" );
                //todo System.exit(1) with error message
            }
            else
            {
                PGPPublicKeyRing peerPubRing = PGPKeyUtil.readPublicKeyRing( peerPubStream );
                PGPPublicKeyRing ownerPeerPubRing = PGPKeyUtil.readPublicKeyRing( ownerPubStream );

                String peerId = PGPKeyUtil.getFingerprint( peerPubRing.getPublicKey().getFingerprint() );
                String ownerPeerFPrint = PGPKeyUtil.getFingerprint( ownerPeerPubRing.getPublicKey().getFingerprint() );

                keyData.setManHostId( peerId );
                saveSecretKeyRing( keyData.getManHostId(), SecurityKeyType.PeerKey.getId(),
                        PGPKeyUtil.readSecretKeyRing( peerSecStream ) );
                savePublicKeyRing( keyData.getManHostId(), SecurityKeyType.PeerKey.getId(), peerPubRing );
                savePublicKeyRing( getOwnerKeyIdx(), SecurityKeyType.PeerOwnerKey.getId(), ownerPeerPubRing );

                //************************************************************
                setKeyTrust( ownerPeerFPrint, peerId, KeyTrustLevel.Full.getId() );
                //************************************************************

                //************************************************************
                //ownerPubStream.close();
                //peerPubStream.close();
                //peerSecStream.close();
                //************************************************************

            }
        }
        catch ( Exception ex )
        {
            LOG.error( " **** Error loading PGPPublicKeyRing/PGPSecretKeyRing **** :" + ex.toString() );
        }
    }


    //todo Nurkaly please revise this so that we take owner idx from the constant
    private String getOwnerKeyIdx()
    {
        return "owner-" + keyData.getManHostId();
    }


    /* ***************************************************************
     *
     */
    @Override
    public String getPeerId()
    {
        return PGPKeyUtil.getFingerprint( getPublicKey( null ).getFingerprint() );
    }


    /* ***************************************************************
     *
     */
    @Override
    public String getOwnerId()
    {
        return PGPKeyUtil.getFingerprint( getPublicKey( getOwnerKeyIdx() ).getFingerprint() );
    }


    /* ***************************************************************
     *
     */
    @Override
    public PGPPublicKeyRing signKey( PGPSecretKeyRing sourceSecRing, PGPPublicKeyRing targetPubRing, int trustLevel )
    {
        try
        {
            String secretFPrint = PGPKeyUtil.getFingerprint( sourceSecRing.getPublicKey().getFingerprint() );
            String publicFPrint = PGPKeyUtil.getFingerprint( targetPubRing.getPublicKey().getFingerprint() );

            //****************************************
            String keyIdentityId = getKeyIdentityDataByFingerprint( secretFPrint ).getIdentityId();
            //****************************************

            targetPubRing = encryptionTool.signPublicKey( targetPubRing, keyIdentityId, sourceSecRing.getSecretKey(), "" );
            keyServer.updatePublicKey( targetPubRing );
            setKeyTrust( secretFPrint, publicFPrint, trustLevel );
        }
        catch ( Exception ex )
        {
        }

        return targetPubRing;
    }


    /* ***************************************************************
     *
     */
    @Override
    public PGPPublicKeyRing signKey( String sourceFingerprint, String targetFingerprint, int trustLevel )
    {
        PGPPublicKeyRing targetPubRing = getPublicKeyRing( sourceFingerprint );
        PGPSecretKeyRing sourceSecRing = getSecretKeyRing( targetFingerprint );

        return signKey( sourceSecRing, targetPubRing, trustLevel );
    }


    /* ***************************************************************
     *
     */
    @Override
    public String signPublicKey( String sourceIdentityId, String keyText, int trustLevel )
    {
        String keyStr = "";

        try
        {
            PGPPublicKeyRing targetPubRing = PGPKeyUtil.readPublicKeyRing( keyText );
            PGPSecretKeyRing sourceSecRing = getSecretKeyRing( sourceIdentityId );

            targetPubRing = signKey( sourceSecRing, targetPubRing, trustLevel );

            keyStr = encryptionTool.armorByteArrayToString( targetPubRing.getEncoded() );
        }
        catch ( Exception ex )
        {
            LOG.error( "**** Error !!! Error signing key, IdentityId: "+sourceIdentityId,ex );
        }
        return keyStr;
    }


    /* ***************************************************************
     *
     */
    @Override
    public void setKeyTrust( String sourceFingerprint, String targetFingerprint, int trustLevel )
    {
        try
        {
            SecurityKeyTrust securityKeyTrust =
                    securityDataService.getKeyTrustData( sourceFingerprint, targetFingerprint );

            if ( trustLevel == KeyTrustLevel.Never.getId() )
            {
                //******************************************
                removeSignature( sourceFingerprint, targetFingerprint );
                //******************************************
            }
            else
            {
                if ( securityKeyTrust == null )
                {
                    securityDataService.saveKeyTrustData( sourceFingerprint, targetFingerprint, trustLevel );

                    //******************************************
                    signKey( sourceFingerprint, targetFingerprint ,trustLevel );
                    //******************************************
                }
            }

            securityKeyTrust.setLevel( trustLevel );
            securityDataService.updateKeyTrustData( securityKeyTrust );
        }
        catch ( Exception ex )
        {
            LOG.error( " **** Error!!! Error creating key trust:" + ex.toString(), ex );
        }
    }


    /* ***************************************************************
     *
     */
    @Override
    public PGPPublicKeyRing removeSignature( String sourceFingerprint, String targetFingerprint )
    {
        PGPPublicKeyRing targetPubRing = getPublicKeyRingByFingerprint( targetFingerprint );
        String sourceIdentityId = getKeyIdentityDataByFingerprint( sourceFingerprint ).getIdentityId();

        return encryptionTool.removeSignature(sourceIdentityId, targetPubRing);
    }


    /* ***************************************************************
     *
     */
    @Override
    public PGPPublicKeyRing removeSignature( String sourceIdentityId, PGPPublicKeyRing targetPubRing )
    {
        PGPPublicKeyRing newRing = null;
        try
        {
            encryptionTool.removeSignature(sourceIdentityId,targetPubRing);
            keyServer.updatePublicKey( newRing );
        }
        catch ( IOException | PGPException e )
        {
            LOG.error( "**** Error !!! Error removing key signature IdentityId: "+sourceIdentityId,e );
        }

        return newRing;
    }


    /* ***************************************************************
     *
     */
    @Override
    public SecurityKeyTrust getKeyTrustData( String sourceFingerprint, String targetFingerprint )
    {
        return securityDataService.getKeyTrustData( sourceFingerprint, targetFingerprint );
    }


    /* ***************************************************************
     *
     */
    @Override
    public List<SecurityKeyTrust> getKeyTrustData( final String sourceFingerprint )
    {
        return securityDataService.getKeyTrustData( sourceFingerprint );
    }


    /* ***************************************************************
     *
     */
    @Override
    public void removeKeyTrust( String sourceFingerprint )
    {
        try
        {
            securityDataService.removeKeyTrustData( sourceFingerprint );
        }
        catch ( Exception ex )
        {
            LOG.error( " **** Error!!! Error removing key trust:" + ex.toString(), ex );
        }
    }


    /* ***************************************************************
     *
     */
    @Override
    public void removeKeyTrust( String sourceFingerprint, String targetFingerprint )
    {
        try
        {
            securityDataService.removeKeyTrustData( sourceFingerprint, targetFingerprint );
        }
        catch ( Exception ex )
        {
            LOG.error( " **** Error!!! Error removing key trust:" + ex.toString(), ex );
        }
    }


    /* ***************************************************************
     *
     */
    @Override
    public void saveSecretKeyRing( String identityId, int type, PGPSecretKeyRing secretKeyRing )
    {
        try
        {
            PGPPublicKey publicKey = secretKeyRing.getPublicKey();

            if ( publicKey != null )
            {
                // Store secretKey
                String fingerprint = PGPKeyUtil.getFingerprint( publicKey.getFingerprint() );
                String pwd = keyData.getSecretKeyringPwd();

                //*******************
                securityDataService.saveSecretKeyData( fingerprint, secretKeyRing.getEncoded(), pwd, type );
                securityDataService.saveKeyIdentityData( identityId, fingerprint, "", type );
                //*******************
            }
        }
        catch ( Exception ex )
        {
            LOG.error( "Error storing Public key:" + ex.toString() );
        }
    }


    /* ***************************************************************
     *
     */
    @Override
    public void savePublicKeyRing( String identityId, int type, String keyringAsASCII )
    {
        try
        {
            PGPPublicKeyRing pgpPublicKeyRing = PGPKeyUtil.readPublicKeyRing( keyringAsASCII );

            if ( pgpPublicKeyRing != null )
            {
                savePublicKeyRing( identityId, type, pgpPublicKeyRing );
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
    public void savePublicKeyRing( String identityId, int type, PGPPublicKeyRing publicKeyRing )
    {
        try
        {
            PGPPublicKey publicKey = PGPKeyUtil.readPublicKey( publicKeyRing );

            if ( Strings.isNullOrEmpty( identityId ) )
            {
                identityId = keyData.getManHostId();
            }

            if ( publicKey != null )
            {
                // Store public key in the KeyServer
                keyServer.addPublicKey( publicKeyRing );

                //*************************
                String fingerprint = PGPKeyUtil.getFingerprint( publicKey.getFingerprint() );
                securityDataService.saveKeyIdentityData( identityId, "", fingerprint, type );
                //*************************
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
    public void removePublicKeyRing( String identityId )
    {
        try
        {
            if ( !Objects.equals( identityId, keyData.getManHostId() ) )
            {
                securityDataService.removeKeyIdentityData( identityId );
            }

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
    public void removeSecretKeyRing( String identityId )
    {
        try
        {
            if ( !Objects.equals( identityId, keyData.getManHostId() ) )
            {
                SecurityKeyIdentity keyIden = securityDataService.getKeyIdentityData( identityId );

                if ( keyIden != null )
                {
                    securityDataService.removeSecretKeyData( keyIden.getSecretKeyFingerprint() );
                }
            }
            else
            {
                throw new AccessControlException( " ***** Error!Management Keys cannot be removed ****" );
            }
        }
        catch ( Exception ex )
        {
            LOG.error( "Error removing Secret key:" + ex.toString() );
        }
    }


    /* *****************************
     *
     */
    @Override
    public SecurityKeyIdentity getKeyIdentityData( String identityId )
    {
        SecurityKeyIdentity keyIden = null;
        try
        {
            if ( Strings.isNullOrEmpty( identityId ) )
            {
                identityId = keyData.getManHostId();
            }
            keyIden = securityDataService.getKeyIdentityData( identityId );
        }
        catch ( Exception ex )
        {
            LOG.error( "Error removing Secret key:" + ex.toString() );
        }
        return keyIden;
    }



    /* *****************************
     *
     */
    @Override
    public SecurityKeyIdentity getKeyIdentityDataByFingerprint( String fingerprint )
    {
        SecurityKeyIdentity keyIden = null;
        try
        {
           keyIden = securityDataService.getKeyIdentityDataByFingerprint( fingerprint );
        }
        catch ( Exception ex )
        {
            LOG.error( "Error removing Secret key:" + ex.toString() );
        }
        return keyIden;
    }


    /* *****************************
     *
     */
    @Override
    public void removeKeyIdentityData( String identityId )
    {
        try
        {
            if ( !Objects.equals( identityId, keyData.getManHostId() ) )
            {
                securityDataService.removeKeyIdentityData( identityId );
                removeKeyRings( identityId );
            }
        }
        catch ( Exception ex )
        {
            LOG.error( "Error removing Secret key:" + ex.toString() );
        }
    }


    /* *****************************
     *
     */
    @Override
    public PGPPublicKey getPublicKey( String identityId )
    {
        PGPPublicKeyRing publicKeyRing;

        try
        {
            publicKeyRing = getPublicKeyRing( identityId );

            if ( publicKeyRing != null )
            {
                return PGPKeyUtil.readPublicKey( publicKeyRing );
            }
            else
            {
                LOG.error( "********* Error getting Public key ********" );
                return null;
            }
        }
        catch ( PGPException e )
        {
            return null;
        }
    }

    /* *****************************
     *
     */
    @Override
    public PGPPublicKeyRing getPublicKeyRingByFingerprint( String fingerprint )
    {
        PGPPublicKeyRing pubKeyRing = null;
        try
        {
            byte[] keyData = keyServer.getPublicKeyByFingerprint( fingerprint ).getKeyData();

            if(keyData!=null)
            {
                return PGPKeyUtil.readPublicKeyRing( keyData );
            }
        }
        catch ( PGPException e )
        {
        }

        return pubKeyRing;
    }

    /* *****************************
     *
     */
    @Override
    public String getPublicKeyRingAsASCII( String identityId )
    {
        if ( Strings.isNullOrEmpty( identityId ) )
        {
            identityId = keyData.getManHostId();
        }

        try
        {
            SecurityKeyIdentity keyIden = securityDataService.getKeyIdentityData( identityId );

            if ( keyIden == null )
            {
                LOG.error( "Error !Public key not found :" );
                return "";
            }
            {
                byte[] keyData = keyServer.getPublicKeyByFingerprint( keyIden.getPublicKeyFingerprint() ).getKeyData();

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
    public PGPPublicKeyRing getPublicKeyRing( String identityId )
    {
        PGPPublicKeyRing publicKeyRing;

        if ( Strings.isNullOrEmpty( identityId ) )
        {
            identityId = keyData.getManHostId();
        }

        try
        {
            SecurityKeyIdentity keyIden = securityDataService.getKeyIdentityData( identityId );

            if ( keyIden == null )
            {
                throw new NullPointerException( "***** Error! Key Identity not found." );
            }
            else
            {

                byte[] keyData = keyServer.getPublicKeyByFingerprint( keyIden.getPublicKeyFingerprint() ).getKeyData();

                publicKeyRing = PGPKeyUtil.readPublicKeyRing( keyData );

                return publicKeyRing;
            }
        }
        catch ( Exception ex )
        {
            LOG.error( "Error getting Public key:" + ex.toString() );
            return null;
        }
    }


    /* *****************************
     *
     */
    @Override
    public String getFingerprint( String identityId )
    {

        if ( Strings.isNullOrEmpty( identityId ) )
        {
            identityId = keyData.getManHostId();
        }

        try
        {
            SecurityKeyIdentity keyIden = securityDataService.getKeyIdentityData( identityId );
            return keyIden.getPublicKeyFingerprint();
        }
        catch ( Exception ex )
        {
            LOG.error( "Error getting public key fingerprint", ex );
            return null;
        }
    }


    /* *****************************
     *
     */
    @Override
    public PGPSecretKeyRing getSecretKeyRing( String identityId )
    {
        if ( Strings.isNullOrEmpty( identityId ) )
        {
            identityId = keyData.getManHostId();
        }

        try
        {
            PGPSecretKeyRing secretKeyRing;
            SecurityKeyIdentity keyIden = securityDataService.getKeyIdentityData( identityId );

            if ( keyIden == null )
            {
                LOG.error( " **** Error! Identity Info not found for host:" + identityId );
                return null;
            }
            else
            {
                String fingerprint = keyIden.getSecretKeyFingerprint();
                secretKeyRing =
                        PGPKeyUtil.readSecretKeyRing( securityDataService.getSecretKeyData( fingerprint ).getData() );

                if ( secretKeyRing != null )
                {
                    return secretKeyRing;
                }
                else
                {
                    LOG.error( " **** Error! Object not found with fprint:" + fingerprint );
                    return null;
                }
            }
        }
        catch ( Exception ex )
        {
            LOG.error( " **** Error getting Secret key:" + ex.toString(), ex );
            return null;
        }
    }


    /* *****************************
     *
     */
    @Override
    public InputStream getSecretKeyRingInputStream( String identityId )
    {
        if ( Strings.isNullOrEmpty( identityId ) )
        {
            identityId = keyData.getManHostId();
        }

        try
        {
            SecurityKeyIdentity keyIden = securityDataService.getKeyIdentityData( identityId );

            if ( keyIden != null )
            {
                return PGPKeyUtil.readSecretKeyRingInputStream(
                        securityDataService.getSecretKeyData( keyIden.getSecretKeyFingerprint() ).getData() );
            }
            else
            {
                return null;
            }
        }
        catch ( Exception ex )
        {
            LOG.error( "Error getting Secret key:" + ex.toString() );
            return null;
        }
    }


    /* *************************************************************
     *
     */
    @Override
    public PGPSecretKey getSecretKey( String identityId )
    {
        if ( Strings.isNullOrEmpty( identityId ) )
        {
            identityId = keyData.getManHostId();
        }

        try
        {
            PGPSecretKeyRing secretKeyRing = getSecretKeyRing( identityId );

            if ( secretKeyRing != null )
            {
                return PGPKeyUtil.readSecretKey( secretKeyRing );
            }
            else
            {
                return null;
            }
        }
        catch ( Exception ex )
        {
            LOG.error( "Error getting Secret key:" + ex.toString() );
            return null;
        }
    }


    /* ******************************************************
     *
     */
    @Override
    public PGPPrivateKey getPrivateKey( String identityId )
    {
        PGPPrivateKey privateKey;

        if ( Strings.isNullOrEmpty( identityId ) )
        {
            identityId = keyData.getManHostId();
        }

        try
        {
            PGPSecretKey secretKey = getSecretKey( identityId );

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
            return null;
        }
    }


    /* *****************************
     *
     */
    @Override
    public void updatePublicKeyRing( final PGPPublicKeyRing publicKeyRing )
    {
        try
        {
            keyServer.updatePublicKey( publicKeyRing );
        }
        catch ( IOException | PGPException e )
        {
            LOG.warn( e.getMessage() );
        }
    }


    /* ******************************************************************
     *
     */
    @Override
    public PGPSecretKey getSecretKeyByFingerprint( String fingerprint )
    {
        PGPSecretKey secretKey = null;

        try
        {
            ByteArrayInputStream barIn =
                    new ByteArrayInputStream( securityDataService.getSecretKeyData( fingerprint ).getData() );

            secretKey = PGPEncryptionUtil.findSecretKeyByFingerprint( barIn, fingerprint );
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


    /* *****************************************
     *
     */
    @Override
    public KeyPair generateKeyPair( String identityId, boolean armored )
    {
        KeyPair keyPair;

        try
        {
            keyPair = PGPEncryptionUtil.generateKeyPair( identityId, keyData.getSecretKeyringPwd(), armored );
            return keyPair;
        }
        catch ( Exception ex )
        {
            return null;
        }
    }


    /* *****************************************
     *
     */
    @Override
    public void saveKeyPair( String identityId, int type, KeyPair keyPair )
    {
        try
        {
            saveSecretKeyRing( identityId, type, PGPKeyUtil.readSecretKeyRing( keyPair.getSecKeyring() ) );
            savePublicKeyRing( identityId, type, PGPKeyUtil.readPublicKeyRing( keyPair.getPubKeyring() ) );
        }
        catch ( Exception ignored )
        {

        }
    }


    /* *****************************************
     *
     */
    @Override
    public void removeKeyRings( String identityId )
    {
        try
        {
            if ( !Objects.equals( identityId, keyData.getManHostId() ) )
            {
                removeSecretKeyRing( identityId );
                removePublicKeyRing( identityId );
            }
            else
            {
                LOG.error( identityId + " Cannot be removed (possibly ManagementHost):" );
            }
        }
        catch ( Exception ignored )
        {

        }
    }


    /* *************************************************************
     * Get Public key and save it in the local KeyServer
     */
    @Override
    public PGPPublicKey getRemoteHostPublicKey( String remoteHostId, String ip )
    {
        try
        {
            PGPPublicKeyRing pubRing;

            if ( Strings.isNullOrEmpty( remoteHostId ) )
            {
                remoteHostId = getRemoteHostId( ip );
            }

            pubRing = getPublicKeyRing( remoteHostId );

            if ( pubRing == null ) // Get from HTTP
            {
                String baseUrl = String.format( "https://%s:%s/rest/v1", ip, ChannelSettings.SECURE_PORT_X1 );
                WebClient client = RestUtil.createTrustedWebClient( baseUrl, keyData.getJsonProvider() );
                client.type( MediaType.MULTIPART_FORM_DATA ).accept( MediaType.APPLICATION_JSON );

                Response response =
                        client.path( "security/keyman/getpublickeyring" ).query( "hostid", remoteHostId ).get();

                if ( response.getStatus() == Response.Status.OK.getStatusCode() )
                {
                    String publicKeyring = response.readEntity( String.class );
                    savePublicKeyRing( remoteHostId, ( short ) 3, publicKeyring );
                }
                return getPublicKey( remoteHostId );
            }
            else
            {
                return PGPKeyUtil.readPublicKey( pubRing );
            }
        }
        catch ( Exception ex )
        {
            return null;
        }
    }


    /* *************************************************************
     * Get HOST ID key
     */
    private String getRemoteHostId( String ip )
    {
        // Get Remote peer Public Key and save in the local keystore

        String peerId = "";

        String baseUrl = String.format( "https://%s:%s/rest/v1/peer", ip, ChannelSettings.SECURE_PORT_X1 );
        WebClient clientPeerId = RestUtil.createTrustedWebClient( baseUrl, keyData.getJsonProvider() );
        clientPeerId.type( MediaType.APPLICATION_JSON ).accept( MediaType.APPLICATION_JSON );
        PeerInfo peerInfo = clientPeerId.path( "/info" ).get( PeerInfo.class );

        if ( clientPeerId.getResponse().getStatus() == Response.Status.OK.getStatusCode() )
        {
            peerId = peerInfo.getId();
        }

        return peerId;
    }


    /* *************************************************************
     *
     */
    @Override
    public SecurityKeyIdentity getKeyTrustTree( final String hostId )
    {
        String fingerprint = getFingerprint( hostId );
        SecurityKeyIdentity securityKeyIdentity = getKeyIdentityData( hostId );
        Set<String> trustChain = Sets.newHashSet();

        List<SecurityKeyTrust> keyTrustList = securityDataService.getKeyTrustData( fingerprint );

        LOG.debug( fingerprint );
        LOG.debug( "***key trust size: " + keyTrustList.size() );

        for ( final SecurityKeyTrust keyTrust : keyTrustList )
        {
            getKeyTrustList( securityKeyIdentity, keyTrust, trustChain );
        }

        return securityKeyIdentity;
    }


    /* *****************************
     *
     */
    private void getKeyTrustList( final SecurityKeyIdentity securityKeyIdentity, SecurityKeyTrust securityKeyTrust,
                                  Set<String> trustIdSet )
    {
        List<SecurityKeyTrust> trustedKeys =
                securityDataService.getKeyTrustData( securityKeyTrust.getTargetFingerprint() );
        SecurityKeyIdentity trustedIdentity =
                securityDataService.getKeyIdentityDataByFingerprint( securityKeyTrust.getTargetFingerprint() );
        trustIdSet.add( securityKeyIdentity.getIdentityId() );

        if ( trustedIdentity != null )
        {
            securityKeyIdentity.getTrustedKeys().add( trustedIdentity );
        }

        for ( final SecurityKeyTrust trustedKey : trustedKeys )
        {
            if ( trustedIdentity != null && !trustIdSet.contains( trustedIdentity.getIdentityId() ) )
            getKeyTrustList( trustedIdentity, trustedKey, trustIdSet );
        }
    }


    /* *****************************
     *
     */
    @Override
    public int getTrustLevel( final String sourceIdentityId, final String targetIdentityId )
    {
        String sFingerprint = getFingerprint( sourceIdentityId );
        String tFingerprint = getFingerprint( targetIdentityId );

        if ( sFingerprint.equals( tFingerprint ) )
        {
            // ultimate trust exists if asked trust level for myself
            return KeyTrustLevel.Ultimate.getId();
        }

        Set<String> sTrustChain = Sets.newHashSet();
        Set<String> tTrustChain = Sets.newHashSet();

        constructTrustChain( sFingerprint, sTrustChain );
        constructTrustChain( tFingerprint, tTrustChain );

        if ( tTrustChain.contains( sFingerprint ) || sTrustChain.contains( tFingerprint ) )
        {
            return KeyTrustLevel.Full.getId();
        }
        else
        {
            for ( final String fingerprint : sTrustChain )
            {
                if ( tTrustChain.contains( fingerprint ) )
                {
                    return KeyTrustLevel.Marginal.getId();
                }
            }
        }
        return KeyTrustLevel.Never.getId();
    }


    /* *****************************
     *
     */
    private void constructTrustChain( String fingerprint, Set<String> chain )
    {
        chain.add( fingerprint );
        List<SecurityKeyTrust> keyTrusts = securityDataService.getKeyTrustData( fingerprint );
        for ( final SecurityKeyTrust keyTrust : keyTrusts )
        {
            if ( !chain.contains( keyTrust.getTargetFingerprint() ) )
            {
                constructTrustChain( keyTrust.getTargetFingerprint(), chain );
            }
        }
    }
}
