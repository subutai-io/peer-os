package io.subutai.core.hubmanager.api;


import java.util.Map;

import io.subutai.core.hubmanager.api.exception.HubManagerException;
import io.subutai.core.hubmanager.api.model.Config;
import io.subutai.hub.share.dto.SystemConfDto;


public interface HubManager
{
    String HUB_EMAIL_SUFFIX = "@hub.subut.ai";

    void registerPeer( String email, String password, String peerName, String peerScope ) throws HubManagerException;

    void unregisterPeer() throws HubManagerException;

    String getPeerName();

    void sendHeartbeat() throws HubManagerException;

    void triggerHeartbeat();

    void sendResourceHostInfo() throws HubManagerException;

    String getHubDns() throws HubManagerException;

    String getProducts() throws HubManagerException;

    void installPlugin( String url, String filename, String uid ) throws HubManagerException;

    void uninstallPlugin( String name, String uid );

    Map<String, String> getPeerInfo() throws HubManagerException;

    Config getHubConfiguration();

    String getChecksum();

    void sendSystemConfiguration( SystemConfDto dto );

    String getCurrentUserEmail();

    boolean isRegisteredWithHub();

    boolean isHubReachable();

    boolean canWorkWithHub();
}
