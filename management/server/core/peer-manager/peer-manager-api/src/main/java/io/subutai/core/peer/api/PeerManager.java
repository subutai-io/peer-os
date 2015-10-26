package io.subutai.core.peer.api;


import java.util.List;
import java.util.Set;

import io.subutai.common.host.ContainerHostState;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerInfo;
import io.subutai.common.peer.RegistrationData;
import io.subutai.common.protocol.N2NConfig;


public interface PeerManager
{
    public static final String SOURCE_REMOTE_PEER = "PEER_REMOTE";
    public static final String SOURCE_LOCAL_PEER = "PEER_LOCAL";

    /**
     * Registers remote peer
     */
    //    boolean register( RegistrationRequest registrationRequest );

    //    boolean register( PeerInfo peerInfo, String cert );

    /**
     * Updates peer metadata
     */
    boolean update( PeerInfo peerInfo );

    /**
     * Returns all registered peers metadata objects
     */
    public List<PeerInfo> getPeerInfos();

    void doUnregisterRequest( RegistrationData request ) throws PeerException;

    List<RegistrationData> getRegistrationRequests();

    /**
     * Returns local peer's metadata
     */
    public PeerInfo getLocalPeerInfo();

    /**
     * Returns peer metadata by peer id
     */
    public PeerInfo getPeerInfo( String id );

    /**
     * Unregisters peer
     */
    @Deprecated
    boolean unregister( PeerInfo peerInfo, String keyPhrase ) throws PeerException;

    //
    @Deprecated
    boolean unregister( String id ) throws PeerException;

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

    List<N2NConfig> setupN2NConnection( final Set<Peer> peers ) throws PeerException;


    void doRegistrationRequest( String destinationHost, String keyPhrase ) throws PeerException;

    void doApproveRequest( RegistrationData request ) throws PeerException;

    void doRejectRequest( RegistrationData request ) throws PeerException;

    void doCancelRequest( RegistrationData request ) throws PeerException;

    void processCancelRequest( RegistrationData registrationData ) throws PeerException;

    RegistrationData processApproveRequest( RegistrationData registrationData ) throws PeerException;

    RegistrationData processRegistrationRequest( RegistrationData registrationData ) throws PeerException;

    void processUnregisterRequest( RegistrationData registrationData ) throws PeerException;

    void processRejectRequest( RegistrationData registrationData ) throws PeerException;

    @Deprecated
    boolean register( PeerInfo remotePeerInfo ) throws PeerException;

    void startContainer( ContainerId containerId ) throws PeerException;

    void stopContainer( ContainerId containerId ) throws PeerException;

    void destroyContainer( ContainerId containerId ) throws PeerException;

    ContainerHostState getContainerState( ContainerId containerId );

    String getPeerIdByIp( String ip ) throws PeerException;
}
