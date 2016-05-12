package io.subutai.core.hubadapter.api;


import java.util.List;


public interface HubAdapter
{
    public boolean isRegistered();
    //
    // Environments
    //

    String getUserEnvironmentsForPeer();

    void destroyContainer( String envId, String containerId );

    void uploadEnvironment( String json );

    void removeEnvironment( String envId );

    //
    // Plugins
    //

    <T> List<T> getPluginData( String pluginKey, Class<T> clazz );

    boolean uploadPluginData( String pluginKey, String key, Object data );
}
