package io.subutai.core.hubmanager.api;


import java.util.Set;

import io.subutai.core.hubmanager.api.exception.HubManagerException;


public interface StateLinkProcessor
{
    /**
     * Returns true if next heartbeats should go faster. See HeartbeatProcessor for details.
     */
    boolean processStateLinks( Set<String> stateLinks ) throws HubManagerException;
}
