package io.subutai.core.environment.impl.workflow.creation.steps.helpers;


import java.util.concurrent.Callable;

import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRing;

import io.subutai.common.environment.Environment;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Peer;
import io.subutai.common.security.PublicKeyContainer;
import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.common.security.objects.KeyTrustLevel;
import io.subutai.core.security.api.crypto.KeyManager;

public class PeerEnvironmentKeyTask implements Callable<Peer>
{
    private final LocalPeer localPeer;
    private final PGPSecretKeyRing envSecKeyRing;
    private final PGPPublicKeyRing localPeerSignedPEK;
    private final Environment environment;
    private final Peer peer;
    private final KeyManager keyManager;


    public PeerEnvironmentKeyTask( final LocalPeer localPeer, final PGPSecretKeyRing envSecKeyRing,
                                   final PGPPublicKeyRing localPeerSignedPEK, final Environment environment,
                                   final Peer peer, final KeyManager keyManager )
    {
        this.localPeer = localPeer;
        this.envSecKeyRing = envSecKeyRing;
        this.localPeerSignedPEK = localPeerSignedPEK;
        this.environment = environment;
        this.peer = peer;
        this.keyManager = keyManager;
    }


    @Override
    public Peer call() throws Exception
    {
        PublicKeyContainer publicKeyContainer = peer.createPeerEnvironmentKeyPair( environment.getEnvironmentId() );

        PGPPublicKeyRing pubRing = PGPKeyUtil.readPublicKeyRing( publicKeyContainer.getKey() );

        PGPPublicKeyRing signedPEK = keyManager.setKeyTrust( envSecKeyRing, pubRing, KeyTrustLevel.Full.getId() );

        peer.updatePeerEnvironmentPubKey( environment.getEnvironmentId(), signedPEK );
        peer.addPeerEnvironmentPubKey( localPeer.getId() + "-" + environment.getEnvironmentId().getId(),
                localPeerSignedPEK );

        localPeer.addPeerEnvironmentPubKey( peer.getId() + "-" + environment.getEnvironmentId().getId(), signedPEK );

        return peer;
    }
}

