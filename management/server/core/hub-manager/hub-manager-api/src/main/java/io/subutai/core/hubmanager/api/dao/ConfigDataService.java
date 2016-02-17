package io.subutai.core.hubmanager.api.dao;


import io.subutai.core.hubmanager.api.model.Config;


/**
 * Created by ermek on 10/28/15.
 */
public interface ConfigDataService
{
    public void saveHubConfig( final Config config );

    public Config getHubConfig( String peerId );
}
