package io.subutai.core.registration.api;


import java.util.List;
import java.util.UUID;

import io.subutai.core.registration.api.exception.NodeRegistrationException;
import io.subutai.core.registration.api.service.ContainerToken;
import io.subutai.core.registration.api.service.RequestedHost;


/**
 * Created by talas on 8/24/15.
 */
public interface RegistrationManager
{

    public List<RequestedHost> getRequests();

    public RequestedHost getRequest( UUID requestId );

    public void queueRequest( RequestedHost requestedHost );

    public void rejectRequest( UUID requestId );

    public void approveRequest( UUID requestId );

    public void removeRequest( UUID requestId );

    public ContainerToken generateContainerTTLToken( String containerHostId, Long ttl );

    public ContainerToken verifyToken( String token ) throws NodeRegistrationException;

}
