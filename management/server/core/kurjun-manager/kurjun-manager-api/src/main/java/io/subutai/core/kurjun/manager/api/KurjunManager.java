package io.subutai.core.kurjun.manager.api;


import org.apache.commons.configuration.ConfigurationException;

import io.subutai.core.kurjun.manager.api.dao.KurjunDataService;


/**
 *
 */
public interface KurjunManager
{

    //****************************************
    String registerUser( String url, int kurjunType );


    //****************************************
    String authorizeUser( String url, int kurjunType, String signedMessage );


    //****************************************
    boolean setSystemOwner( String url, int kurjunType );


    //****************************************
    String getSystemOwner( int kurjunType );


    //****************************************
    String getUser( String url, int kurjunType );


    //****************************************
    KurjunDataService getDataService();


    //****************************************
    void saveUrl(String url, int type) throws ConfigurationException;

}
