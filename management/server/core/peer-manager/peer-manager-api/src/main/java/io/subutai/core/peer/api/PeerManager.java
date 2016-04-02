package io.subutai.core.peer.api;


import java.util.List;
import java.util.Set;

import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerPolicy;
import io.subutai.common.peer.RegistrationData;
import io.subutai.common.protocol.PingDistances;
import io.subutai.common.resource.PeerGroupResources;


public interface PeerManager
{


    void registerPeerActionListener( PeerActionListener peerActionListener );

    void unregisterPeerActionListener( PeerActionListener peerActionListener );


    void doUnregisterRequest( RegistrationData request ) throws PeerException;

    List<RegistrationData> getRegistrationRequests();


    /**
     * Returns peer instance by peer id
     */
    public Peer getPeer( String peerId ) throws PeerException;


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

    String getRemotePeerIdByIp( String ip ) throws PeerException;

    PeerGroupResources getPeerGroupResources() throws PeerException;

    PeerPolicy getAvailablePolicy();

    PeerPolicy getPolicy( String peerId );

    void setPolicy( String peerId, PeerPolicy peerPolicy ) throws PeerException;

    Set<Peer> resolve( Set<String> peers ) throws PeerException;

    PingDistances getP2PSwarmDistances();

    void setPublicUrl( String peerId, String publicUrl, int securePort ) throws PeerException;
}
