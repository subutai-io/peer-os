package io.subutai.core.hubmanager.api;


/**
 * Implementers of this class can obtain Peer-Hub lifecycle events
 */
public interface HubEventListener
{
    void onRegistrationSucceeded();

    //add more events if needed
}
