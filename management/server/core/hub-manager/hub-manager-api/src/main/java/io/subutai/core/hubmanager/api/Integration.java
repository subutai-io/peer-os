package io.subutai.core.hubmanager.api;


public interface Integration
{
    public void sendHeartbeat() throws HubPluginException;

    public void sendResourceHostInfo() throws HubPluginException;

    public void registerPeer( String hupIp, String email, String password ) throws HubPluginException;

    public String getHubDns() throws HubPluginException;

    public String getProducts() throws HubPluginException;

    public void installPlugin( String url ) throws HubPluginException;

    public void uninstallPlugin( String url );
}
