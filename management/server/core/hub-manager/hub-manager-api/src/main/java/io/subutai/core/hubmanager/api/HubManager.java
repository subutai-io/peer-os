package io.subutai.core.hubmanager.api;


import java.util.Map;

import io.subutai.core.hubmanager.api.exception.HubManagerException;
import io.subutai.core.hubmanager.api.model.Config;
import io.subutai.core.identity.api.model.UserToken;
import io.subutai.hub.share.dto.BrokerSettingsDto;

public interface HubManager
{
    String HUB_EMAIL_SUFFIX = "@bazaar.subutai.io";

    void registerPeer( String email, String password, String peerName, String peerScope ) throws HubManagerException;

    void unregisterPeer() throws HubManagerException;

    String getPeerName();

    void sendHeartbeat() throws HubManagerException;

    void triggerHeartbeat();

    String getHubDns() throws HubManagerException;

    Map<String, String> getPeerInfo() throws HubManagerException;

    Config getHubConfiguration();

    String getCurrentUserEmail();

    boolean isRegisteredWithHub();

    boolean isHubReachable();

    boolean canWorkWithHub();

    boolean isPeerUpdating();

    RestClient getRestClient();

    boolean hasHubTasksInAction();

    void notifyHubThatPeerIsOffline();

    BrokerSettingsDto getBrokers();

    void sendPeersMertics() throws HubManagerException;

    void sendContainerMertics() throws HubManagerException;

    void schedulePeerMetrics();

    UserToken getUserToken( String envOwnerId, String peerId );
}
