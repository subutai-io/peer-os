package io.subutai.bazaar.share.common;


/**
 * Implementers of this class can obtain Peer-Bazaar lifecycle events
 */
public interface BazaarEventListener
{
    void onRegistrationSucceeded();


    void onUnregister();

    //add more events if needed
}
