package io.subutai.core.hubmanager.api.dao;


import io.subutai.core.hubmanager.api.model.Config;


/**
 * Created by ermek on 10/28/15.
 */
public interface ConfigDataService
{
    void saveHubConfig( final Config config );

    Config getHubConfig( String peerId );

    void deleteConfig( final String peerId );
}
