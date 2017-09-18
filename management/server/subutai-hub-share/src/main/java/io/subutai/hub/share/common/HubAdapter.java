package io.subutai.hub.share.common;


import java.util.List;


public interface HubAdapter
{
    //
    // Environments
    //

    String getUserEnvironmentsForPeer();

    String getAllEnvironmentsForPeer();

    String getDeletedEnvironmentsForPeer();

    void destroyContainer( String envId, String containerId );

    void uploadEnvironment( String json );

    boolean uploadPeerOwnerEnvironment( String json );

    void removeEnvironment( String envId );

    void removeSshKey( String envId, String sshKey );

    void addSshKey( String envId, String sshKey );

    //
    // Plugins
    //

    <T> List<T> getPluginData( String pluginKey, Class<T> clazz );

    <T> T getPluginDataByKey( String pluginKey, String key, Class<T> clazz );

    boolean uploadPluginData( String pluginKey, String key, Object data );

    boolean deletePluginData( String pluginKey, String key );
}
