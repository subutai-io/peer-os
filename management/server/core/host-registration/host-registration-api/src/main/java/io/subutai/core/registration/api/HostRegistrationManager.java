package io.subutai.core.registration.api;


import java.util.List;

import io.subutai.core.registration.api.exception.HostRegistrationException;
import io.subutai.core.registration.api.service.ContainerToken;
import io.subutai.core.registration.api.service.RequestedHost;


public interface HostRegistrationManager
{
    List<RequestedHost> getRequests();

    RequestedHost getRequest( String requestId );

    void queueRequest( RequestedHost requestedHost ) throws HostRegistrationException;

    void rejectRequest( String requestId ) throws HostRegistrationException;

    void approveRequest( String requestId ) throws HostRegistrationException;

    void removeRequest( String requestId ) throws HostRegistrationException;

    void unblockRequest( String requestId ) throws HostRegistrationException;

    ContainerToken generateContainerTTLToken( long ttlInMs ) throws HostRegistrationException;

    ContainerToken verifyToken( String token, String containerHostId, String publicKey )
            throws HostRegistrationException;

    void changeRhHostname( String rhId, String hostname ) throws HostRegistrationException;
}
