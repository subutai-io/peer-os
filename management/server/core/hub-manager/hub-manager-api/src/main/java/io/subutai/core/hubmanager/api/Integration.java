package io.subutai.core.hubmanager.api;


public interface Integration
{
    void sendHeartbeat() throws HubPluginException;

    void sendResourceHostInfo() throws HubPluginException;

    void registerPeer( String hupIp, String email, String password ) throws HubPluginException;

    String getHubDns() throws HubPluginException;

    String getProducts() throws HubPluginException;

    void installPlugin( String url ) throws HubPluginException;

    void uninstallPlugin( String url, String name );

    void unregisterPeer() throws HubPluginException;

    boolean getRegistrationState();
}
