package io.subutai.core.hubadapter.api;


public interface HubAdapter
{
    String getUserEnvironmentsForPeer();

    void destroyContainer( String envId, String containerId );

    void uploadEnvironment( String json );

    void removeEnvironment( String envId );
}
