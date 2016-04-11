package io.subutai.core.registration.api;


import java.io.UnsupportedEncodingException;
import java.util.List;

import io.subutai.common.peer.HostNotFoundException;
import io.subutai.core.registration.api.exception.NodeRegistrationException;
import io.subutai.core.registration.api.service.ContainerToken;
import io.subutai.core.registration.api.service.RequestedHost;


public interface RegistrationManager
{
    public List<RequestedHost> getRequests();

    public RequestedHost getRequest( String requestId );

    public void queueRequest( RequestedHost requestedHost ) throws NodeRegistrationException;

    public void rejectRequest( String requestId ) throws UnsupportedEncodingException;

    public void approveRequest( String requestId );

    public void removeRequest( String requestId ) throws HostNotFoundException;

    public ContainerToken generateContainerTTLToken( Long ttl );

    public ContainerToken verifyToken( String token, String containerHostId, String publicKey )
            throws NodeRegistrationException;
}
