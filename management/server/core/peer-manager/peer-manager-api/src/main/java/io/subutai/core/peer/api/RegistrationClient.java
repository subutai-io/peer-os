package io.subutai.core.peer.api;


import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.RegistrationData;


/**
 * REST client for registration process
 */
public interface RegistrationClient
{

    RegistrationData sendInitRequest( String destinationHost, RegistrationData registrationData )
            throws PeerException;

    void sendCancelRequest( String destinationHost, RegistrationData request ) throws PeerException;

    void sendRejectRequest( String destinationHost, RegistrationData request ) throws PeerException;

    void sendUnregisterRequest( String destinationHost, RegistrationData request ) throws PeerException;

    RegistrationData sendApproveRequest( String destinationHost, RegistrationData registrationData )
            throws PeerException;
}
