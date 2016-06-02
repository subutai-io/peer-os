package io.subutai.core.hubmanager.impl;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.Calendar;
import java.util.Date;

import javax.ws.rs.core.Response;

import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxrs.client.WebClient;

import io.subutai.common.security.crypto.keystore.KeyStoreTool;
import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.common.settings.Common;
import io.subutai.common.settings.SecuritySettings;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.api.model.UserToken;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.hub.share.pgp.key.PGPKeyHelper;
import io.subutai.hub.share.pgp.message.PGPMessenger;


public class ConfigManager
{
    private static final Logger LOG = LoggerFactory.getLogger( ConfigManager.class.getName() );

    private static final int HUB_PORT = 444;

    private IdentityManager identityManager;
    private PeerManager peerManager;

    private PGPPublicKey hPublicKey;
    private PGPPublicKey ownerPublicKey;
    private PGPPublicKey peerPublicKey;
    private PGPPrivateKey sender;
    private KeyStore keyStore;
    private String peerId;
    private PGPMessenger messenger;

    private KeyStoreTool keyStoreTool;


    public PeerManager getPeerManager()
    {
        return peerManager;
    }


    public ConfigManager( final SecurityManager securityManager, final PeerManager peerManager,
                          final IdentityManager identityManager ) throws Exception
    {
        this.identityManager = identityManager;

        this.peerManager = peerManager;

        this.sender = securityManager.getKeyManager().getPrivateKey( null );

        this.peerId = peerManager.getLocalPeer().getId();

        this.hPublicKey = PGPKeyHelper.readPublicKey( Common.H_PUB_KEY );

        this.ownerPublicKey =
                securityManager.getKeyManager().getPublicKeyRing( securityManager.getKeyManager().getPeerOwnerId() )
                               .getPublicKey();

        this.peerPublicKey = securityManager.getKeyManager().getPublicKey( null );

        this.messenger = new PGPMessenger( sender, hPublicKey );

        this.keyStoreTool = new KeyStoreTool();

        this.keyStore = keyStoreTool.createPeerCertKeystore( Common.PEER_CERT_ALIAS,
                PGPKeyUtil.getFingerprint( peerPublicKey.getFingerprint() ) );
    }


    public String getPeerId()
    {
        return peerId;
    }


    public PGPPublicKey getOwnerPublicKey()
    {
        return ownerPublicKey;
    }


    public PGPPublicKey getPeerPublicKey()
    {
        return peerPublicKey;
    }


    public PGPMessenger getMessenger()
    {
        return messenger;
    }


    public WebClient getTrustedWebClientWithAuth( String path, final String hubIp )
            throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException
    {
        String baseUrl = String.format( "https://%s:" + HUB_PORT, hubIp );

        return HttpClient.createTrustedWebClientWithAuth( baseUrl + path, keyStore,
                SecuritySettings.KEYSTORE_PX1_PSW.toCharArray(), hPublicKey.getFingerprint() );
    }


    public String getHubIp()
    {
        return "hub.subut.ai";
    }


    public byte[] readContent( Response response ) throws IOException
    {
        if ( response.getEntity() == null )
        {
            return null;
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        InputStream is = ( ( InputStream ) response.getEntity() );

        IOUtils.copy( is, bos );
        return bos.toByteArray();
    }


    public UserToken getPermanentToken()
    {
        Date newDate = new Date();
        java.util.Calendar cal = Calendar.getInstance();
        cal.setTime( newDate );
        cal.add( Calendar.YEAR, 2 );

        User user = identityManager.getActiveUser();

        return identityManager.createUserToken( user, null, null, null, 2, cal.getTime() );
    }


    public User getActiveUser()
    {
        return identityManager.getActiveUser();
    }
}
