package io.subutai.core.peer.api;


import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerInfo;
import io.subutai.common.peer.RegistrationRequest;


/**
 * REST client for registration process
 */
public interface RegistrationClient
{

    RegistrationRequest sendInitRequest( String destinationHost, RegistrationRequest registrationRequest )
            throws PeerException;

    void sendCancelRequest( String destinationHost, RegistrationRequest request ) throws PeerException;

    void sendRejectRequest( String destinationHost, RegistrationRequest request ) throws PeerException;

    void sendUnregisterRequest( String destinationHost, RegistrationRequest request ) throws PeerException;

    RegistrationRequest sendApproveRequest( String destinationHost, RegistrationRequest registrationRequest )
            throws PeerException;
}
