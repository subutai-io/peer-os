package io.subutai.core.hubmanager.api;


import java.util.Map;

import io.subutai.core.hubmanager.api.model.Config;
import io.subutai.hub.share.dto.SystemConfDto;


public interface HubManager
{
    void sendHeartbeat() throws HubPluginException;

    void triggerHeartbeat();

    void sendResourceHostInfo() throws HubPluginException;

    void registerPeer( String hupIp, String email, String password ) throws HubPluginException;

    String getHubDns() throws HubPluginException;

    String getProducts() throws HubPluginException;

    void installPlugin( String url, String filename ) throws HubPluginException;

    void uninstallPlugin( String name );

    void unregisterPeer() throws HubPluginException;

    boolean getRegistrationState();

    Map<String, String> getPeerInfo() throws HubPluginException;

    Config getHubConfiguration();

    String getChecksum();

    void sendSystemConfiguration( SystemConfDto dto );
}
