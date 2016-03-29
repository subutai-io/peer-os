package io.subutai.core.hubmanager.impl.environment;


import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKeyRing;

import io.subutai.common.environment.Environment;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.security.PublicKeyContainer;
import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.common.security.objects.PermissionObject;
import io.subutai.common.security.relation.RelationLinkDto;


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
        RelationLinkDto envLink = new RelationLinkDto( dto.getEnvironmentId(), Environment.class.getSimpleName(),
                PermissionObject.EnvironmentManagement.getName(), "" );

        PublicKeyContainer publicKeyContainer = localPeer.createPeerEnvironmentKeyPair( envLink);

        PGPPublicKeyRing pubRing = PGPKeyUtil.readPublicKeyRing( publicKeyContainer.getKey() );

        localPeer.updatePeerEnvironmentPubKey( environmentId, pubRing );

        log.debug( "Generating PEK - END");
    }
}
