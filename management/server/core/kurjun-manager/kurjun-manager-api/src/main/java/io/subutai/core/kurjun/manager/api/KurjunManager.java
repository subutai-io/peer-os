package io.subutai.core.kurjun.manager.api;


import org.apache.commons.configuration.ConfigurationException;

import io.subutai.core.kurjun.manager.api.dao.KurjunDataService;


/**
 *
 */
public interface KurjunManager
{

    //****************************************
    String registerUser( int id );


    //****************************************
    String authorizeUser( int id, String signedMessage );


    //****************************************
    boolean setSystemOwner( String url, int kurjunType );


    //****************************************
    String getSystemOwner( int kurjunType );


    //****************************************
    String getUser( int id );


    //****************************************
    KurjunDataService getDataService();


    //****************************************
    void saveUrl( String url, int type ) throws ConfigurationException;


    //****************************************
    void updateUrl( int id, String url ) throws ConfigurationException;
}
