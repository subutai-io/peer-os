package io.subutai.core.hubadapter.api;


import java.util.List;

//TODO put this API to hub-share module and its impl to hub-manager module
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

    <T> T getPluginDataByKey( String pluginKey, String key, Class<T> clazz );

    boolean uploadPluginData( String pluginKey, String key, Object data );
}
