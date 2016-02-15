package io.subutai.core.environment.impl.workflow.creation.steps;


import java.util.Map;
import java.util.Set;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRing;

import com.google.common.collect.Maps;

import io.subutai.common.environment.Environment;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.security.PublicKeyContainer;
import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.common.security.objects.KeyTrustLevel;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;


/**
 * PEK generation step
 */
public class PEKGenerationStep
{
    private final Topology topology;
    private final Environment environment;
    private final PeerManager peerManager;
    private final User user;
    private SecurityManager securityManager;


    public PEKGenerationStep( final Topology topology, final Environment environment, final PeerManager peerManager,
                              SecurityManager securityManager, User user )
    {
        this.topology = topology;
        this.environment = environment;
        this.peerManager = peerManager;
        this.securityManager = securityManager;
        this.user = user;
    }


    //TODO this EK should be uploaded by user when creating environment via UI. @Nurkaly!
    //    private PGPSecretKeyRing createEnvironmentKeyPair( EnvironmentId envId, String userSecKeyId ) throws
    // PeerException
    //    {
    //        KeyManager keyManager = securityManager.getKeyManager();
    //        String pairId = envId.getId();
    //        final PGPSecretKeyRing userSecKeyRing = securityManager.getKeyManager().getSecretKeyRing( userSecKeyId );
    //        try
    //        {
    //            KeyPair keyPair = keyManager.generateKeyPair( pairId, false );
    //
    //            //******Create PEK *****************************************************************
    //            PGPSecretKeyRing secRing = PGPKeyUtil.readSecretKeyRing( keyPair.getSecKeyring() );
    //            PGPPublicKeyRing pubRing = PGPKeyUtil.readPublicKeyRing( keyPair.getPubKeyring() );
    //
    //            //***************Save Keys *********************************************************
    //            keyManager.saveSecretKeyRing( pairId, SecurityKeyType.EnvironmentKey.getId(), secRing );
    //            keyManager.savePublicKeyRing( pairId, SecurityKeyType.EnvironmentKey.getId(), pubRing );
    //
    //            //***************Sign Keys *********************************************************
    //            securityManager.getKeyManager().setKeyTrust( userSecKeyRing, pubRing, KeyTrustLevel.Full.getId() );
    //
    //            return secRing;
    //        }
    //        catch ( PGPException ex )
    //        {
    //            throw new PeerException( ex );
    //        }
    //    }


    public Map<Peer, String> execute() throws PeerException
    {
        PGPSecretKeyRing envSecKeyRing =
                securityManager.getKeyManager().getSecretKeyRing( environment.getEnvironmentId().getId() );

        Set<Peer> peers = peerManager.resolve( topology.getAllPeers() );

        peers.remove( peerManager.getLocalPeer() );

        Map<Peer, String> peerPekPubKeys = Maps.newHashMap();

        // first creating PEK for local peer
        PGPPublicKeyRing localPeerSignedPEK;
        try
        {
            PublicKeyContainer publicKeyContainer =
                    peerManager.getLocalPeer().createPeerEnvironmentKeyPair( environment.getEnvironmentId() );

            PGPPublicKeyRing pubRing = PGPKeyUtil.readPublicKeyRing( publicKeyContainer.getKey() );

            localPeerSignedPEK =
                    securityManager.getKeyManager().setKeyTrust( envSecKeyRing, pubRing, KeyTrustLevel.Full.getId() );

            peerManager.getLocalPeer()
                       .updatePeerEnvironmentPubKey( environment.getEnvironmentId(), localPeerSignedPEK );
        }
        catch ( PGPException e )
        {
            throw new PeerException( "Could not create PEK for: " + peerManager.getLocalPeer().getId() );
        }

        // creating PEK on remote peers
        for ( final Peer peer : peers )
        {
            try
            {
                PublicKeyContainer publicKeyContainer =
                        peer.createPeerEnvironmentKeyPair( environment.getEnvironmentId() );

                PGPPublicKeyRing pubRing = PGPKeyUtil.readPublicKeyRing( publicKeyContainer.getKey() );

                PGPPublicKeyRing signedPEK = securityManager.getKeyManager().setKeyTrust( envSecKeyRing, pubRing,
                        KeyTrustLevel.Full.getId() );

                peer.updatePeerEnvironmentPubKey( environment.getEnvironmentId(), signedPEK );
                peer.addPeerEnvironmentPubKey(
                        peerManager.getLocalPeer().getId() + "-" + environment.getEnvironmentId().getId(),
                        localPeerSignedPEK );

                peerManager.getLocalPeer().addPeerEnvironmentPubKey(
                        peer.getId() + "-" + environment.getEnvironmentId().getId(), signedPEK );
            }
            catch ( PGPException e )
            {
                throw new PeerException( "Could not create PEK for: " + peer.getId() );
            }
        }

        return peerPekPubKeys;
    }
}
