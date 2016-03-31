package io.subutai.core.hubmanager.impl.environment;


import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.PeerException;


class VniHelper extends Helper
{
    VniHelper( LocalPeer localPeer )
    {
        super( localPeer );
    }


    @Override
    void execute( PeerEnvironmentDto dto ) throws PeerException
    {
        //todo reimplement this
        //        Vni vni = new Vni( dto.getVniId(), dto.getEnvironmentId() );
        //
        //        Vni resultVni = localPeer.reserveVni( vni );
        //
        //        log.debug( "resultVni: {}", resultVni );
        //
        //        log.debug( "Peer reserved VNIs: {}", localPeer.getReservedVnis().list() );
    }
}
