package io.subutai.core.hubmanager.impl.environment;


import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKeyRing;

import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.security.PublicKeyContainer;
import io.subutai.common.security.crypto.pgp.PGPKeyUtil;


class PekHelper extends Helper
{
    PekHelper( LocalPeer localPeer )
    {
        super( localPeer );
    }


    @Override
    void execute( PeerEnvironmentDto dto ) throws PeerException, PGPException
    {
        log.debug( "Generating PEK - START");

        EnvironmentId environmentId = new EnvironmentId( dto.getEnvironmentId() );

        PublicKeyContainer publicKeyContainer = localPeer.createPeerEnvironmentKeyPair( environmentId );

        PGPPublicKeyRing pubRing = PGPKeyUtil.readPublicKeyRing( publicKeyContainer.getKey() );

        localPeer.updatePeerEnvironmentPubKey( environmentId, pubRing );

        log.debug( "Generating PEK - END");
    }
}
