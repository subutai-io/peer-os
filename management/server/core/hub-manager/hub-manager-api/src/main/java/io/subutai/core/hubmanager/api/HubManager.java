package io.subutai.core.hubmanager.api;


import java.util.Map;

import io.subutai.core.hubmanager.api.model.Config;
import io.subutai.hub.share.dto.SystemConfDto;


public interface HubManager
{
    void registerPeer( String hupIp, String email, String password ) throws HubPluginException;

    void unregisterPeer() throws HubPluginException;

    boolean isRegistered();

    void sendHeartbeat() throws HubPluginException;

    void triggerHeartbeat();

    void sendResourceHostInfo() throws HubPluginException;

    String getHubDns() throws HubPluginException;

    String getProducts() throws HubPluginException;

    void installPlugin( String url, String filename ) throws HubPluginException;

    void uninstallPlugin( String name );



    Map<String, String> getPeerInfo() throws HubPluginException;

    Config getHubConfiguration();

    String getChecksum();

    void sendSystemConfiguration( SystemConfDto dto );
}
