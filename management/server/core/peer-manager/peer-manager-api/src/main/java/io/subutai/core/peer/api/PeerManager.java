package io.subutai.core.peer.api;


import java.util.List;
import java.util.Set;

import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerNotRegisteredException;
import io.subutai.common.peer.PeerPolicy;
import io.subutai.common.peer.RegistrationData;
import io.subutai.common.peer.RegistrationStatus;
import io.subutai.common.peer.RemotePeer;


public interface PeerManager
{

    void registerPeerActionListener( PeerActionListener peerActionListener );

    void unregisterPeerActionListener( PeerActionListener peerActionListener );

    List<RegistrationData> getRegistrationRequests();

    /**
     * Returns peer instance by peer id throws PeerException if peer is not found
     */
    Peer getPeer( String peerId ) throws PeerException;

    /**
     * Returns remote peer instance by peer id or null if not found
     */
    RemotePeer findPeer( String peerId );

    /**
     * Returns all peer instances
     */
    List<Peer> getPeers();

    /**
     * Returns local peer instance
     */
    LocalPeer getLocalPeer();

    void doRegistrationRequest( String destinationHost, String keyPhrase ) throws PeerException;

    void doUnregisterRequest( String peerId, boolean forceAction ) throws PeerException;

    void doApproveRequest( String keyPhrase, String peerId ) throws PeerException;

    void doRejectRequest( String peerId, boolean forceAction ) throws PeerException;

    void doCancelRequest( String peerId, boolean forceAction ) throws PeerException;

    void processCancelRequest( RegistrationData registrationData ) throws PeerException;

    void processApproveRequest( RegistrationData registrationData ) throws PeerException;

    RegistrationData processRegistrationRequest( RegistrationData registrationData ) throws PeerException;

    void processUnregisterRequest( RegistrationData registrationData ) throws PeerException;

    void processRejectRequest( RegistrationData registrationData ) throws PeerException;

    String getRemotePeerIdByIp( String ip ) throws PeerNotRegisteredException;

    PeerPolicy getAvailablePolicy();

    RegistrationStatus getRegistrationStatus( String peerId );

    RegistrationStatus getRemoteRegistrationStatus( String peerId );

    PeerPolicy getPolicy( String peerId );

    void setPolicy( String peerId, PeerPolicy peerPolicy ) throws PeerException;

    Set<Peer> resolve( Set<String> peers ) throws PeerException;

    void setPublicUrl( String peerId, String publicUrl, int securePort, boolean userRhIp ) throws PeerException;

    void checkHostAvailability( final String destinationHost ) throws PeerException;

    void setName( String peerId, String newName ) throws PeerException;

    void updatePeerUrl( String peerId, String ip ) throws PeerException;
}
