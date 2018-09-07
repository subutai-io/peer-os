package io.subutai.core.bazaarmanager.api.dao;


import io.subutai.core.bazaarmanager.api.model.Config;


public interface ConfigDataService
{
    void saveBazaarConfig( final Config config );

    Config getBazaarConfig( String peerId );

    void deleteConfig( final String peerId );

    String getPeerOwnerId( String peerId );

    boolean isPeerRegisteredToBazaar( String peerId );
}
