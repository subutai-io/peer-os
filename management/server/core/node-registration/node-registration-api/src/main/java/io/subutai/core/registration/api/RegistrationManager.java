package io.subutai.core.registration.api;


import java.util.List;
import java.util.UUID;

import io.subutai.common.environment.Environment;
import io.subutai.common.peer.ContainerHost;
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

    public void queueRequest( RequestedHost requestedHost ) throws NodeRegistrationException;

    public void rejectRequest( UUID requestId );

    public void approveRequest( UUID requestId );

    public void removeRequest( UUID requestId );

    public ContainerToken generateContainerTTLToken( Long ttl );

    public ContainerToken verifyToken( String token, String containerHostId, String publicKey )
            throws NodeRegistrationException;

    /**
     * Import empty environment by applying network configuration and persisting relevant containers
     * @param environment - environment
     * @param containerHosts - containers
     */
    public void importEnvironment(Environment environment, List<ContainerHost> containerHosts)
            throws NodeRegistrationException;

}
