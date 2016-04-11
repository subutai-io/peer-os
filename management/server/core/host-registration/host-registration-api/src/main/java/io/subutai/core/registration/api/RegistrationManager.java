package io.subutai.core.registration.api;


import java.util.List;

import io.subutai.core.registration.api.exception.HostRegistrationException;
import io.subutai.core.registration.api.service.ContainerToken;
import io.subutai.core.registration.api.service.RequestedHost;


public interface RegistrationManager
{
    public List<RequestedHost> getRequests();

    public RequestedHost getRequest( String requestId );

    public void queueRequest( RequestedHost requestedHost ) throws HostRegistrationException;

    public void rejectRequest( String requestId ) throws HostRegistrationException;

    public void approveRequest( String requestId ) throws HostRegistrationException;

    public void removeRequest( String requestId ) throws HostRegistrationException;

    public ContainerToken generateContainerTTLToken( Long ttl ) throws HostRegistrationException;

    public ContainerToken verifyToken( String token, String containerHostId, String publicKey )
            throws HostRegistrationException;
}
