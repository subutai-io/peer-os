package io.subutai.core.hubmanager.api.dao;


import io.subutai.core.hubmanager.api.model.Config;


public interface ConfigDataService
{
    void saveHubConfig( final Config config );

    Config getHubConfig( String peerId );

    void deleteConfig( final String peerId );

    String getPeerOwnerId( String peerId );

    boolean isPeerRegisteredToHub( String peerId );
}
