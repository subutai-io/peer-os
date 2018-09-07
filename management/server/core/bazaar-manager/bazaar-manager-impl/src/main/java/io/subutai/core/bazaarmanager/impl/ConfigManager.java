package io.subutai.core.bazaarmanager.impl;


import java.security.KeyStore;


import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;

import org.apache.cxf.jaxrs.client.WebClient;

import io.subutai.common.security.crypto.keystore.KeyStoreTool;
import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.common.security.objects.TokenType;
import io.subutai.common.settings.Common;
import io.subutai.common.settings.SecuritySettings;
import io.subutai.core.bazaarmanager.api.exception.BazaarManagerException;
import io.subutai.core.bazaarmanager.impl.http.HttpClient;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.api.model.UserToken;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.bazaar.share.pgp.key.PGPKeyHelper;
import io.subutai.bazaar.share.pgp.message.PGPMessenger;


public class ConfigManager
{

    private static final int BAZAAR_PORT = 444;

    private IdentityManager identityManager;
    private PeerManager peerManager;
    private PGPPublicKey bzrPublicKey;
    private PGPPublicKey ownerPublicKey;
    private PGPPublicKey peerPublicKey;
    private KeyStore keyStore;
    private String peerId;
    private PGPMessenger messenger;


    public PeerManager getPeerManager()
    {
        return peerManager;
    }


    public ConfigManager( final SecurityManager securityManager, final PeerManager peerManager,
                          final IdentityManager identityManager ) throws BazaarManagerException
    {
        try
        {

            this.identityManager = identityManager;

            this.peerManager = peerManager;

            final PGPPrivateKey sender = securityManager.getKeyManager().getPrivateKey( null );

            this.peerId = peerManager.getLocalPeer().getId();

            this.bzrPublicKey = PGPKeyHelper.readPublicKey( Common.BAZAAR_PUB_KEY );

            this.ownerPublicKey =
                    securityManager.getKeyManager().getPublicKeyRing( securityManager.getKeyManager().getPeerOwnerId() )
                                   .getPublicKey();

            this.peerPublicKey = securityManager.getKeyManager().getPublicKey( null );

            this.messenger = new PGPMessenger( sender, bzrPublicKey );

            final KeyStoreTool keyStoreTool = new KeyStoreTool();

            this.keyStore = keyStoreTool.createPeerCertKeystore( Common.PEER_CERT_ALIAS,
                    PGPKeyUtil.getFingerprint( peerPublicKey.getFingerprint() ) );
        }
        catch ( Exception e )
        {
            throw new BazaarManagerException( e );
        }
    }


    public String getPeerId()
    {
        return peerId;
    }


    PGPPublicKey getOwnerPublicKey()
    {
        return ownerPublicKey;
    }


    PGPPublicKey getPeerPublicKey()
    {
        return peerPublicKey;
    }


    public PGPMessenger getMessenger()
    {
        return messenger;
    }


    public WebClient getTrustedWebClientWithAuth( String path, final String bazaarIp ) throws BazaarManagerException
    {
        String baseUrl = String.format( "https://%s:%d", bazaarIp, BAZAAR_PORT );

        return HttpClient.createTrustedWebClientWithAuth( baseUrl + path, keyStore,
                SecuritySettings.KEYSTORE_PX1_PSW.toCharArray(), bzrPublicKey.getFingerprint() );
    }


    public String getbazaarIp()
    {
        return Common.BAZAAR_IP;
    }


    UserToken getUserToken()
    {
        User user = identityManager.getActiveUser();
        return identityManager.createUserToken( user, null, null, null, TokenType.SESSION.getId(), null );
    }


    User getActiveUser()
    {
        return identityManager.getActiveUser();
    }
}
