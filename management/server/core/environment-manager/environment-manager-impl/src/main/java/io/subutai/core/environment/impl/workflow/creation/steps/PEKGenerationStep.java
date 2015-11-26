package io.subutai.core.environment.impl.workflow.creation.steps;


import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.subutai.common.environment.Environment;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.security.PublicKeyContainer;
import io.subutai.common.security.crypto.pgp.KeyPair;
import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.common.security.objects.KeyTrustLevel;
import io.subutai.common.security.objects.SecurityKeyType;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.EncryptionTool;
import io.subutai.core.security.api.crypto.KeyManager;


/**
 * PEK generation step
 */
public class PEKGenerationStep
{
    private final Topology topology;
    private final Environment environment;
    private final LocalPeer localPeer;
    private final User user;
    private SecurityManager securityManager;


    public PEKGenerationStep( final Topology topology, final Environment environment, final LocalPeer localPeer,
                              SecurityManager securityManager, User user )
    {
        this.topology = topology;
        this.environment = environment;
        this.localPeer = localPeer;
        this.securityManager = securityManager;
        this.user = user;
    }


    private PGPSecretKeyRing createEnvironmentKeyPair( EnvironmentId envId, String userSecKeyId ) throws PeerException
    {
        KeyManager keyManager = securityManager.getKeyManager();
        String pairId = envId.getId();
        final PGPSecretKeyRing userSecKeyRing = securityManager.getKeyManager().getSecretKeyRing( userSecKeyId );
        try
        {
            KeyPair keyPair = keyManager.generateKeyPair( pairId, false );

            //******Create PEK *****************************************************************
            PGPSecretKeyRing secRing = PGPKeyUtil.readSecretKeyRing( keyPair.getSecKeyring() );
            PGPPublicKeyRing pubRing = PGPKeyUtil.readPublicKeyRing( keyPair.getPubKeyring() );


            //***************Save Keys *********************************************************
            keyManager.saveSecretKeyRing( pairId, SecurityKeyType.PeerEnvironmentKey.getId(), secRing );
            keyManager.savePublicKeyRing( pairId, SecurityKeyType.PeerEnvironmentKey.getId(), pubRing );

            //***************Sign Keys *********************************************************
            securityManager.getKeyManager().signKey( userSecKeyRing, pubRing, KeyTrustLevel.Full.getId() );

            return secRing;
        }
        catch ( PGPException ex )
        {
            throw new PeerException( ex );
        }
    }


    public Map<Peer, String> execute() throws PeerException
    {
        PGPSecretKeyRing envSecKeyRing =
                createEnvironmentKeyPair( environment.getEnvironmentId(), user.getSecurityKeyId() );

        Set<Peer> peers = Sets.newHashSet( topology.getAllPeers() );
        peers.add( localPeer );

        Map<Peer, String> peerPekPubKeys = Maps.newHashMap();

        for ( final Peer peer : peers )
        {
            try
            {
                PublicKeyContainer publicKeyContainer =
                        peer.createPeerEnvironmentKeyPair( environment.getEnvironmentId() );

                PGPPublicKeyRing pubRing = PGPKeyUtil.readPublicKeyRing( publicKeyContainer.getKey() );

                PGPPublicKeyRing signedPEK =
                        securityManager.getKeyManager().signKey( envSecKeyRing, pubRing, KeyTrustLevel.Full.getId() );

                peer.updatePeerEnvironmentPubKey( environment.getEnvironmentId(), signedPEK );
            }
            catch ( PGPException e )
            {
                throw new PeerException( "Could not create PEK for: " + peer.getId() );
            }
        }

        return peerPekPubKeys;
    }
}
