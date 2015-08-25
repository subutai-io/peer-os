package io.subutai.core.registration.api;


import java.util.List;
import java.util.UUID;

import io.subutai.core.registration.api.resource.host.RequestedHost;


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
}
