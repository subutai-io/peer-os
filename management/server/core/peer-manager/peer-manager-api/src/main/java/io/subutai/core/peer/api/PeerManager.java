package io.subutai.core.peer.api;


import java.util.List;

import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerInfo;
import io.subutai.common.peer.PeerPolicy;
import io.subutai.common.peer.RegistrationData;


public interface PeerManager
{
//    public static final String SOURCE_REMOTE_PEER = "PEER_REMOTE";
//    public static final String SOURCE_LOCAL_PEER = "PEER_LOCAL";

    void registerPeerActionListener( PeerActionListener peerActionListener );

    void unregisterPeerActionListener( PeerActionListener peerActionListener );

    /**
     * Updates peer metadata
     */
//    boolean update( PeerInfo peerInfo );

    /**
     * Returns all registered peers metadata objects
     */
//    public List<PeerInfo> getPeerInfos();

    void doUnregisterRequest( RegistrationData request ) throws PeerException;

    List<RegistrationData> getRegistrationRequests();

    /**
     * Returns local peer's metadata
     */
//    public PeerInfo getLocalPeerInfo();

    /**
     * Returns peer metadata by peer id
     */
//    public PeerInfo getPeerInfo( String id );

    /**
     * Returns peer instance by peer id
     */
    public Peer getPeer( String peerId );


    /**
     * Returns all peer instances
     */
    public List<Peer> getPeers();

    /**
     * Returns local peer instance
     */
    public LocalPeer getLocalPeer();

    void doRegistrationRequest( String destinationHost, String keyPhrase ) throws PeerException;

    void doApproveRequest( String keyPhrase, RegistrationData request ) throws PeerException;

    void doRejectRequest( RegistrationData request ) throws PeerException;

    void doCancelRequest( RegistrationData request ) throws PeerException;

    void processCancelRequest( RegistrationData registrationData ) throws PeerException;

    void processApproveRequest( RegistrationData registrationData ) throws PeerException;

    RegistrationData processRegistrationRequest( RegistrationData registrationData ) throws PeerException;

    void processUnregisterRequest( RegistrationData registrationData ) throws PeerException;

    void processRejectRequest( RegistrationData registrationData ) throws PeerException;

    String getPeerIdByIp( String ip ) throws PeerException;

    PeerPolicy getAvailablePolicy();
}
