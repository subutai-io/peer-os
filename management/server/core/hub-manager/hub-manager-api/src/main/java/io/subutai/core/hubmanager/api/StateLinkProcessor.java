package io.subutai.core.hubmanager.api;


import java.util.Set;


public interface StateLinkProcessor
{
    /**
     * Returns true if next heartbeats should go faster. See HeartbeatProcessor for details.
     */
    boolean processStateLinks( Set<String> stateLinks ) throws Exception;
}
