package io.subutai.hub.share.common;


import io.subutai.hub.share.dto.PeerProductDataDto;


/**
 * Implementers of this class can obtain Peer-Hub lifecycle events
 */
public interface HubEventListener
{
    void onRegistrationSucceeded();

    void onPluginEvent( String pluginUid, PeerProductDataDto.State state );

    void onUnregister();

    //add more events if needed
}
