package io.subutai.core.bazaarmanager.api;


import java.util.Set;

import io.subutai.core.bazaarmanager.api.exception.BazaarManagerException;


public interface StateLinkProcessor
{
    /**
     * Returns true if next heartbeats should go faster. See HeartbeatProcessor for details.
     */
    boolean processStateLinks( Set<String> stateLinks ) throws BazaarManagerException;
}
